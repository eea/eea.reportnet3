package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface WebFormMapper.
 */
@Mapper(componentModel = "spring")
public interface WebformMetabaseMapper extends IMapper<WebformMetabase, WebformMetabaseVO> {

}

