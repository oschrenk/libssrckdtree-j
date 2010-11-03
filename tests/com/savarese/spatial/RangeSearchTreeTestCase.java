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

import java.util.*;

import junit.framework.*;

/**
 * RangeSearchTreeTestCase tests the KDTree class.
 */
public abstract class RangeSearchTreeTestCase<Coord extends Number & Comparable<? super Coord>>
  extends TestCase
{

  private Set<GenericPoint<Coord>> __points;
  protected RangeSearchTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>>
    _tree_;

  /**
   * Creates a new RangeSearchTree instance for testing.
   *
   * @return A new RangeSearchTree instance for testing.
   */
  protected abstract
    RangeSearchTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>>
               _newTreeFixture_();

  /**
   * Instantiates {@link #_tree_} to use as a fixture for tests.
   */
  protected void setUp() {
    _tree_ = _newTreeFixture_();
  }

  /**
   * Clears the tree fixture, removing all its contents so
   * that a subsequent test will not be affected by the previous one.
   */
  protected void tearDown() {
    _tree_.clear();
  }

  /**
   * Inserts a set of points into a map.
   *
   * @param map The map to fill with points.
   */
  protected <M extends Map<GenericPoint<Coord>, GenericPoint<Coord>>>
    void _fillMap_(M map)
  {
    for(GenericPoint<Coord> point : __points)
      map.put(point, point);
  }

  public RangeSearchTreeTestCase() {
    __points = new HashSet<GenericPoint<Coord>>();

    Random random = new Random();

    for(int i = 0; i < getNumPoints(); ++i) {
      int x = getMinCoord().intValue() +
        random.nextInt(getMaxCoord().intValue() - getMinCoord().intValue());
      int y = getMinCoord().intValue() +
        random.nextInt(getMaxCoord().intValue() - getMinCoord().intValue());
      __points.add(new GenericPoint<Coord>(newCoord(x), newCoord(y)));
    }
  }

  public abstract Coord newCoord(int val);

  public abstract Coord getMaxCoord();

  public abstract Coord getMinCoord();

  public abstract int getNumPoints();

  public void testSize() {
    _fillMap_(_tree_);
    assertEquals(__points.size(), _tree_.size());
  }

  public void testClear() {
    _fillMap_(_tree_);

    assertTrue(_tree_.size() > 0);

    _tree_.clear();

    assertTrue(_tree_.isEmpty());
    assertEquals(0, _tree_.size());
  }

  public void testContainsKey() {
    _fillMap_(_tree_);

    for(GenericPoint<Coord> point : __points)
      assertTrue(_tree_.containsKey(point));
  }

  public void testContainsValue() {
    _fillMap_(_tree_);

    int i = 5;
    for(GenericPoint<Coord> point : __points) {
      assertTrue(_tree_.containsValue(point));
      if(--i <= 0)
        break;
    }

    assertFalse(_tree_.containsValue(null));
    _tree_.put(new GenericPoint<Coord>(newCoord(0), newCoord(0)), null);
    assertTrue(_tree_.containsValue(null));
  }

  public void testGet() {
    _fillMap_(_tree_);

    for(GenericPoint<Coord> point : __points) {
      GenericPoint<Coord> value = _tree_.get(point);

      assertNotNull(value);
      assertEquals(point, value);
    }
  }

  public void testRemove() {
    _fillMap_(_tree_);

    for(GenericPoint<Coord> point : __points)
      assertEquals(point, _tree_.remove(point));

    assertTrue(_tree_.isEmpty());
    assertEquals(0, _tree_.size());

    for(GenericPoint<Coord> point : __points)
      assertNull(_tree_.remove(point));
  }

  public void testPutAll() {
    HashMap<GenericPoint<Coord>, GenericPoint<Coord>> map =
      new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

    _fillMap_(map);
    _tree_.putAll(map);

    for(GenericPoint<Coord> point : __points) {
      GenericPoint<Coord> value = _tree_.get(point);

      assertNotNull(value);
      assertEquals(point, value);
    }
  }

  public void testEntrySet() {
    _fillMap_(_tree_);

    Set<Map.Entry<GenericPoint<Coord>,GenericPoint<Coord>>> set = 
      _tree_.entrySet();

    assertEquals(_tree_.size(), set.size());

    int size = 0;
    for(Map.Entry<GenericPoint<Coord>,GenericPoint<Coord>> e : set) {
      ++size;
      assertEquals(_tree_.get(e.getKey()), e.getValue());
    }

    assertEquals(_tree_.size(), size);
  }

  public void testKeySet() {
    _fillMap_(_tree_);

    Set<GenericPoint<Coord>> set = _tree_.keySet();

    assertEquals(_tree_.size(), set.size());

    int size = 0;
    for(GenericPoint<Coord> key : set) {
      ++size;
      assertTrue(_tree_.containsKey(key));
    }

    assertEquals(_tree_.size(), size);
  }

  public void testValues() {
    HashMap<GenericPoint<Coord>, GenericPoint<Coord>> map =
      new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

    int i = 10;
    for(GenericPoint<Coord> p : __points) {
      map.put(p, p);
      if(--i <= 0)
        break;
    }

    _tree_.putAll(map);

    assertTrue(_tree_.values().containsAll(map.values()));
    assertTrue(map.values().containsAll(_tree_.values()));
  }

  public void testIterator() {
    _fillMap_(_tree_);

    Iterator<Map.Entry<GenericPoint<Coord>,GenericPoint<Coord>>> range =
      _tree_.iterator(new GenericPoint<Coord>(getMinCoord(), getMinCoord()),
                      new GenericPoint<Coord>(getMaxCoord(), getMaxCoord()));
    int size = 0;

    while(range.hasNext()) {
      Map.Entry<GenericPoint<Coord>,GenericPoint<Coord>> e = range.next();
      assertEquals(_tree_.get(e.getKey()), e.getValue());
      ++size;
    }

    assertEquals(_tree_.size(), size);
  }

  public void testEquals() {
    _fillMap_(_tree_);

    Map<GenericPoint<Coord>, GenericPoint<Coord>> map =
      new HashMap<GenericPoint<Coord>, GenericPoint<Coord>>();

    _fillMap_(map);

    assertEquals(map, _tree_);
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
      (KDTree<Coord, GenericPoint<Coord>, GenericPoint<Coord>>)_tree_;

    _fillMap_(tree);

    ArrayList<GenericPoint<Coord>> sortedPoints = new ArrayList(__points);

    Collections.sort(sortedPoints, new Comparator<GenericPoint<Coord>>() {
        public int compare(GenericPoint<Coord> o1, GenericPoint<Coord> o2) {
          double d1 = d.distance2(query, o1);
          double d2 = d.distance2(query, o2);
          if(d1 < d2)
            return -1;
          else if(d1 > d2)
            return 1;
          return 0;
        }
        public boolean equals(Object obj) {
          return (obj == this);
        }
      });

    NearestNeighbors.Entry<Coord,GenericPoint<Coord>,GenericPoint<Coord>>[] n;

    for(int i = 1; i < 11; ++i) {
      n = nn.get(tree, query, i, false);

      assertNotNull(n);
      assertEquals(i, n.length);

      for(int j = 0; j < n.length; ++j) {
        assertEquals(sortedPoints.get(j), n[j].getNeighbor().getKey());
      }
    }

    for(int i = 1; i < 11; ++i) {
      int j = 0;
      n = nn.get(tree, query, i);

      assertNotNull(n);
      assertEquals(i, n.length);

      if(sortedPoints.get(0).equals(query)) {
        ++j;
      }

      for(int k = 0; k < n.length; ++k, ++j) {
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
                 d.distance2(q, n[0].getNeighbor().getKey()) < 2);

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

    for(int i = 0; i < data.length; ++i) {
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
