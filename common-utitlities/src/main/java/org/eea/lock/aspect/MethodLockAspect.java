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
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    LockMethod lockMethod = method.getAnnotation(LockMethod.class);

    try {
      Object user = ThreadPropertiesManager.getVariable("user");
      LockVO lockVO = lockService.createLock(new Timestamp(System.currentTimeMillis()),
          user != null ? (String) user : null, LockType.METHOD, getLockCriteria(joinPoint));

      Object rtn = joinPoint.proceed();
      if (lockMethod.removeWhenFinish()) {
        lockService.removeLock(lockVO.getId());
      }

      return rtn;

    } catch (EEAException e) {
      if (lockMethod.isController()) {
        throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
      }
      throw e;
    }
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
    Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
    Annotation[][] annotations = joinPoint.getTarget().getClass()
        .getMethod(signature.getMethod().getName(), parameterTypes).getParameterAnnotations();

    Object[] arguments = joinPoint.getArgs();
    HashMap<String, Object> criteria = new HashMap<>();
    criteria.put("signature", signature.toShortString());
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
