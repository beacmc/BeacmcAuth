package com.beacmc.beacmcauth.api.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Cache<T extends CachedData<ID>, ID> implements Iterable<T> {

    public abstract Map<ID, T> getCaches();

    public void addOrUpdateCache(T data) {
        if (data != null)
            getCaches().put(data.getId(), data);
    }

    public void addCache(T data) {
        if (data != null)
            getCaches().put(data.getId(), data);
    }

    public @Nullable T updateCache(T data) {
        if (data == null)
            return null;

        ID id = data.getId();
        if (!getCaches().containsKey(id))
            return null;

        getCaches().put(id, data);
        return data;
    }

    public T getCacheData(ID id) {
        return id != null ? getCaches().get(id) : null;
    }

    public void removeById(ID id) {
        if (id != null)
            getCaches().remove(id);
    }

    public void removeCache(T data) {
        if (data != null)
            getCaches().remove(data.getId(), data);
    }

    public boolean contains(ID id) {
        return getCaches().containsKey(id);
    }

    public @NotNull Stream<T> stream() {
        return getCaches().values().stream();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return getCaches().values().iterator();
    }
}
