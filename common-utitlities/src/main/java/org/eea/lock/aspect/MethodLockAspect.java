package org.eea.lock.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.model.Lock;
import org.eea.lock.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodLockAspect {

  @Autowired
  private LockService lockService;

  @Around("@annotation(org.eea.lock.annotation.LockMethod)")
  public Object addLock(ProceedingJoinPoint joinPoint) throws Throwable {

    Object rtn = null;

    Lock lock = lockService.createLock(new Timestamp(System.currentTimeMillis()),
        SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
        getLockCriteria(joinPoint), joinPoint.getSignature().toShortString());

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    LockMethod lockMethod = method.getAnnotation(LockMethod.class);

    if (lock != null) {
      rtn = joinPoint.proceed();
      if (lockMethod.removeWhenFinish()) {
        lockService.removeLock(lock.getId());
      }
    }

    return rtn;
  }

  private Map<Integer, Object> getLockCriteria(ProceedingJoinPoint joinPoint)
      throws NoSuchMethodException {

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String methodName = signature.getMethod().getName();
    Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
    Annotation[][] annotations = joinPoint.getTarget().getClass()
        .getMethod(methodName, parameterTypes).getParameterAnnotations();

    Object[] arguments = joinPoint.getArgs();
    HashMap<Integer, Object> criteria = new HashMap<>();
    for (int i = 0; i < annotations.length; i++) {
      // annotated parameter, search @LockCriteria annotated parameter if any
      if (annotations[i].length > 0) {
        for (Annotation annotation : annotations[i]) {
          if (annotation.annotationType().equals(LockCriteria.class)) {
            criteria.put(i, arguments[i]);
          }
        }
      }
    }

    return criteria;
  }
}
