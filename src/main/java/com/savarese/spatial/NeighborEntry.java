package com.savarese.spatial;

import java.util.Map;

public class NeighborEntry<Coord extends Number & Comparable<? super Coord>, P extends Point<Coord>, V>
		implements Entry<Coord, P, V>, Comparable<Entry<Coord, P, V>> {
	double distanceSquared;
	Map.Entry<P, V> neighbor;

	NeighborEntry(double distanceSquared, Map.Entry<P, V> neighbor) {
		this.distanceSquared = distanceSquared;
		this.neighbor = neighbor;
	}

	public double getDistance() {
		return StrictMath.sqrt(distanceSquared);
	}

	public double getDistanceSquared() {
		return distanceSquared;
	}

	public Map.Entry<P, V> getNeighbor() {
		return neighbor;
	}

	public int compareTo(Entry<Coord, P, V> obj) {
		final double d = obj.getDistanceSquared();

		if (distanceSquared < d)
			return -1;
		else if (distanceSquared > d)
			return 1;

		return 0;
	}
}