package org.eea.lock.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLockServiceImpl implements RedisLockService {

  private final RedisTemplate<String, String> redisTemplate;

  public RedisLockServiceImpl(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean checkAndAcquireLock(String lockKey, String value, long expireTimeInMillis) {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTimeInMillis, TimeUnit.MILLISECONDS);
    return success != null && success;
  }

  @Override
  public void releaseLock(String lockKey, String value) {
    String storedValue = redisTemplate.opsForValue().get(lockKey);
    if (value.equals(storedValue)) {
      redisTemplate.delete(lockKey);
    }
  }
}
