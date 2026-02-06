package com.beacmc.beacmcauth.api.cache;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public interface Cache<T extends CachedData<ID>, ID> extends Iterable<T> {

    Map<ID, T> getCaches();

    default T addOrUpdateCache(T data) {
        if (data == null) return null;
        getCaches().put(data.getId(), data);
        return data;
    }

    default void addCache(T data) {
        if (data == null) return;
        getCaches().put(data.getId(), data);
    }

    default T updateCache(T data) {
        if (data == null) return null;

        ID id = data.getId();
        if (!getCaches().containsKey(id)) return null;

        getCaches().put(id, data);
        return data;
    }

    default T getCacheData(ID id) {
        if (id == null) return null;
        return getCaches().get(id);
    }

    default void removeById(ID id) {
        if (id == null) return;
        getCaches().remove(id);
    }

    default void removeCache(T data) {
        if (data == null) return;
        getCaches().remove(data.getId(), data);
    }

    default Stream<T> stream() {
        return getCaches().values().stream();
    }

    @Override
    default @NotNull Iterator<T> iterator() {
        return getCaches().values().iterator();
    }
}
