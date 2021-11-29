package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.IssueController;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.rod.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The type Issue controller.
 */
@RestController
@ApiIgnore
@RequestMapping("/obligation_issue")
public class IssueControllerImpl implements IssueController {

  /** The issue service. */
  @Autowired
  private IssueService issueService;

  /**
   * Find all.
   *
   * @return the list
   */
  @Override
  @GetMapping(value = "/")
  @ApiOperation(value = "Gets a list with all the obligation issues", response = IssueVO.class,
      responseContainer = "List", hidden = true)
  public List<IssueVO> findAll() {
    return issueService.findAll();
  }
}
