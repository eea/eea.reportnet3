package org.eea.lock.redis;

public interface RedisLockService {
  boolean checkAndAcquireLock(String lockKey, String value, long expireTimeInMillis);
  void releaseLock(String lockKey, String value);
}
