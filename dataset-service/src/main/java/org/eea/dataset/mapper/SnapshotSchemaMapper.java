package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;



/**
 * The Interface SnapshotSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface SnapshotSchemaMapper extends IMapper<SnapshotSchema, SnapshotVO> {

}
