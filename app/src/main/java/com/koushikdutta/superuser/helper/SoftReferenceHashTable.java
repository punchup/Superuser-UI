/**
 * Superuser
 * Copyright (C) 2016 Pierre-Hugues Husson (phhusson)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.koushikdutta.superuser.helper;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

class SoftReferenceHashTable<K,V> {
    private Hashtable<K, SoftReference<V>> mTable = new Hashtable<>();

    public V put(K key, V value) {
        SoftReference<V> old = mTable.put(key, new SoftReference<V>(value));

        if (old == null) return null;

        return old.get();
    }

    public V get(K key) {
        SoftReference<V> val = mTable.get(key);

        if (val == null) return null;

        V ret = val.get();

        if (ret == null) mTable.remove(key);

        return ret;
    }

    public V remove(K k) {
        SoftReference<V> v = mTable.remove(k);

        if (v == null) return null;

        return v.get();
    }
}
