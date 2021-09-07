package org.eea.collaboration.persistence.repository;

import java.util.Collection;
import java.util.List;
import org.eea.collaboration.persistence.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
   * @param providerId the provider id
   * @param read the read
   * @param pageable the pageable
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderIdAndRead(Long dataflowId, Long providerId, boolean read,
      Pageable pageable);

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param pageable the pageable
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderId(Long dataflowId, Long providerId, Pageable pageable);

  /**
   * Find by dataflow id and id in.
   *
   * @param dataflowId the dataflow id
   * @param messageIds the message ids
   * @return the list
   */
  List<Message> findByDataflowIdAndIdIn(Long dataflowId, Collection<Long> messageIds);

  /**
   * Count by data flow id.
   *
   * @param dataflowId the dataflow id
   * @return the long
   */
  @Query(nativeQuery = true,
      value = "select count(*) from message m where m.dataflow_id = :dataflowId")
  Long countByDataFlowId(@Param("dataflowId") Long dataflowId);
}
