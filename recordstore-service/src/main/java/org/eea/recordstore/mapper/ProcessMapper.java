package org.eea.recordstore.mapper;

import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.mapper.IMapper;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


/**
 * The Interface ProcessMapper.
 */
@Mapper(componentModel = "spring")
public interface ProcessMapper extends IMapper<EEAProcess, ProcessVO> {

  String DELETED = "DELETED";

  /**
   * Sets the names.
   *
   * @param processVO the new names
   */
  @AfterMapping
  default void setNames(@MappingTarget ProcessVO processVO) {
    processVO.setDataflowName(
        processVO.getDataflowName() != null ? processVO.getDataflowName() : DELETED);
    processVO
        .setDatasetName(processVO.getDatasetName() != null ? processVO.getDatasetName() : DELETED);
  }
}

