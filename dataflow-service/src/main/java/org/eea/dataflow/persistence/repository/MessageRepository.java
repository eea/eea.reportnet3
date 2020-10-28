package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface MessageRepository.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

  /**
   * Find by dataflow id and read.
   *
   * @param dataflowId the dataflow id
   * @param read the read
   * @param pageable the pageable
   * @return the page
   */
  Page<Message> findByDataflowIdAndRead(Long dataflowId, boolean read, Pageable pageable);

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @param pageable the pageable
   * @return the page
   */
  Page<Message> findByDataflowId(Long dataflowId, Pageable pageable);

  /**
   * Find by dataflow id and provider ids and read.
   *
   * @param dataflowId the dataflow id
   * @param providerIds the provider ids
   * @param read the read
   * @param pageRequest the page request
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderIdInAndRead(Long dataflowId, List<Long> providerIds,
      Boolean read, Pageable page);

  /**
   * Find by dataflow id and provider ids.
   *
   * @param dataflowId the dataflow id
   * @param providerIds the provider ids
   * @param pageRequest the page request
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderIdIn(Long dataflowId, List<Long> providerIds,
      Pageable page);

  /**
   * Update read status.
   *
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @param read the read
   * @return the int
   */
  @Modifying
  @Query("UPDATE Message m SET m.read = :read WHERE m.id = :messageId AND m.dataflowId = :dataflowId")
  int updateReadStatus(@Param("dataflowId") Long dataflowId, @Param("messageId") Long messageId,
      @Param("read") boolean read);
}
