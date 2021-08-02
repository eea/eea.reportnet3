package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Representative;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface RepresentativeMapper.
 */
@Mapper(componentModel = "spring", uses = LeadReporterMapper.class)
public interface RepresentativeMapper extends IMapper<Representative, RepresentativeVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the representative VO
   */
  @Override
  @Mapping(source = "dataProvider.id", target = "dataProviderId")
  @Mapping(source = "dataProvider.dataProviderGroup.id", target = "dataProviderGroupId")
  RepresentativeVO entityToClass(Representative entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the representative
   */
  @Override
  @Mapping(source = "dataProviderId", target = "dataProvider.id")
  @Mapping(source = "dataProviderGroupId", target = "dataProvider.dataProviderGroup.id")
  Representative classToEntity(RepresentativeVO model);

}
