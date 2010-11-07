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

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * NearestNeighbors implements an algorithm for finding the k-nearest
 * neighbors to a query point within the set of points contained by a
 * {@link KDTree} instance. The algorithm can be specialized with a custom
 * distanceMetric-finding function by passing a {@link Distance} instance to its
 * constructor.
 */
public class NearestNeighbors<Coord extends Number & Comparable<? super Coord>, P extends Point<Coord>, V> {

	private boolean omitQueryPoint;
	private int numberNeighbors;
	private double minimumDistance;
	private Distance<Coord, P> distanceMetric;
	private PriorityQueue<Entry<Coord, P, V>> priorityQueue;
	private P queryPoint;

	/**
	 * Constructs a new NearestNeighbors instance, using the specified
	 * distanceMetric-finding functor to calculate distances during searches.
	 * 
	 * @param distanceMetric
	 *            A distanceMetric-finding functor implementing
	 *            the {@link Distance} interface.
	 */
	public NearestNeighbors(Distance<Coord, P> distance) {
		this.distanceMetric = distance;
	}

	/**
	 * Constructs a NearestNeighbors instance using a {@link EuclideanDistance}
	 * instance to calculate distances between points.
	 */
	public NearestNeighbors() {
		this(new EuclideanDistance<Coord, P>());
	}

	/**
	 * Sets the distanceMetric-finding functor used to calculate distances
	 * during
	 * searches.
	 * 
	 * @param distanceMetric
	 *            The distanceMetric-finding functor to use for distanceMetric
	 *            calculations.
	 */
	public void setDistance(Distance<Coord, P> distanceMetric) {
		this.distanceMetric = distanceMetric;
	}

	private void find(KDTree<Coord, P, V>.KDNode node) {
		if (node == null)
			return;

		final int discriminator = node.discriminator;
		final P point = node.getKey();
		double d2 = distanceMetric.distanceSquared(queryPoint, point);

		if (d2 < minimumDistance && (d2 != 0.0 || !omitQueryPoint)) {
			if (priorityQueue.size() == numberNeighbors) {
				priorityQueue.poll();
				priorityQueue.add(new NeighborEntry<Coord, P, V>(d2, node));
				minimumDistance = priorityQueue.peek().getDistanceSquared();
			} else {
				priorityQueue.add(new NeighborEntry<Coord, P, V>(d2, node));
				if (priorityQueue.size() == numberNeighbors) {
					minimumDistance = priorityQueue.peek().getDistanceSquared();
				}
			}
		}

		double dp =
				queryPoint.getCoord(discriminator).doubleValue() -
						point.getCoord(discriminator).doubleValue();

		d2 = dp * dp;

		if (dp < 0) {
			find(node.low);
			if (d2 < minimumDistance) {
				find(node.high);
			}
		} else {
			find(node.high);
			if (d2 < minimumDistance) {
				find(node.low);
			}
		}
	}

	/**
	 * Finds the k-nearest neighbors to a query point withina KDTree instance.
	 * The neighbors are returned as an array of {@link Entry} instances, sorted
	 * from nearest to farthest.
	 * 
	 * @param tree
	 *            The KDTree to search.
	 * @param queryPoint
	 *            The query point.
	 * @param numNeighbors
	 *            The number of nearest neighbors to find. This should
	 *            be a positive value. Non-positive values result in no
	 *            neighbors
	 *            being found.
	 * @param omitQueryPoint
	 *            If true, point-value mappings at a distanceMetric of
	 *            zero are omitted from the result. If false, mappings at a
	 *            distanceMetric of zero are included.
	 * @return An array containing the nearest neighbors and their distances
	 *         sorted by least distanceMetric to greatest distanceMetric. If no
	 *         neighbors
	 *         are found, the array will have a length of zero.
	 */
	public Entry<Coord, P, V>[] get(KDTree<Coord, P, V> tree,
								P queryPoint,
								int numNeighbors,
								boolean omitQueryPoint) {
		this.omitQueryPoint = omitQueryPoint;
		numberNeighbors = numNeighbors;
		this.queryPoint = queryPoint;
		minimumDistance = Double.POSITIVE_INFINITY;

		priorityQueue = new PriorityQueue<Entry<Coord, P, V>>(numNeighbors,
													new EntryComparator());

		if (numNeighbors > 0) {
			find(tree.root);
		}

		Entry<Coord, P, V>[] neighbors = new Entry[priorityQueue.size()];

		priorityQueue.toArray(neighbors);
		Arrays.sort(neighbors);

		priorityQueue = null;
		queryPoint = null;

		return neighbors;
	}

	/**
	 * Same as {@link #get get(tree, queryPoint, numNeighbors, true)}.
	 */
	public Entry<Coord, P, V>[]
			get(KDTree<Coord, P, V> tree, P queryPoint, int numNeighbors) {
		return get(tree, queryPoint, numNeighbors, true);
	}
}
