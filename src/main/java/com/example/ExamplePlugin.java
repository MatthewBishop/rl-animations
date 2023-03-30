package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.events.PlayerSpawned;
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
	protected void startUp() throws Exception
	{
		gson = new Gson();
		FileReader reader = null;
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
		}
	}

	void write(HashMap object, String name) {
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

	//See ZalcanoPlugin
/*	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		event.getProjectile().getAnimation();
	}

	//See TimersPlugin
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {

	}*/

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
