package org.eea.lock.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockServiceImpl implements RedisLockService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RedisLockServiceImpl.class);

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

  @Override
  public Map<String, String> listActiveLocks(String pattern){
    Map<String, String> activeLocks = new HashMap<>();

    ScanOptions options = ScanOptions.scanOptions()
            .match(pattern)
            .count(100)
            .build();

    RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
    try (Cursor<byte[]> cursor = connection.scan(options)) {
      while (cursor.hasNext()) {
        String key = new String(cursor.next(), StandardCharsets.UTF_8);
        String value = redisTemplate.opsForValue().get(key);
        activeLocks.put(key, value);
      }
    }
    catch (Exception e){
      LOG.error("Could not retrieve locks with pattern {}. Error: {}", pattern, e.getMessage());
      return null;
    }
    return activeLocks;
  }

  @Override
  public Boolean lockExists(String lockKey){
    if(redisTemplate.opsForValue().get(lockKey) != null){
      return true;
    }
    return false;
  }
}
