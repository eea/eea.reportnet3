package org.eea.collaboration.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.MessageVO;

public interface CollaborationService {

  MessageVO createMessage(Long dataflowId, MessageVO messageVO) throws EEAException;

  void updateMessageReadStatus(Long dataflowId, List<MessageVO> messageVOs) throws EEAException;

  List<MessageVO> findMessages(Long dataflowId, Long providerId, Boolean read, int page)
      throws EEAException;
}
