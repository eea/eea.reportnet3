package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface SnapshotMapper.
 */
@Mapper(componentModel = "spring")
public interface PreparationDatasetMapper extends IMapper<PreparationDataset, PreparationDatasetVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the snapshot VO
   */
  @Override
  PreparationDatasetVO entityToClass(PreparationDataset entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the snapshot
   */
  @Override
  PreparationDataset classToEntity(PreparationDatasetVO model);
}
