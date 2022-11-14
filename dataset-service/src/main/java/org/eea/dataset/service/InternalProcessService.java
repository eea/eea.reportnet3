package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.InternalProcess;
import org.eea.interfaces.vo.dataset.InternalProcessVO;

import java.util.List;

public interface InternalProcessService {

    void save(InternalProcess internalProcess);

    void delete(Long id);

    List<InternalProcessVO> findAll();
}
