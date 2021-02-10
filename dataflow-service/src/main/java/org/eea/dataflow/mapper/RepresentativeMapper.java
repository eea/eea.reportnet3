package org.eea.dataflow.mapper;

import java.util.stream.Collectors;
import org.eea.dataflow.persistence.domain.Representative;
import org.eea.dataflow.persistence.domain.User;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface RepresentativeMapper.
 */
@Mapper(componentModel = "spring")
public interface RepresentativeMapper extends IMapper<Representative, RepresentativeVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the representative VO
   */
  @Override
  @Mapping(source = "dataProvider.groupId", target = "dataProviderGroupId")
  @Mapping(source = "dataProvider.id", target = "dataProviderId")
  RepresentativeVO entityToClass(Representative entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the representative
   */
  @Override
  @Mapping(source = "dataProviderGroupId", target = "dataProvider.groupId")
  @Mapping(source = "dataProviderId", target = "dataProvider.id")
  Representative classToEntity(RepresentativeVO model);

  @AfterMapping
  default void fillEmails(Representative representative,
      @MappingTarget RepresentativeVO representativeVO) {
    representativeVO.setProviderAccounts(
        representative.getReporters().stream().map(User::getUserMail).collect(Collectors.toList()));
  }

  @AfterMapping
  default void fillEmails(RepresentativeVO representativeVO,
      @MappingTarget Representative representative) {
    representative.setReporters(representativeVO.getProviderAccounts().stream()
        .map(email -> new User(null, email, null)).collect(Collectors.toSet()));
  }

}
