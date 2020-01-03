package org.eea.dataset.service.impl;

import org.eea.dataset.mapper.CodelistMapper;
import org.eea.dataset.persistence.data.domain.Codelist;
import org.eea.dataset.persistence.data.repository.CodelistRepository;
import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
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
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return codelistMapper.entityToClass(codelist);
  }

  @Override
  public void delete(Long codelistId) {
    codelistRepository.deleteById(codelistId);
  }

  @Override
  public Long create(CodelistVO codelistVO, Long codelistId) throws EEAException {
    Long response;
    if (codelistId == null) {
      codelistVO.setStatus(CodelistStatusEnum.DESIGN);
      response = codelistRepository.save(codelistMapper.classToEntity(codelistVO)).getId();
    } else {
      Codelist oldCodelist = codelistRepository.findById(codelistId).orElse(null);
      if (oldCodelist == null) {
        throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
      }
      Codelist newCodelist = codelistMapper.classToEntity(codelistVO);
      if (newCodelist.getCategory() == null) {
        newCodelist.setCategory(oldCodelist.getCategory());
      }
      if (newCodelist.getDescription() == null) {
        newCodelist.setDescription(oldCodelist.getDescription());
      }
      if (newCodelist.getItems() == null) {
        newCodelist.setItems(oldCodelist.getItems());
      }
      if (newCodelist.getName() == null) {
        newCodelist.setName(oldCodelist.getName());
      }
      newCodelist.setVersion(oldCodelist.getVersion() + 1);
      newCodelist.setStatus(CodelistStatusEnum.DESIGN);
      response = codelistRepository.save(newCodelist).getId();
    }
    return response;
  }

  @Override
  public Long update(CodelistVO codelistVO) throws EEAException {
    Codelist oldCodelist = codelistRepository.findById(codelistVO.getId()).orElse(null);
    if (oldCodelist == null) {
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    switch (oldCodelist.getStatus()) {
      case DESIGN:
        if (CodelistStatusEnum.READY.equals(codelistVO.getStatus())) {
          oldCodelist.setStatus(CodelistStatusEnum.READY);
        }
        break;
      case DEPRECATED:
        if (CodelistStatusEnum.READY.equals(codelistVO.getStatus())) {
          oldCodelist.setStatus(CodelistStatusEnum.READY);
          codelistRepository.save(oldCodelist);
        }
        break;
      case READY:
        if (CodelistStatusEnum.DEPRECATED.equals(codelistVO.getStatus())) {
          oldCodelist.setStatus(CodelistStatusEnum.DEPRECATED);
          codelistRepository.save(oldCodelist);
        }
        break;
      default:
        throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return codelistVO.getId();
  }
}
