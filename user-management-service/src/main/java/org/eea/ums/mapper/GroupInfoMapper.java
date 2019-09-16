package org.eea.ums.mapper;

import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.mapper.IMapper;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.mapstruct.Mapper;

/**
 * The interface Group info mapper.
 */
@Mapper(componentModel = "spring")
public interface GroupInfoMapper extends IMapper<GroupInfo, ResourceInfoVO> {

}
