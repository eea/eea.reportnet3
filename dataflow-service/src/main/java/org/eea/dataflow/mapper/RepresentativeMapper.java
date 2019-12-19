package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Representative;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface DataflowRepresentativeMapper.
 */
@Mapper(componentModel = "spring")
public interface RepresentativeMapper extends IMapper<Representative, RepresentativeVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the dataflow representative VO
   */
  @Override
  @Mapping(source = "userMail", target = "providerAccount")
  @Mapping(source = "dataProvider.groupId", target = "dataProviderGroupId")
  @Mapping(source = "dataProvider.id", target = "dataProviderId")
  RepresentativeVO entityToClass(Representative entity);


  /**
   * Class to entity.
   *
   * @param model the model
   * @return the dataflow representative
   */
  @Override
  @Mapping(source = "providerAccount", target = "userMail")
  @Mapping(source = "dataProviderGroupId", target = "dataProvider.groupId")
  @Mapping(source = "dataProviderId", target = "dataProvider.id")
  Representative classToEntity(RepresentativeVO model);
}
