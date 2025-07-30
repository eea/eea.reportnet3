package org.eea.lock.redis;

import java.util.Set;

public interface RedisLockService {
  boolean checkAndAcquireLock(String lockKey, String value, long expireTimeInMillis);
  void releaseLock(String lockKey, String value);

  Set<String> listActiveLocks(String prefix);
}
