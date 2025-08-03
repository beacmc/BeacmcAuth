package com.beacmc.beacmcauth.api.cache;

import java.util.Iterator;
import java.util.List;

public interface Cache<T, ID> extends Iterable<T> {

    T updateCache(T data);

    default void addCache(T data) {
        if (data != null) getCaches().add(data);
    }

    default T addOrUpdateCache(T data) {
        if (data == null)
            return null;

        if (updateCache(data) == null) {
            addCache(data);
        }
        return data;
    }

    default Iterator<T> iterator() {
        return getCaches().iterator();
    }

    default void removeCache(T data) {
        getCaches().remove(data);
    }

    T getCacheData(ID id);

    List<T> getCaches();
}
