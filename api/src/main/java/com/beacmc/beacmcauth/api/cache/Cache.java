package com.beacmc.beacmcauth.api.cache;

import java.util.Iterator;
import java.util.List;

public interface Cache<T extends CachedData<ID>, ID> extends Iterable<T> {

    default T updateCache(T data) {
        T oldData = getCacheData(data.getId());
        if (oldData == null) return null;

        getCaches().remove(oldData);
        getCaches().add(data);
        return data;
    }

    default void addCache(T data) {
        if (data != null) getCaches().add(data);
    }

    default CachedData<ID> addOrUpdateCache(T data) {
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

    default T getCacheData(ID id) {
        return getCaches().stream()
                .filter(data -> data.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    List<T> getCaches();
}
