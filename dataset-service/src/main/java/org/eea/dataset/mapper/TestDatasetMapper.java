package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.TestDataset;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface TestDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface TestDatasetMapper extends IMapper<TestDataset, TestDatasetVO> {

}
