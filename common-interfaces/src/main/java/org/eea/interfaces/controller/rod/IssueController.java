package org.eea.interfaces.controller.rod;

import java.util.List;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * The interface Issue controller.
 */
public interface IssueController {

  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "rodIssue", path = "/obligation_issue")
  interface IssueControllerZull extends IssueController {

  }


  /**
   * Find all issues.
   *
   * @return the list
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  List<IssueVO> findAll();


}
