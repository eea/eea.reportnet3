package org.eea.dataset.service.impl;

import org.eea.dataset.mapper.InternalProcessMapper;
import org.eea.dataset.persistence.metabase.domain.InternalProcess;
import org.eea.dataset.persistence.metabase.repository.InternalProcessRepository;
import org.eea.dataset.service.InternalProcessService;
import org.eea.interfaces.vo.dataset.InternalProcessVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalProcessServiceImpl implements InternalProcessService {

    private InternalProcessRepository internalProcessRepository;
    private InternalProcessMapper internalProcessMapper;

    @Autowired
    public InternalProcessServiceImpl(InternalProcessRepository internalProcessRepository, InternalProcessMapper internalProcessMapper) {
        this.internalProcessRepository = internalProcessRepository;
        this.internalProcessMapper = internalProcessMapper;
    }

    @Override
    public void save(InternalProcess internalProcess) {
        internalProcessRepository.save(internalProcess);
    }


    @Override
    public void delete(Long id) {
        internalProcessRepository.deleteById(id);
    }

    @Override
    public List<InternalProcessVO> findAll() {
        List<InternalProcess> internalProcesses = internalProcessRepository.findAll();
        return internalProcessMapper.entityListToClass(internalProcesses);
    }
}
