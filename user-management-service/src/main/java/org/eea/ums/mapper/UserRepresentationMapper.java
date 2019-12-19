package org.eea.ums.mapper;

import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.mapper.IMapper;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

/**
 * The Interface UserRepresentationMapper.
 */
@Mapper(componentModel = "spring")
public interface UserRepresentationMapper
    extends IMapper<UserRepresentation, UserRepresentationVO> {

}
