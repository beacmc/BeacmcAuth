package com.beacmc.beacmcauth.api.cache;

import lombok.SneakyThrows;
import org.checkerframework.checker.signature.qual.SignatureUnknown;
import org.checkerframework.checker.units.qual.C;

import java.util.Iterator;
import java.util.List;

public interface Cache<T extends CachedData<ID>, ID> extends Iterable<T> {

    T updateCache(T data);

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
