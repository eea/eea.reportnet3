package org.eea.dataset.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.persistence.metabase.repository.PreparationDatasetRepository;
import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.eea.dataset.service.PreparationDatasetService;
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
            Long providerId) {

        List<PreparationDataset> entities;

        if (providerId != null) {
            entities =
                    preparationDatasetRepository
                            .findByDataflowIdAndProviderId(dataflowId, providerId);
        } else {
            entities =
                    preparationDatasetRepository
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
            PreparationDatasetVO vo) {

        if (vo == null) {
            throw new IllegalArgumentException("PreparationDatasetVO is mandatory");
        }

        if (StringUtils.isBlank(vo.getCode())) {
            throw new IllegalArgumentException("Preparation dataset code is mandatory");
        }

        if (StringUtils.isBlank(vo.getDatasetName())) {
            throw new IllegalArgumentException("Preparation dataset name is mandatory");
        }

        PreparationDataset preparationDataset = new PreparationDataset();
        preparationDataset.setDataflowId(dataflowId);
        preparationDataset.setProviderId(vo.getProviderId());
        preparationDataset.setParentDatasetId(parentDatasetId);
        preparationDataset.setCode(vo.getCode());
        preparationDataset.setDatasetName(vo.getDatasetName());
        preparationDataset.setIsCreated(
                vo.getIsCreated() != null ? vo.getIsCreated() : null);

        preparationDatasetRepository.save(preparationDataset);
    }

    @Override
    @Transactional
    public void deletePreparationDatasetById(Long preparationDatasetId) {
        preparationDatasetRepository.deleteById(preparationDatasetId);
    }


    @Override
    @Transactional
    public void deleteAllPreparationDatasetsByProviderAndDataflowId(
            Long dataflowId,
            Long providerId) {

        preparationDatasetRepository
                .deleteByDataflowIdAndProviderId(dataflowId, providerId);
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
