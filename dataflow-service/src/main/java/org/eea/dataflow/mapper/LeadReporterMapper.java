package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.LeadReporter;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface LeadReporterMapper.
 */
@Mapper(componentModel = "spring")
public interface LeadReporterMapper extends IMapper<LeadReporter, LeadReporterVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the representative VO
   */
  @Override
  @Mapping(source = "representative.id", target = "representativeId")
  LeadReporterVO entityToClass(LeadReporter entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the representative
   */
  @Override
  @Mapping(source = "representativeId", target = "representative.id")
  LeadReporter classToEntity(LeadReporterVO model);

}
