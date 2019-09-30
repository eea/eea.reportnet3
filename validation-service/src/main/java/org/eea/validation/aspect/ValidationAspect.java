package org.eea.validation.aspect;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidationAspect {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationAspect.class);

  private static Map<Integer, CompletableFuture<Object>> map = new ConcurrentHashMap<>();

  @Around("@annotation(org.eea.annotation.ExecuteOnlyOnce)")
  public Object checkDaasetBlocked(ProceedingJoinPoint jointPoint) throws Throwable {

    CompletableFuture<Object> alreadyExists;
    CompletableFuture<Object> response = new CompletableFuture<>();
    Integer hash =
        generateHashCode(jointPoint.getSignature().toShortString(), jointPoint.getArgs());

    synchronized (map) {
      alreadyExists = map.putIfAbsent(hash, response);
    }
    if (alreadyExists != null) {
      LOG.info("nuevo suscriptor a {}:{}", jointPoint.getSignature().toShortString(),
          jointPoint.getArgs());
      return alreadyExists.get();
    }

    LOG.info("añadido {}:{}", jointPoint.getSignature().toShortString(), jointPoint.getArgs());
    response.complete(jointPoint.proceed());
    synchronized (map) {
      map.remove(hash);
      LOG.info("eliminado {}:{}", jointPoint.getSignature().toShortString(), jointPoint.getArgs());
    }

    return response.get();
  }

  private int generateHashCode(String signature, Object[] args) {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(args);
    result = prime * result + Objects.hash(signature);
    return result;
  }

}
