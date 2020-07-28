package org.eea.rod.persistence.repository;

import java.util.List;
import org.eea.rod.persistence.domain.Issue;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * The interface Issue feign repository.
 */
@FeignClient(name = "rodIssueInterface", url = "${rod.url}", path = "/rest/issue")
public interface IssueFeignRepository {


  /**
   * Find all list.
   *
   * @return the list
   */
  @Cacheable("rod_issue_cache")
  @GetMapping(value = "/findAll")
  List<Issue> findAll();


}
