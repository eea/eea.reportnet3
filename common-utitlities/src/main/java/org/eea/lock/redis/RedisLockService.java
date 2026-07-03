package org.eea.lock.redis;

import java.util.Map;

public interface RedisLockService {
  boolean checkAndAcquireLock(String lockKey, String value, long expireTimeInMillis);
  void releaseLock(String lockKey, String value);

  Map<String, String> listActiveLocks(String prefix);

  void setBlocker(Long datasetId, String processId, String preparationCode);
  boolean hasBlocker(Long datasetId, String processId, String preparationCode);
  void removeBlocker(Long datasetId, String processId, String preparationCode);
}
