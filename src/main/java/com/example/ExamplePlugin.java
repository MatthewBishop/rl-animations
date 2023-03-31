package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(ExamplePlugin.class);
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	private Gson gson;
	private HashMap<Integer, AnimationData> anims = new HashMap<>();
	private HashMap<Integer, ProjectileData> projs = new HashMap<>();

	void logAnimation(Player player) {
		int itemId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);

		if(itemId >-1) {
			if(!client.getWorldType().contains(WorldType.MEMBERS)) {
				ItemComposition definition = client.getItemDefinition(itemId);
				if (definition.isMembers()) {
					//skip animations from member items on f2p worlds.
					//this is because on f2p worlds member items use the same set of animations.
					//"stand":808,"walk":819,"run":824,"walkR180":820,"walkRRight":822,"walkRLeft":821,"standRRight":823,"standRLeft":823
					return;
				}
			}
		}

		AnimationData d = new AnimationData(player);

		int hash = d.hashCode();

		AnimationData data = anims.get(hash);
		if(data == null) {
			anims.put(hash, d);
			data = d;
		}
		data.ids.add(itemId);
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		logAnimation(event.getPlayer());
	}

	@Subscribe
	public void onPlayerChanged(PlayerChanged event)
	{
		logAnimation(event.getPlayer());
	}

	@Override
	protected void startUp()
	{
		gson = new Gson();
		FileReader reader = null;

		try {
			reader = new FileReader("./projectiles.json");
			ProjectileData[] defs = gson.fromJson(reader, ProjectileData[].class);
			for (ProjectileData def : defs) {
				projs.put(def.hashCode(), def);
			}
		} catch (FileNotFoundException e) {
			//File is missing. Catch exception so Runelite doesnt kill plugin.
		}

		try {
			reader = new FileReader("./item-animations.json");
			AnimationData[] defs = gson.fromJson(reader, AnimationData[].class);
			for (AnimationData def : defs) {
				anims.put(def.hashCode(), def);
			}
		} catch (FileNotFoundException e) {
			//File is missing. Catch exception so Runelite doesnt kill plugin.
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		//TODO, hook for plugin shutdown. if needed.
	}

	private boolean started = false;

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if(gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			started = true;
		}
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN && started) {
			write(anims, "./item-animations.json");
			write(projs, "./projectiles.json");
		}
	}

	private void write(HashMap object, String name) {
		GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
		Gson gson2 = builder.setPrettyPrinting().create();
		try (OutputStream outputStream = new FileOutputStream(name)){
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII));
			gson2.toJson(object.values(), bufferedWriter);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getHeightmapY(int sceneX, int sceneZ, int level) {
		int tileX = sceneX >> 7;
		int tileZ = sceneZ >> 7;
		if(tileX >= 0 && tileZ >= 0 && tileX <= 103 && tileZ <= 103) {
			int realLevel = level;
			if(level < 3 && (client.getTileSettings()[1][tileX][tileZ] & 2) == 2) {
				realLevel = level + 1;
			}

			int tileLocalX = sceneX & 127;
			int tileLocalZ = sceneZ & 127;
			int y00 = (128 - tileLocalX) * client.getTileHeights()[realLevel][tileX][tileZ] + tileLocalX * client.getTileHeights()[realLevel][tileX + 1][tileZ] >> 7;
			int y11 = tileLocalX * client.getTileHeights()[realLevel][tileX + 1][tileZ + 1] + client.getTileHeights()[realLevel][tileX][tileZ + 1] * (128 - tileLocalX) >> 7;
			return y11 * tileLocalZ + y00 * (128 - tileLocalZ) >> 7;
		} else {
			return 0;
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		//onProjectileMoved is called within the updateVelocity function.
		//this function is called immediately following the creation of a new projectile object.
		//as a result, brand new projectiles will have a velocity values and scalar of zero.
		final Projectile projectile = event.getProjectile();
		final LocalPoint dest = event.getPosition();

		//only check for new projectiles.
		if(projectile.getScalar() == 0.0d && projectile.getVelocityX() == 0.0d && projectile.getVelocityY() == 0.0d && projectile.getVelocityZ() == 0.0d) {

			int srcX = projectile.getX1();
			int srcY = projectile.getY1();

			int startHeight = (getHeightmapY(srcX, srcY, client.getPlane()) - projectile.getHeight()) / 4;
			int endHeight = projectile.getEndHeight() / 4;

			int delay = projectile.getStartCycle() - client.getGameCycle();
			int speed = projectile.getEndCycle() - client.getGameCycle();

			int delta1 = Math.abs(fix(srcX) - fix(dest.getX()));//deltaX
			int delta2 = Math.abs(fix(srcY) - fix(dest.getY()));//deltaY

			//WIP
			if(delta1 > delta2) {
				int var = delta1;
				delta1 = delta2;
				delta2 = var;
			}

			ProjectileData d = new ProjectileData(projectile.getId(), startHeight, endHeight, delay, speed, projectile.getSlope(), projectile.getStartHeight(), delta2, delta1);

			int hash = d.hashCode();

			ProjectileData data = projs.get(hash);
			if(data == null) {
				projs.put(hash, d);
				data = d;
			}
			data.ids.add(projectile.getId());
		}
	}

	private static int fix(int i) {
		return (i - 64) / 128;
	}

/*
	//See TimersPlugin, TODO maybe log the weapon/ammo used using this event? Or maybe in the player update packet?
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {

	}*/

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
