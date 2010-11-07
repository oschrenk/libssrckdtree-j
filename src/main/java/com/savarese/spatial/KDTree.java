/*
 * Copyright 2001-2005 Daniel F. Savarese
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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

// All the view classes are inefficient for anything other than iteration.
/**
 * <p>A k-d tree divides a k-dimensional space relative to the points it
 * contains by storing them in a binary tree, discriminating by a
 * different dimension at each level of the tree. It allows efficient
 * point data retrieval (<em>O(lg(n))</em>) and range searching.</p>
 * 
 * <p>KDTree conforms to the java.util.Map interface except that
 * Iterator.remove is not supported by the returned views.</p>
 */
public class KDTree<Coord extends Comparable<? super Coord>, P extends Point<Coord>, V>
		implements RangeSearchTree<Coord, P, V> {
	final class KDNode implements Map.Entry<P, V> {
		int discriminator;
		P point;
		V value;
		KDNode low, high;

		KDNode(int discriminator, P point, V value) {
			this.point = point;
			this.value = value;
			low = high = null;
			this.discriminator = discriminator;
		}

		public boolean equals(Object o) {
			KDNode node = (KDNode) o;

			if (node == this)
				return true;

			return ((getKey() == null ?
					node.getKey() == null : getKey().equals(node.getKey())) && (getValue() == null ?
					node.getValue() == null : getValue().equals(node.getValue())));
		}

		public P getKey() {
			return point;
		}

		public V getValue() {
			return value;
		}

		// Only call if the node is in the tree.
		public V setValue(V value) {
			V old = value;
			hashCode -= hashCode();
			this.value = value;
			hashCode += hashCode();
			return old;
		}

		public int hashCode() {
			return ((getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode()));
		}
	}

	final class MapEntryIterator implements Iterator<Map.Entry<P, V>> {
		LinkedList<KDNode> stack;
		KDNode nextKDNode;
		P lowerPoint, upperPoint;

		MapEntryIterator(P lowerPoint, P upperPoint) {
			stack = new LinkedList<KDNode>();
			this.lowerPoint = lowerPoint;
			this.upperPoint = upperPoint;
			nextKDNode = null;

			if (root != null)
				stack.addLast(root);
			next();
		}

		MapEntryIterator() {
			this(null, null);
		}

		public boolean hasNext() {
			return (nextKDNode != null);
		}

		public Map.Entry<P, V> next() {
			KDNode old = nextKDNode;

			while (!stack.isEmpty()) {
				KDNode node = stack.removeLast();
				int discriminator = node.discriminator;

				if ((upperPoint == null ||
						node.point.getCoord(discriminator).compareTo(
								upperPoint.getCoord(discriminator)) <= 0) && node.high != null)
					stack.addLast(node.high);

				if ((lowerPoint == null ||
						node.point.getCoord(discriminator).compareTo(
								lowerPoint.getCoord(discriminator)) > 0) && node.low != null)
					stack.addLast(node.low);

				if (isInRange(node.point, lowerPoint, upperPoint)) {
					nextKDNode = node;
					return old;
				}
			}

			nextKDNode = null;

			return old;
		}

		// This violates the contract for entrySet, but we can't support
		// in a reasonable fashion the removal of mappings through the iterator.
		// Java iterators require a hasNext() function, which forces the stack
		// to reflect a future search state, making impossible to adjust the current
		// stack after a removal.  Implementation alternatives are all too
		// expensive.  Yet another reason to favor the C++ implementation...
		public void remove()
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	final class KeyIterator implements Iterator<P> {
		MapEntryIterator iterator;

		KeyIterator(MapEntryIterator it) {
			iterator = it;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public P next() {
			Map.Entry<P, V> next = iterator.next();
			return (next == null ? null : next.getKey());
		}

		public void remove()
				throws UnsupportedOperationException {
			iterator.remove();
		}
	}

	final class ValueIterator implements Iterator<V> {
		MapEntryIterator iterator;

		ValueIterator(MapEntryIterator it) {
			iterator = it;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public V next() {
			Map.Entry<P, V> next = iterator.next();
			return (next == null ? null : next.getValue());
		}

		public void remove()
				throws UnsupportedOperationException {
			iterator.remove();
		}
	}

	abstract class CollectionView<E> implements Collection<E> {

		public boolean add(E o)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends E> c)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			KDTree.this.clear();
		}

		public boolean containsAll(Collection<?> c) {
			for (Object o : c) {
				if (!contains(o))
					return false;
			}
			return true;
		}

		public int hashCode() {
			return KDTree.this.hashCode();
		}

		public boolean isEmpty() {
			return KDTree.this.isEmpty();
		}

		public int size() {
			return KDTree.this.size();
		}

		public Object[] toArray() {
			Object[] obja = new Object[size()];
			int i = 0;

			for (E e : this) {
				obja[i] = e;
				++i;
			}

			return obja;
		}

		public <T> T[] toArray(T[] a) {
			Object[] array = a;

			if (array.length < size())
				array = a =
						(T[]) Array.newInstance(a.getClass().getComponentType(), size());

			if (array.length > size())
				array[size()] = null;

			int i = 0;
			for (E e : this) {
				array[i] = e;
				++i;
			}

			return a;
		}
	}

	abstract class SetView<E> extends CollectionView<E> implements Set<E> {
		public boolean equals(Object o) {
			if (!(o instanceof Set))
				return false;

			if (o == this)
				return true;

			Set<?> set = (Set<?>) o;

			if (set.size() != size())
				return false;

			try {
				return containsAll(set);
			} catch (ClassCastException cce) {
				return false;
			}
		}
	}

	final class MapEntrySet extends SetView<Map.Entry<P, V>> {
		public boolean contains(Object o)
				throws ClassCastException, NullPointerException {
			Map.Entry<P, V> e = (Map.Entry<P, V>) o;
			KDNode node = getNode(e.getKey());

			if (node == null)
				return false;

			return e.getValue().equals(node.getValue());
		}

		public Iterator<Map.Entry<P, V>> iterator() {
			return new MapEntryIterator();
		}

		public boolean remove(Object o)
				throws ClassCastException {
			int size = size();
			Map.Entry<P, V> e = (Map.Entry<P, V>) o;

			KDTree.this.remove(e.getKey());

			return (size != size());
		}

		public boolean removeAll(Collection<?> c)
				throws ClassCastException {
			int size = size();

			for (Object o : c) {
				Map.Entry<P, V> e = (Map.Entry<P, V>) o;
				KDTree.this.remove(e.getKey());
			}

			return (size != size());
		}

		public boolean retainAll(Collection<?> c)
				throws ClassCastException {
			for (Object o : c) {
				if (contains(o)) {
					Collection<Map.Entry<P, V>> col = (Collection<Map.Entry<P, V>>) c;
					clear();
					for (Map.Entry<P, V> e : col)
						put(e.getKey(), e.getValue());
					return true;
				}
			}
			return false;
		}
	}

	final class KeySet extends SetView<P> {

		public boolean contains(Object o)
				throws ClassCastException, NullPointerException {
			return KDTree.this.containsKey(o);
		}

		public Iterator<P> iterator() {
			return new KeyIterator(new MapEntryIterator());
		}

		public boolean remove(Object o)
				throws ClassCastException {
			int size = size();
			KDTree.this.remove(o);
			return (size != size());
		}

		public boolean removeAll(Collection<?> c)
				throws ClassCastException {
			int size = size();

			for (Object o : c)
				KDTree.this.remove(o);

			return (size != size());
		}

		public boolean retainAll(Collection<?> c)
				throws ClassCastException {
			HashMap<P, V> map = new HashMap<P, V>();
			int size = size();

			for (Object o : c) {
				V val = get(o);

				if (val != null || contains(o))
					map.put((P) o, val);
			}

			clear();
			putAll(map);

			return (size != size());
		}
	}

	final class ValueCollection extends CollectionView<V> {

		public boolean contains(Object o)
				throws ClassCastException, NullPointerException {
			return KDTree.this.containsValue(o);
		}

		public Iterator<V> iterator() {
			return new ValueIterator(new MapEntryIterator());
		}

		public boolean remove(Object o)
				throws ClassCastException {
			KDNode node = findValue(root, o);

			if (node != null) {
				KDTree.this.remove(node.getKey());
				return true;
			}

			return false;
		}

		public boolean removeAll(Collection<?> c)
				throws ClassCastException {
			int size = size();

			for (Object o : c) {
				KDNode node = findValue(root, o);

				while (node != null) {
					KDTree.this.remove(o);
					node = findValue(root, o);
				}
			}

			return (size != size());
		}

		public boolean retainAll(Collection<?> c)
				throws ClassCastException {
			HashMap<P, V> map = new HashMap<P, V>();
			int size = size();

			for (Object o : c) {
				KDNode node = findValue(root, o);

				while (node != null) {
					map.put(node.getKey(), node.getValue());
					node = findValue(root, o);
				}
			}

			clear();
			putAll(map);

			return (size != size());
		}
	}

	int size, hashCode, dimensions;
	KDNode root;

	KDNode getNode(P point, KDNode[] parent) {
		int discriminator;
		KDNode node = root, current, last = null;
		Coord c1, c2;

		while (node != null) {
			discriminator = node.discriminator;
			c1 = point.getCoord(discriminator);
			c2 = node.point.getCoord(discriminator);
			current = node;

			if (c1.compareTo(c2) > 0)
				node = node.high;
			else if (c1.compareTo(c2) < 0)
				node = node.low;
			else if (node.point.equals(point)) {
				if (parent != null)
					parent[0] = last;
				return node;
			} else
				node = node.high;

			last = current;
		}

		if (parent != null)
			parent[0] = last;

		return null;
	}

	KDNode getNode(P point) {
		return getNode(point, null);
	}

	KDNode getMinimumNode(KDNode node, KDNode p, int discriminator,
						KDNode[] parent) {
		KDNode result;

		if (discriminator == node.discriminator) {
			if (node.low != null)
				return getMinimumNode(node.low, node, discriminator, parent);
			else
				result = node;
		} else {
			KDNode nlow = null, nhigh = null;
			KDNode[] plow = new KDTree.KDNode[1], phigh = new KDTree.KDNode[1];

			if (node.low != null)
				nlow = getMinimumNode(node.low, node, discriminator, plow);

			if (node.high != null)
				nhigh = getMinimumNode(node.high, node, discriminator, phigh);

			if (nlow != null && nhigh != null) {
				if (nlow.point.getCoord(discriminator).compareTo(nhigh.point.getCoord(discriminator)) < 0) {
					result = nlow;
					parent[0] = plow[0];
				} else {
					result = nhigh;
					parent[0] = phigh[0];
				}
			} else if (nlow != null) {
				result = nlow;
				parent[0] = plow[0];
			} else if (nhigh != null) {
				result = nhigh;
				parent[0] = phigh[0];
			} else
				result = node;
		}

		if (result == node)
			parent[0] = p;
		else if (node.point.getCoord(discriminator).compareTo(result.point.getCoord(discriminator)) < 0) {
			result = node;
			parent[0] = p;
		}

		return result;
	}

	KDNode recursiveRemoveNode(KDNode node) {
		int discriminator;

		if (node.low == null && node.high == null)
			return null;
		else
			discriminator = node.discriminator;

		if (node.high == null) {
			node.high = node.low;
			node.low = null;
		}

		KDNode[] parent = new KDTree.KDNode[1];
		KDNode newRoot =
				getMinimumNode(node.high, node, discriminator, parent);
		KDNode child = recursiveRemoveNode(newRoot);

		if (parent[0].low == newRoot)
			parent[0].low = child;
		else
			parent[0].high = child;

		newRoot.low = node.low;
		newRoot.high = node.high;
		newRoot.discriminator = node.discriminator;

		return newRoot;
	}

	KDNode findValue(KDNode node, Object value) {
		if (node == null || (value == null ? node.getValue() == null :
						value.equals(node.getValue())))
			return node;

		KDNode result;

		if ((result = findValue(node.low, value)) == null)
			result = findValue(node.high, value);

		return result;
	}

	boolean isInRange(P point, P lower, P upper) {
		Coord coordinate1, coordinate2 = null, coordinate3 = null;

		if (lower != null || upper != null) {
			int dimensions;
			dimensions = point.getDimensions();

			for (int i = 0; i < dimensions; ++i) {
				coordinate1 = point.getCoord(i);
				if (lower != null)
					coordinate2 = lower.getCoord(i);
				if (upper != null)
					coordinate3 = upper.getCoord(i);
				if ((coordinate2 != null && coordinate1.compareTo(coordinate2) < 0) ||
						(coordinate3 != null && coordinate1.compareTo(coordinate3) > 0))
					return false;
			}
		}

		return true;
	}

	/**
	 * Creates a two-dimensional KDTree.
	 */
	public KDTree() {
		this(2);
	}

	/**
	 * Creates a KDTree of the specified number of dimensions.
	 * 
	 * @param dimensions
	 *            The number of dimensions. Must be greater than 0.
	 */
	public KDTree(int dimensions) {
		assert (dimensions > 0);
		this.dimensions = dimensions;
		clear();
	}

	// Begin Map interface methods

	/**
	 * Removes all elements from the container, leaving it empty.
	 */
	public void clear() {
		root = null;
		size = hashCode = 0;
	}

	/**
	 * Returns true if the container contains a mapping for the specified key.
	 * 
	 * @param key
	 *            The point key to search for.
	 * @return true if the container contains a mapping for the specified key.
	 * @exception ClassCastException
	 *                if the key is not an instance of P.
	 */
	public boolean containsKey(Object key)
			throws ClassCastException {
		return (getNode((P) key) != null);
	}

	/**
	 * Returns true if the container contains a mapping with the specified
	 * value.
	 * Note: this is very inefficient for KDTrees because it requires searching
	 * the entire tree.
	 * 
	 * @param value
	 *            The value to search for.
	 * @return true If the container contains a mapping with the specified
	 *         value.
	 */
	public boolean containsValue(Object value) {
		return (findValue(root, value) != null);
	}

	/**
	 * Returns a Set view of the point to value mappings in the KDTree.
	 * Modifications to the resulting set will be reflected in the KDTree
	 * and vice versa, except that {@code Iterator.remove} is not supported.
	 * 
	 * @return A Set view of the point to value mappings in the KDTree.
	 */
	public Set<Map.Entry<P, V>> entrySet() {
		return new MapEntrySet();
	}

	/**
	 * Returns true if the object contains the same mappings, false if not.
	 * 
	 * @param o
	 *            The object to test for equality.
	 * @return true if the object contains the same mappings, false if not.
	 */
	public boolean equals(Object o)
			throws ClassCastException {
		if (!(o instanceof Map))
			return false;

		if (o == this)
			return true;

		Map map = (Map) o;

		return (entrySet().equals(map.entrySet()));
	}

	/**
	 * Retrieves the value at the given location.
	 * 
	 * @param point
	 *            The location from which to retrieve the value.
	 * @return The value at the given location, or null if no value is present.
	 * @exception ClassCastException
	 *                If the given point is not of the
	 *                expected type.
	 */
	public V get(Object point) throws ClassCastException {
		KDNode node = getNode((P) point);

		return (node == null ? null : node.getValue());
	}

	/**
	 * Returns the hash code value for this map.
	 * 
	 * @return The sum of the hash codes of all of the map entries.
	 */
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Returns true if the container has no elements, false if it
	 * contains one or more elements.
	 * 
	 * @return true if the container has no elements, false if it
	 *         contains one or more elements.
	 */
	public boolean isEmpty() {
		return (root == null);
	}

	/**
	 * Returns a Set view of the point keys for the mappings in the
	 * KDTree. Changes to the Set are reflected in the KDTree and vice
	 * versa, except that {@code Iterator.remove} is not supported.
	 * 
	 * @return A Set view of the point keys for the mappings in the KDTree.
	 */
	public Set<P> keySet() {
		return new KeySet();
	}

	/**
	 * Inserts a point value pair into the tree, preserving the
	 * spatial ordering.
	 * 
	 * @param point
	 *            The point serving as a key.
	 * @param value
	 *            The value to insert at the point.
	 * @return The old value if an existing value is replaced by the
	 *         inserted value.
	 */
	public V put(P point, V value) {
		KDNode[] parent = new KDTree.KDNode[1];
		KDNode node = getNode(point, parent);
		V old = null;

		if (node != null) {
			old = node.getValue();
			hashCode -= node.hashCode();
			node.value = value;
		} else {
			if (parent[0] == null)
				node = root = new KDNode(0, point, value);
			else {
				int discriminator = parent[0].discriminator;

				if (point.getCoord(discriminator).compareTo(
							parent[0].point.getCoord(discriminator)) >= 0)
					node = parent[0].high =
							new KDNode((discriminator + 1) % dimensions, point, value);
				else
					node = parent[0].low =
							new KDNode((discriminator + 1) % dimensions, point, value);
			}

			++size;
		}

		hashCode += node.hashCode();

		return old;
	}

	/**
	 * Copies all of the point-value mappings from the given Map into the
	 * KDTree.
	 * 
	 * @param map
	 *            The Map from which to copy the mappings.
	 */
	public void putAll(Map<? extends P, ? extends V> map) {
		for (Map.Entry<? extends P, ? extends V> pair : map.entrySet())
			put(pair.getKey(), pair.getValue());
	}

	/**
	 * Removes the point-value mapping corresponding to the given point key.
	 * 
	 * @param key
	 *            The point key of the mapping to remove.
	 * @return The value part of the mapping, if a mapping existed and
	 *         was removed. Null if not.
	 * @exception ClassCastException
	 *                If the key is not an instance of P.
	 */
	public V remove(Object key)
			throws ClassCastException {
		KDNode[] parent = new KDTree.KDNode[1];
		KDNode node = getNode((P) key, parent);
		V old = null;

		if (node != null) {
			KDNode child = node;

			node = recursiveRemoveNode(child);

			if (parent[0] == null)
				root = node;
			else if (child == parent[0].low)
				parent[0].low = node;
			else if (child == parent[0].high)
				parent[0].high = node;

			--size;
			hashCode -= child.hashCode();
			old = child.getValue();
		}

		return old;
	}

	/**
	 * Returns the number of point-value mappings in the KDTree.
	 * 
	 * @return The number of point-value mappings in the KDTree.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns a Collection view of the values contained in the KDTree.
	 * Changes to the Collection are reflected in the KDTree and vice versa.
	 * Note: the resulting Collection is very inefficient.
	 * 
	 * @return A Collection view of the values contained in the KDTree.
	 */
	public Collection<V> values() {
		return new ValueCollection();
	}

	// End Map interface methods

	public Iterator<Map.Entry<P, V>> iterator(P lower, P upper) {
		return new MapEntryIterator(lower, upper);
	}

	int fillArray(KDNode[] a, int index, KDNode node) {
		if (node == null)
			return index;
		a[index] = node;
		index = fillArray(a, index + 1, node.low);
		return fillArray(a, index, node.high);
	}

	final class NodeComparator implements Comparator<KDNode> {
		private int discriminator = 0;

		void setDiscriminator(int val) {
			discriminator = val;
		}

		int getDiscriminator() {
			return discriminator;
		}

		public int compare(KDNode n1, KDNode n2) {
			return n1.point.getCoord(discriminator).compareTo(n2.point.getCoord(discriminator));
		}
	}

	KDNode optimize(KDNode[] nodes, int begin, int end, NodeComparator comp) {
		KDNode midpoint = null;
		int size = end - begin;

		if (size > 1) {
			int nth = begin + (size >> 1);
			int nthprev = nth - 1;
			int d = comp.getDiscriminator();

			Arrays.sort(nodes, begin, end, comp);

			while (nth > begin &&
					nodes[nth].point.getCoord(d).compareTo(
									nodes[nthprev].point.getCoord(d)) == 0) {
				--nth;
				--nthprev;
			}

			midpoint = nodes[nth];
			midpoint.discriminator = d;

			if (++d >= dimensions)
				d = 0;

			comp.setDiscriminator(d);

			midpoint.low = optimize(nodes, begin, nth, comp);

			comp.setDiscriminator(d);

			midpoint.high = optimize(nodes, nth + 1, end, comp);
		} else if (size == 1) {
			midpoint = nodes[begin];
			midpoint.discriminator = comp.getDiscriminator();
			midpoint.low = midpoint.high = null;
		}

		return midpoint;
	}

	/**
	 * Optimizes the performance of future search operations by balancing the
	 * KDTree. The balancing operation is relatively expensive, but can
	 * significantly improve the performance of searches. Usually, you
	 * don't have to optimize a tree which contains random key values
	 * inserted in a random order.
	 */
	public void optimize() {
		if (isEmpty())
			return;

		KDNode[] nodes =
				(KDNode[]) Array.newInstance(KDNode.class, size());
		fillArray(nodes, 0, root);

		root = optimize(nodes, 0, nodes.length, new NodeComparator());
	}
}
