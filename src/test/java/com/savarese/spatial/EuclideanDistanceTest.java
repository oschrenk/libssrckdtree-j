/*
 * Copyright 2010 Savarese Software Research Corporation
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

import junit.framework.TestCase;

/**
 * EuclideanDistanceTest tests the EuclideanDistance class.
 */
public class EuclideanDistanceTest extends TestCase {

	/**
	 * Computes the square of the distance and distance between
	 * assorted <code>{@literal GenericPoint}</code> instances,
	 * verifying the results equal the expected values.
	 */
	public void testDistance() {
		EuclideanDistance<Integer, Point<Integer>> d =
				new EuclideanDistance<Integer, Point<Integer>>();
		GenericPoint<Integer> from = new GenericPoint<Integer>(1);
		GenericPoint<Integer> to = new GenericPoint<Integer>(1);

		from.setCoord(0, 1);
		to.setCoord(0, 1);

		// 1-D
		assertEquals(d.distanceSquared(from, to), 0.0);
		assertEquals(d.distance(from, to), 0.0);

		to.setCoord(0, 2);

		assertEquals(d.distanceSquared(from, to), 1.0);
		assertEquals(d.distance(from, to), 1.0);

		from.setCoord(0, 48);
		to.setCoord(0, 52);

		assertEquals(d.distanceSquared(from, to), 16.0);
		assertEquals(d.distance(from, to), 4.0);

		from.setCoord(0, 4);
		to.setCoord(0, 1);

		assertEquals(d.distanceSquared(from, to), 9.0);
		assertEquals(d.distance(from, to), 3.0);

		// 2-D
		from = new GenericPoint<Integer>(1, 1);
		to = new GenericPoint<Integer>(1, 1);

		assertEquals(d.distanceSquared(from, to), 0.0);
		assertEquals(d.distance(from, to), 0.0);

		to = new GenericPoint<Integer>(2, 2);

		assertEquals(d.distanceSquared(from, to), 2.0);
		assertEquals(d.distance(from, to), StrictMath.sqrt(2));

		from = new GenericPoint<Integer>(83, 9451);
		to = new GenericPoint<Integer>(4382, 2383);

		assertEquals(d.distanceSquared(from, to), 68438025.0);
		assertEquals(d.distance(from, to), StrictMath.sqrt(68438025));

		// 3-D
		from = new GenericPoint<Integer>(1, 1, 1);
		to = new GenericPoint<Integer>(1, 1, 1);

		assertEquals(d.distanceSquared(from, to), 0.0);
		assertEquals(d.distance(from, to), 0.0);

		to = new GenericPoint<Integer>(2, 2, 2);

		assertEquals(d.distanceSquared(from, to), 3.0);
		assertEquals(d.distance(from, to), StrictMath.sqrt(3));

		from = new GenericPoint<Integer>(9, 0, 4);
		to = new GenericPoint<Integer>(100, 32, 0);

		assertEquals(d.distanceSquared(from, to), 9321.0);
		assertEquals(d.distance(from, to), StrictMath.sqrt(9321));

		// 4-D
		from = new GenericPoint<Integer>(1, 1, 1, 1);
		to = new GenericPoint<Integer>(1, 1, 1, 1);

		assertEquals(d.distanceSquared(from, to), 0.0);
		assertEquals(d.distance(from, to), 0.0);

		to = new GenericPoint<Integer>(2, 2, 2, 2);

		assertEquals(d.distanceSquared(from, to), 4.0);
		assertEquals(d.distance(from, to), 2.0);

	}

}
