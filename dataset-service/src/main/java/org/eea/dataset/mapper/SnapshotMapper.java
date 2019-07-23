package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface SnapshotMapper.
 */
@Mapper(componentModel = "spring")
public interface SnapshotMapper extends IMapper<Snapshot, SnapshotVO> {

}
