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

/**
 * KDTreeTest tests the KDTree class.
 */
public class KDTreeTest extends RangeSearchTreeTestCase<Integer> {

  protected
    RangeSearchTree<Integer, GenericPoint<Integer>, GenericPoint<Integer>>
    _newTreeFixture_()
  {
    return new KDTree<Integer, GenericPoint<Integer>, GenericPoint<Integer>>();
  }

  public Integer newCoord(int val) {
    return val;
  }

  public Integer getMaxCoord() {
    return 16384;
  }

  public Integer getMinCoord() {
    return 0;
  }

  public int getNumPoints() {
    return 16384;
  }

}
