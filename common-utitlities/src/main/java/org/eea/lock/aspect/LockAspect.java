package org.eea.lock.aspect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.eea.lock.annotation.LockCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LockAspect {

  private static final Logger LOG = LoggerFactory.getLogger(LockAspect.class);

  private static Map<Integer, CompletableFuture<Object>> map = new ConcurrentHashMap<>();

  @Around("@annotation(org.eea.lock.annotation.ExecuteOnlyOnce)")
  public Object checkDaasetBlocked(ProceedingJoinPoint jointPoint) throws Throwable {

    CompletableFuture<Object> alreadyExists;
    CompletableFuture<Object> response = new CompletableFuture<>();
    List<Object> lockCriteria = getLockCriteria(jointPoint);
    Integer hash =
        generateHashCode(jointPoint.getSignature().toShortString(), lockCriteria);

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

  private List<Object> getLockCriteria(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String methodName = signature.getMethod().getName();
    Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
    Annotation[][] annotations = joinPoint.getTarget().getClass()
        .getMethod(methodName, parameterTypes).getParameterAnnotations();

    Object[] arguments = joinPoint.getArgs();
    List<Object> criteria = new ArrayList<>();
    for (int i = 0; i < annotations.length; i++) {
      // annotated parameter, search @LockCriteria annotated parameter if any
      if (annotations[i].length > 0) {
        for (Annotation annotation : annotations[i]) {
          if (annotation.annotationType().equals(LockCriteria.class)) {
            criteria.add(arguments[i]);
          }
        }
      }
    }
    return criteria;
  }

  private int generateHashCode(String signature, List<Object> args) {

    return Objects.hash(args.hashCode(), signature);
  }

}
