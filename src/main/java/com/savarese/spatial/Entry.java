package com.savarese.spatial;

import java.util.Map;

 /**
   * The Entry interface makes accessible the results of a
   * {@link NearestNeighbors} search.  An Entry exposes both the
   * point-value mapping and its distance from the query point.
   */
  public interface Entry<Coord extends Number & Comparable<? super Coord>,
                         P extends Point<Coord>, V>
  {
    /**
     * Returns the distance from result to the query point.  This
     * will usually be implemented by dynamically taking the square root
     * of {@link #getDistance2}.  Therefore, repeated calls may be
     * expensive.
     *
     * @return The distance from result to the query point.
     */
    public double getDistance();

    /**
     * Returns the square of the distance from result to the query point.
     * This will usually be implemented as returning a cached value used
     * during the nearest neighbors search.
     *
     * @return The square of the distance from result to the query point.
     */
    public double getDistanceSquared();

    /**
     * Returns the point-value mapping stored in this query result.
     *
     * @return The point-value mapping stored in this query result.
     */
    public Map.Entry<P,V> getNeighbor();
  }