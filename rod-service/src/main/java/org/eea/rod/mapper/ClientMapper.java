package org.eea.rod.mapper;

import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.mapper.IMapper;
import org.eea.rod.persistence.domain.Client;
import org.mapstruct.Mapper;

/**
 * The interface Client mapper.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper extends IMapper<Client, ClientVO> {

}
