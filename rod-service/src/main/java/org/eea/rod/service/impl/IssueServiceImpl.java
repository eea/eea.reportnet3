package org.eea.rod.service.impl;

import java.util.List;
import org.eea.interfaces.vo.rod.IssueVO;
import org.eea.rod.mapper.IssueMapper;
import org.eea.rod.persistence.repository.IssueFeignRepository;
import org.eea.rod.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Issue service.
 */
@Service
public class IssueServiceImpl implements IssueService {

  @Autowired
  private IssueFeignRepository issueFeignRepository;

  @Autowired
  private IssueMapper issueMapper;

  @Override
  public List<IssueVO> findAll() {
    return issueMapper.entityListToClass(issueFeignRepository.findAll());
  }
}
