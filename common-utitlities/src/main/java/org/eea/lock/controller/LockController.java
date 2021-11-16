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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class LockController.
 */
@RestController
@ApiIgnore
@RequestMapping("/lock")
@Api(tags = "Lock : Lock Manager")
public class LockController {

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * Removes the lock.
   *
   * @param lockId the lock id
   */
  @PostMapping("/private/remove/{lockId}")
  @ApiOperation(value = "Remove Lock", hidden = true)
  public void removeLock(@PathVariable("lockId") final Integer lockId) {
    lockService.removeLock(lockId);
  }

  /**
   * Find all locks.
   *
   * @return the list
   */
  @GetMapping("/private/findAll")
  @ApiOperation(value = "find all Locks", hidden = true)
  public List<LockVO> findAllLocks() {
    return lockService.findAll();
  }

  /**
   * Find one lock.
   *
   * @param lockId the lock id
   * @return the lock VO
   */
  @GetMapping("/private/findOne/{lockId}")
  @ApiOperation(value = "Find  one Lock", hidden = true)
  public LockVO findOneLock(@PathVariable("lockId") final Integer lockId) {
    return lockService.findById(lockId);
  }
}
