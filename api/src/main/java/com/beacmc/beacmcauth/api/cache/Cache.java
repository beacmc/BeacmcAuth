package com.beacmc.beacmcauth.api.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public abstract class Cache<T extends CachedData<ID>, ID> implements Iterable<T> {

    protected final Map<ID, T> caches;

    public void addOrUpdateCache(T data) {
        if (data != null)
            caches.put(data.getId(), data);
    }

    public void addCache(T data) {
        if (data != null)
            caches.put(data.getId(), data);
    }

    public @Nullable T updateCache(T data) {
        if (data == null)
            return null;

        ID id = data.getId();
        if (!caches.containsKey(id)) {
            return null;
        }

        caches.put(id, data);
        return data;
    }

    public @Nullable T getCacheData(ID id) {
        return id != null ? caches.get(id) : null;
    }

    public void removeById(ID id) {
        if (id != null) {
            caches.remove(id);
        }
    }

    public void removeCache(T data) {
        if (data != null) {
            caches.remove(data.getId(), data);
        }
    }

    public void clear() {
        caches.clear();
    }

    public boolean contains(ID id) {
        return caches.containsKey(id);
    }

    public @NotNull Stream<T> stream() {
        return caches.values().stream();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return caches.values().iterator();
    }
}
