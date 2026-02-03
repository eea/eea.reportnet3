package org.eea.dataset.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.persistence.metabase.repository.PreparationDatasetRepository;
import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.eea.dataset.service.PreparationDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class PreparationDatasetServiceImpl.
 */
@Service
public class PreparationDatasetServiceImpl
        implements PreparationDatasetService {

    private final PreparationDatasetRepository preparationDatasetRepository;

    @Autowired
    public PreparationDatasetServiceImpl(
            PreparationDatasetRepository preparationDatasetRepository) {
        this.preparationDatasetRepository = preparationDatasetRepository;
    }

    @Override
    @Transactional
    public List<PreparationDatasetVO> findPreparationDatasets(
            Long dataflowId,
            Long providerId,
            String code) {

        List<PreparationDataset> entities;

        if (StringUtils.isNotBlank(code)) {
            entities = preparationDatasetRepository
                    .findByDataflowIdAndProviderIdAndCode(dataflowId, providerId, code);
        } else if (providerId != null) {
            entities = preparationDatasetRepository
                    .findByDataflowIdAndProviderId(dataflowId, providerId);
        } else {
            entities = preparationDatasetRepository
                    .findByDataflowId(dataflowId);
        }

        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void createPreparationDataset(
            Long dataflowId,
            Long parentDatasetId,
            PreparationDatasetVO vo) throws EEAException {

        if (preparationDatasetRepository
                .existsByDataflowIdAndProviderIdAndCode(
                        dataflowId, vo.getProviderId(), vo.getCode())) {
            throw new EEAException("Preparation dataset with this code already exists");
        }

        PreparationDataset preparationDataset = new PreparationDataset();
        preparationDataset.setDataflowId(dataflowId);
        preparationDataset.setProviderId(vo.getProviderId());
        preparationDataset.setParentDatasetId(parentDatasetId);
        preparationDataset.setCode(vo.getCode());
        preparationDataset.setDatasetName(vo.getDatasetName());
        preparationDataset.setIsCreated(Boolean.TRUE.equals(vo.getIsCreated()));

        preparationDatasetRepository.save(preparationDataset);
    }


    @Override
    @Transactional
    public void deletePreparationDatasetById(Long preparationId) throws EEAException {

        if (!preparationDatasetRepository.existsById(preparationId)) {
            throw new EEAException("Preparation dataset not found");
        }

        preparationDatasetRepository.deleteById(preparationId);
    }

    /**
     * Maps entity to VO.
     */
    private PreparationDatasetVO toVO(
            PreparationDataset preparationDataset) {

        PreparationDatasetVO vo = new PreparationDatasetVO();
        vo.setId(preparationDataset.getId());
        vo.setParentDatasetId(preparationDataset.getParentDatasetId());
        vo.setDataflowId(preparationDataset.getDataflowId());
        vo.setProviderId(preparationDataset.getProviderId());
        vo.setDatasetName(preparationDataset.getDatasetName());
        vo.setCode(preparationDataset.getCode());
        vo.setIsCreated(preparationDataset.getIsCreated());

        return vo;
    }
}
