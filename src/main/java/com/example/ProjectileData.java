package com.example;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//delta2 is the smaller delta variable. This appears to not be involved with calculations for projectiles.
//Instead delta1 is the larger delta in position which would apply as per the Chebyshev distance.
//TODO remove comments to allow serializing delta2 values later if needed.
public class ProjectileData {

    public Set<Integer> ids = new HashSet<>();

    private int startHeight, endHeight, delay, speed, slope, radius, delta1/*, delta2*/;

    public ProjectileData(int id, int startHeight, int endHeight, int delay, int speed, int slope, int radius, int delta1, int delta2) {
//        this.id = id;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.delay = delay;
        this.speed = speed;
        this.slope = slope;
        this.radius = radius;
        this.delta1 = delta1;
//        this.delta2 = delta2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectileData that = (ProjectileData) o;
        return startHeight == that.startHeight && endHeight == that.endHeight && delay == that.delay && speed == that.speed && slope == that.slope && radius == that.radius && delta1 == that.delta1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startHeight, endHeight, delay, speed, slope, radius, delta1);
    }
}
