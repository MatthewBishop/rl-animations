package com.example;

import net.runelite.api.Player;

import java.util.*;

public class AnimationData {

/*
getIdleRotateLeft()
getIdleRotateRight()
getWalkRotateLeft()
getWalkRotateRight()
getWalkRotate180()


getWalkAnimation()
getIdlePoseAnimation()
getRunAnimation()
*/

    public AnimationData(Player player) {
        this.run = player.getRunAnimation();
        this.stand = player.getIdlePoseAnimation();
        this.walk = player.getWalkAnimation();

        this.standRLeft = player.getIdleRotateLeft();
        this.standRRight = player.getIdleRotateRight();
        this.walkRLeft = player.getWalkRotateLeft();
        this.walkRRight = player.getWalkRotateRight();
        this.walkR180 = player.getWalkRotate180();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimationData that = (AnimationData) o;
        return stand == that.stand && walk == that.walk && run == that.run && walkR180 == that.walkR180 && walkRRight == that.walkRRight && walkRLeft == that.walkRLeft && standRRight == that.standRRight && standRLeft == that.standRLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stand, walk, run, walkR180, walkRRight, walkRLeft, standRRight, standRLeft);
    }

    public Set<Integer> ids = new HashSet<>();

    public int stand;
    public int walk;

    public int run;
    public int walkR180;
    public int walkRRight;
    public int walkRLeft;
    public int standRRight;
    public int standRLeft;
}
