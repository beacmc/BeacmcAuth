package com.beacmc.beacmcauth.core.util;

import org.jetbrains.annotations.NotNull;

public class Pair<K, V> implements Comparable<Pair<K, V>> {

    private K key;
    private V value;

    public Pair() {}

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(@NotNull Pair<K, V> other) {
        int comparison = ((Comparable<K>) this.getKey()).compareTo(other.getKey());
        if (comparison == 0) {
            comparison = ((Comparable<V>) this.getValue()).compareTo(other.getValue());
        }
        return comparison;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
