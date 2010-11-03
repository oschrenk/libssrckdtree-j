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

import junit.framework.*;

/**
 * GenericPointTest tests the GenericPoint class.
 */
public class GenericPointTest extends TestCase {

  /**
   * Instantiates several <code>{@literal GenericPoint}</code>
   * instances in succession and verifies that all getter methods
   * return results equivalent to the constructor parameters.
   */
  public void testConstructor() {
    GenericPoint<Integer> point = new GenericPoint<Integer>(8, 1);

    assertEquals(point.getDimensions(),  2);
    assertEquals(point.getCoord(0).intValue(), 8);
    assertEquals(point.getCoord(1).intValue(), 1);

    point = new GenericPoint<Integer>(7, -5, -1);

    assertEquals(point.getDimensions(),  3);
    assertEquals(point.getCoord(0).intValue(), 7);
    assertEquals(point.getCoord(1).intValue(), -5);
    assertEquals(point.getCoord(2).intValue(), -1);
  }

  /**
   * Instantiates several <code>{@literal GenericPoint}</code> instances
   * and verifies that invoking toString() produces an expected value.
   */
  public void testToString() {
    GenericPoint<Integer> point = new GenericPoint<Integer>(10, -4);

    assertEquals("[ 10, -4 ]", point.toString());

    point = new GenericPoint<Integer>(-3, 22, 91);

    assertEquals("[ -3, 22, 91 ]", point.toString());
  }

  /**
   * Instantiates several <code>{@literal GenericPoint}</code>
   * instances and verifies that they are equal to their clones and
   * that their hash codes are the same.
   */
  public void testClone() {
    GenericPoint<Integer> point = new GenericPoint<Integer>(-8, 11);

    assertEquals(point, point.clone());

    point = new GenericPoint<Integer>(88, -13, 42);

    assertEquals(point, point.clone());
    assertEquals(point.hashCode(), point.clone().hashCode());
  }

  /**
   * Tests the equals method.
   */
  public void testEquals() {
    GenericPoint<Integer> point = new GenericPoint<Integer>(2, 3);

    assertFalse(point.equals(null));
    assertEquals(point, point);
  }

}
