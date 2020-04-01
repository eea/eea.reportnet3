package org.eea.rod.mapper;

import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.mapper.IMapper;
import org.eea.rod.persistence.domain.Obligation;
import org.mapstruct.Mapper;

/**
 * The interface Obligation mapper.
 */
@Mapper(componentModel = "spring")
public interface ObligationMapper extends IMapper<Obligation, ObligationVO> {

}
