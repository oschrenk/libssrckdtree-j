package com.savarese.spatial;

import java.util.Comparator;

class EntryComparator<Coord extends Number & Comparable<? super Coord>, P extends Point<Coord>, V>
		implements Comparator<Entry<Coord, P, V>> {
	// Invert relationship so priority queue keeps highest on top.
	public int compare(Entry<Coord, P, V> n1, Entry<Coord, P, V> n2) {
		final double d1 = n1.getDistanceSquared();
		final double d2 = n2.getDistanceSquared();

		if (d1 < d2)
			return 1;
		else if (d1 > d2)
			return -1;

		return 0;
	}

	public boolean equals(Object obj) {
		return (obj != null && obj == this);
	}
}