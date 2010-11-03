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

/**
 * OptimiizedKDTreeTest tests the KDTree class after optimization.
 */
public class OptimizedKDTreeTest extends KDTreeTest {

  protected <M extends Map<GenericPoint<Integer>, GenericPoint<Integer>>>
    void _fillMap_(M map)
  {
    super._fillMap_(map);

    Object obj = map;

    if(obj instanceof KDTree<?,?,?>)
      ((KDTree)obj).optimize();
  }
}
