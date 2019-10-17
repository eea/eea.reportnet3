package org.eea.lock.controller;

import java.util.List;
import org.eea.interfaces.vo.lock.LockVO;
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
  public List<LockVO> findAllLocks() {
    return lockService.findAll();
  }

  @GetMapping("/findOne/{lockId}")
  public LockVO findOneLock(@PathVariable("lockId") final Integer lockId) {
    return lockService.findLock(lockId);
  }
}
