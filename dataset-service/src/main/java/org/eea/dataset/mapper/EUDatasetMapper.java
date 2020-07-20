package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;



/**
 * The Interface EUDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface EUDatasetMapper extends IMapper<EUDataset, EUDatasetVO> {

}
