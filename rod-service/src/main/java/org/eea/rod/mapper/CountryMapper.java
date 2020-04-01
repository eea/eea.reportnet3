package org.eea.rod.mapper;

import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.mapper.IMapper;
import org.eea.rod.persistence.domain.Country;
import org.mapstruct.Mapper;

/**
 * The interface Country mapper.
 */
@Mapper(componentModel = "spring")
public interface CountryMapper extends IMapper<Country, CountryVO> {

}
