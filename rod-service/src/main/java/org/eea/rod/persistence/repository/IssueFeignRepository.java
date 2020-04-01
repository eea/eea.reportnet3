package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Issue;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * The interface Issue feign repository.
 */
@FeignClient(name = "rodIssueInterface", url = "${rod.url}", path = "/rest/country")
public interface IssueFeignRepository {


  /**
   * Find all list.
   *
   * @return the list
   */
  @Cacheable("rod_issue_cache")
  @RequestMapping(value = "/findAll", method = RequestMethod.GET)
  List<Issue> findAll();


}
