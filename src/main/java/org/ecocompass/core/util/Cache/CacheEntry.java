package org.ecocompass.core.util.Cache;

import java.time.LocalDateTime;
import java.util.Objects;

public class CacheEntry<T> {
    private T data;
    private LocalDateTime expirationTime;

    public CacheEntry(T data, int expirationTime) {
        this.data = data;
        this.expirationTime = LocalDateTime.now().plusMinutes(expirationTime);
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntry<?> cacheEntry = (CacheEntry<?>) o;
        return Objects.equals(data, cacheEntry.data) && Objects.equals(expirationTime, cacheEntry.expirationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, expirationTime);
    }
}