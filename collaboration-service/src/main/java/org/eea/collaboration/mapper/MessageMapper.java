package org.eea.collaboration.mapper;

import org.eea.collaboration.persistence.domain.Message;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface MessageMapper.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper extends IMapper<Message, MessageVO> {

}
