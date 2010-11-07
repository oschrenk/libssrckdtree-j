/*
 * Copyright 2005 Daniel F. Savarese
 * Copyright 2006-2009 Savarese Software Research Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.savarese.com/software/ApacheLicense-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.savarese.spatial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

/**
 * RangeSearchTreeTestCase tests the KDTree class.
 */
public abstract class RangeSearchTreeTestCase<Coord extends Number & Comparable<? super Coord>>
		extends TestCase {

	private Set<GenericPoint<Coord>> points;
	protected RangeSearchTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>> tree;

	/**
	 * Creates a new RangeSearchTree instance for testing.
	 * 
	 * @return A new RangeSearchTree instance for testing.
	 */
	protected abstract RangeSearchTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>>
				newTreeFixture();

	/**
	 * Instantiates {@link #tree} to use as a fixture for tests.
	 */
	protected void setUp() {
		tree = newTreeFixture();
	}

	/**
	 * Clears the tree fixture, removing all its contents so
	 * that a subsequent test will not be affected by the previous one.
	 */
	protected void tearDown() {
		tree.clear();
	}

	/**
	 * Inserts a set of points into a map.
	 * 
	 * @param map
	 *            The map to fill with points.
	 */
	protected <M extends Map<GenericPoint<Coord>, GenericPoint<Coord>>>
			void fillMap(M map) {
		for (GenericPoint<Coord> point : points)
			map.put(point, point);
	}

	public RangeSearchTreeTestCase() {
		points = new HashSet<GenericPoint<Coord>>();

		Random random = new Random();

		for (int i = 0; i < getNumPoints(); ++i) {
			int x = getMinCoord().intValue() +
					random.nextInt(getMaxCoord().intValue() - getMinCoord().intValue());
			int y = getMinCoord().intValue() +
					random.nextInt(getMaxCoord().intValue() - getMinCoord().intValue());
			points.add(new GenericPoint<Coord>(newCoord(x), newCoord(y)));
		}
	}

	public abstract Coord newCoord(int val);

	public abstract Coord getMaxCoord();

	public abstract Coord getMinCoord();

	public abstract int getNumPoints();

	public void testSize() {
		fillMap(tree);
		assertEquals(points.size(), tree.size());
	}

	public void testClear() {
		fillMap(tree);

		assertTrue(tree.size() > 0);

		tree.clear();

		assertTrue(tree.isEmpty());
		assertEquals(0, tree.size());
	}

	public void testContainsKey() {
		fillMap(tree);

		for (GenericPoint<Coord> point : points)
			assertTrue(tree.containsKey(point));
	}

	public void testContainsValue() {
		fillMap(tree);

		int i = 5;
		for (GenericPoint<Coord> point : points) {
			assertTrue(tree.containsValue(point));
			if (--i <= 0)
				break;
		}

		assertFalse(tree.containsValue(null));
		tree.put(new GenericPoint<Coord>(newCoord(0), newCoord(0)), null);
		assertTrue(tree.containsValue(null));
	}

	public void testGet() {
		fillMap(tree);

		for (GenericPoint<Coord> point : points) {
			GenericPoint<Coord> value = tree.get(point);

			assertNotNull(value);
			assertEquals(point, value);
		}
	}

	public void testRemove() {
		fillMap(tree);

		for (GenericPoint<Coord> point : points)
			assertEquals(point, tree.remove(point));

		assertTrue(tree.isEmpty());
		assertEquals(0, tree.size());

		for (GenericPoint<Coord> point : points)
			assertNull(tree.remove(point));
	}

	public void testPutAll() {
		HashMap<GenericPoint<Coord>, GenericPoint<Coord>> map =
				new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

		fillMap(map);
		tree.putAll(map);

		for (GenericPoint<Coord> point : points) {
			GenericPoint<Coord> value = tree.get(point);

			assertNotNull(value);
			assertEquals(point, value);
		}
	}

	public void testEntrySet() {
		fillMap(tree);

		Set<Map.Entry<GenericPoint<Coord>, GenericPoint<Coord>>> set =
				tree.entrySet();

		assertEquals(tree.size(), set.size());

		int size = 0;
		for (Map.Entry<GenericPoint<Coord>, GenericPoint<Coord>> e : set) {
			++size;
			assertEquals(tree.get(e.getKey()), e.getValue());
		}

		assertEquals(tree.size(), size);
	}

	public void testKeySet() {
		fillMap(tree);

		Set<GenericPoint<Coord>> set = tree.keySet();

		assertEquals(tree.size(), set.size());

		int size = 0;
		for (GenericPoint<Coord> key : set) {
			++size;
			assertTrue(tree.containsKey(key));
		}

		assertEquals(tree.size(), size);
	}

	public void testValues() {
		HashMap<GenericPoint<Coord>, GenericPoint<Coord>> map =
				new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

		int i = 10;
		for (GenericPoint<Coord> p : points) {
			map.put(p, p);
			if (--i <= 0)
				break;
		}

		tree.putAll(map);

		assertTrue(tree.values().containsAll(map.values()));
		assertTrue(map.values().containsAll(tree.values()));
	}

	public void testIterator() {
		fillMap(tree);

		Iterator<Map.Entry<GenericPoint<Coord>, GenericPoint<Coord>>> range =
				tree.iterator(new GenericPoint<Coord>(getMinCoord(), getMinCoord()),
						new GenericPoint<Coord>(getMaxCoord(), getMaxCoord()));
		int size = 0;

		while (range.hasNext()) {
			Map.Entry<GenericPoint<Coord>, GenericPoint<Coord>> e = range.next();
			assertEquals(tree.get(e.getKey()), e.getValue());
			++size;
		}

		assertEquals(tree.size(), size);
	}

	public void testEquals() {
		fillMap(tree);

		Map<GenericPoint<Coord>, GenericPoint<Coord>> map =
				new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

		fillMap(map);

		assertEquals(map, tree);
	}

	public void testNearestNeighbors() {
		NearestNeighbors<Coord, GenericPoint<Coord>, GenericPoint<Coord>> nn =
				new NearestNeighbors<Coord, GenericPoint<Coord>, GenericPoint<Coord>>();
		final GenericPoint<Coord> query =
				new GenericPoint<Coord>(newCoord(getMaxCoord().intValue() / 2),
								newCoord(getMaxCoord().intValue() / 2));
		final EuclideanDistance<Coord, GenericPoint<Coord>> d =
				new EuclideanDistance<Coord, GenericPoint<Coord>>();

		KDTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>> tree =
				(KDTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>>) this.tree;

		fillMap(tree);

		ArrayList<GenericPoint<Coord>> sortedPoints = new ArrayList<GenericPoint<Coord>>(points);

		Collections.sort(sortedPoints, new Comparator<GenericPoint<Coord>>() {
			public int compare(GenericPoint<Coord> o1, GenericPoint<Coord> o2) {
				double d1 = d.distanceSquared(query, o1);
				double d2 = d.distanceSquared(query, o2);
				if (d1 < d2)
					return -1;
				else if (d1 > d2)
					return 1;
				return 0;
			}

			public boolean equals(Object obj) {
				return (obj == this);
			}
		});

		Entry<Coord, GenericPoint<Coord>, GenericPoint<Coord>>[] n;

		for (int i = 1; i < 11; ++i) {
			n = nn.get(tree, query, i, false);

			assertNotNull(n);
			assertEquals(i, n.length);

			for (int j = 0; j < n.length; ++j) {
				assertEquals(sortedPoints.get(j), n[j].getNeighbor().getKey());
			}
		}

		for (int i = 1; i < 11; ++i) {
			int j = 0;
			n = nn.get(tree, query, i);

			assertNotNull(n);
			assertEquals(i, n.length);

			if (sortedPoints.get(0).equals(query)) {
				++j;
			}

			for (int k = 0; k < n.length; ++k, ++j) {
				assertEquals(sortedPoints.get(j), n[k].getNeighbor().getKey());
			}
		}

		final GenericPoint<Coord> q =
				new GenericPoint<Coord>(newCoord(1), newCoord(1));
		final GenericPoint<Coord> p =
				new GenericPoint<Coord>(newCoord(2), newCoord(2));
		tree.put(p, p);

		n = nn.get(tree, q, 1);

		assertNotNull(n);
		assertEquals(1, n.length);

		assertTrue(n[0].getNeighbor().getKey().equals(p) ||
					d.distanceSquared(q, n[0].getNeighbor().getKey()) < 2);

		n = nn.get(tree, p, 1, false);

		assertNotNull(n);
		assertEquals(1, n.length);
		assertEquals(p, n[0].getNeighbor().getKey());

		// This should be a separate regression test.  It catches the bug
		// whereby nearest neighbors tests for k = 1 would produce an
		// incorrect result when k > 2 would be correct.
		tree.clear();

		GenericPoint data[] = {
				new GenericPoint<Coord>(newCoord(0), newCoord(0)),
				new GenericPoint<Coord>(newCoord(3), newCoord(1)),
				new GenericPoint<Coord>(newCoord(4), newCoord(2)),
				new GenericPoint<Coord>(newCoord(1), newCoord(1)),
				new GenericPoint<Coord>(newCoord(0), newCoord(1)),
				new GenericPoint<Coord>(newCoord(5), newCoord(5))
		};

		for (int i = 0; i < data.length; ++i) {
			tree.put(data[i], data[i]);
		}

		final GenericPoint<Coord> q2 =
				new GenericPoint<Coord>(newCoord(-1), newCoord(-1));
		final GenericPoint<Coord> p2 =
				new GenericPoint<Coord>(newCoord(0), newCoord(0));

		n = nn.get(tree, q2, 2);

		assertNotNull(n);
		assertEquals(2, n.length);
		assertEquals(p2, n[0].getNeighbor().getKey());

		n = nn.get(tree, q2, 1);

		assertNotNull(n);
		assertEquals(1, n.length);
		assertEquals(p2, n[0].getNeighbor().getKey());
	}
}
