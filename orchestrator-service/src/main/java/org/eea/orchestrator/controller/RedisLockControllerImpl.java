package org.eea.orchestrator.controller;

import org.apache.commons.lang.StringUtils;
import org.eea.interfaces.controller.orchestrator.RedisLockController;
import org.eea.lock.redis.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/redis")
public class RedisLockControllerImpl implements RedisLockController {


    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RedisLockControllerImpl.class);

    @Autowired
    private RedisLockService redisLockService;


    @Override
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping(value = "/getActiveRedisLocksByKey")
    public Set<String> getActiveRedisLocksByKey(@RequestParam(value = "lockKeyPrefix", required = false) String lockKeyPrefix){
        try{
            lockKeyPrefix = (StringUtils.isNotBlank(lockKeyPrefix)) ? lockKeyPrefix : "*";
            return redisLockService.listActiveLocks(lockKeyPrefix);
        }
        catch (Exception e){
            LOG.error("Could not retrieve active redis locks for lock key prefix {} Error {}", lockKeyPrefix, e.getMessage());
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping(value = "/releaseLock")
    public void releaseLock(@RequestParam("lockKey") String lockKey, @RequestParam("lockValue") String lockValue){
        try{
            redisLockService.releaseLock(lockKey, lockValue);
        }
        catch (Exception e){
            LOG.error("Could not release lock with key {} and value {} Error: {}", lockKey, lockValue, e.getMessage());
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping(value = "/createLock")
    public Boolean createLock(@RequestParam(value = "lockKey") String lockKey, @RequestParam(value = "lockValue") String lockValue, @RequestParam(value = "expirationMs") Long expirationMs){
        return redisLockService.checkAndAcquireLock(lockKey, lockValue, expirationMs);
    }
}
