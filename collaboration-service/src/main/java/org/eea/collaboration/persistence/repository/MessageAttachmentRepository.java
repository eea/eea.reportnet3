package org.eea.collaboration.persistence.repository;

import org.eea.collaboration.persistence.domain.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface MessageAttachmentRepository.
 */
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

  /**
   * Find by message id.
   *
   * @param messageId the message id
   * @return the message attachment
   */
  MessageAttachment findByMessageId(Long messageId);
}
