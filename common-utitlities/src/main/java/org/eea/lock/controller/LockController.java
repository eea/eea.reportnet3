package org.eea.lock.controller;

import java.util.List;
import org.eea.lock.model.Lock;
import org.eea.lock.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lock")
public class LockController {

  @Autowired
  private LockService lockService;

  @PostMapping("/remove/{lockId}")
  public void removeLock(@PathVariable("lockId") final Integer lockId) {
    lockService.removeLock(lockId);
  }

  @GetMapping("/findAll")
  public List<Lock> findAllLocks() {
    return lockService.findAll();
  }

  @GetMapping("/findOne/{lockId}")
  public Lock findOneLock(@PathVariable("lockId") final Integer lockId) {
    return lockService.findLock(lockId);
  }
}
