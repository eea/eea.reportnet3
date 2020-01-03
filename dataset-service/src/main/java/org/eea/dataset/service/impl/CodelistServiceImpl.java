package org.eea.dataset.service.impl;

import org.eea.dataset.mapper.CodelistMapper;
import org.eea.dataset.persistence.data.domain.Codelist;
import org.eea.dataset.persistence.data.repository.CodelistRepository;
import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("codelistService")
public class CodelistServiceImpl implements CodelistService {

  @Autowired
  private CodelistRepository codelistRepository;

  @Autowired
  private CodelistMapper codelistMapper;

  @Override
  public CodelistVO getById(Long codelistId) throws EEAException {
    Codelist codelist = codelistRepository.findById(codelistId).orElse(null);
    if (null == codelist) {
      throw new EEAException();
    }
    return codelistMapper.entityToClass(codelist);
  }
}
