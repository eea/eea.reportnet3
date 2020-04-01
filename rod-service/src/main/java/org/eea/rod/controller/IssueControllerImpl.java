package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.IssueController;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.rod.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Issue controller.
 */
@RestController
@RequestMapping("/obligation_issue")
public class IssueControllerImpl implements IssueController {

  @Autowired
  private IssueService issueService;


  @Override
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public List<IssueVO> findAll() {
    return issueService.findAll();
  }
}
