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
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class MethodLockAspect.
 */
@Aspect
@Component
public class MethodLockAspect {

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * Adds the lock.
   *
   * @param joinPoint the join point
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(org.eea.lock.annotation.LockMethod)")
  public Object addLock(ProceedingJoinPoint joinPoint) throws Throwable {

    Object rtn = null;
    Object aux = ThreadPropertiesManager.getVariable("user");

    LockVO lockVO = lockService.createLock(new Timestamp(System.currentTimeMillis()),
        aux != null ? (String) aux : null, LockType.METHOD, getLockCriteria(joinPoint));

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    LockMethod lockMethod = method.getAnnotation(LockMethod.class);

    if (lockVO != null) {
      rtn = joinPoint.proceed();
      if (lockMethod.removeWhenFinish()) {
        lockService.removeLock(lockVO.getId());
      }
    }

    return rtn;
  }

  /**
   * Gets the lock criteria.
   *
   * @param joinPoint the join point
   * @return the lock criteria
   * @throws NoSuchMethodException the no such method exception
   */
  private Map<String, Object> getLockCriteria(ProceedingJoinPoint joinPoint)
      throws NoSuchMethodException {

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String methodName = signature.getMethod().getName();
    Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
    Annotation[][] annotations = joinPoint.getTarget().getClass()
        .getMethod(methodName, parameterTypes).getParameterAnnotations();

    Object[] arguments = joinPoint.getArgs();
    HashMap<String, Object> criteria = new HashMap<>();
    criteria.put("signature", joinPoint.getSignature().toShortString());
    for (int i = 0; i < annotations.length; i++) {
      // annotated parameter, search @LockCriteria annotated parameter if any
      if (annotations[i].length > 0) {
        for (Annotation annotation : annotations[i]) {
          if (annotation.annotationType().equals(LockCriteria.class)) {
            criteria.put(((LockCriteria) annotation).name(), arguments[i]);
          }
        }
      }
    }

    return criteria;
  }
}
