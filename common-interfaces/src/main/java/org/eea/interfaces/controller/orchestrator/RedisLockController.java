package org.eea.interfaces.controller.orchestrator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

public interface RedisLockController {

    /**
     * The Interface RedisLockControllerZuul.
     */
    @FeignClient(value = "redis", path = "/redis")
    interface RedisLockControllerZuul extends RedisLockController {
    }

    @GetMapping(value = "/getActiveRedisLocksByKey")
    Map<String, String> getActiveRedisLocksByKey(@RequestParam(value = "lockKeyPrefix", required = false) String lockKeyPrefix);

    @DeleteMapping(value = "/releaseLock")
    void releaseLock(@RequestParam("lockKey") String lockKey, @RequestParam("lockValue") String lockValue);

    @PostMapping(value = "/createLock")
    Boolean createLock(@RequestParam(value = "lockKey") String lockKey, @RequestParam(value = "lockValue") String lockValue, @RequestParam(value = "expirationMs") Long expirationMs);
}
