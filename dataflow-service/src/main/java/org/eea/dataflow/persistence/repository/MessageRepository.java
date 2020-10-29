package org.eea.dataflow.persistence.repository;

import java.util.Collection;
import java.util.List;
import org.eea.dataflow.persistence.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
   * @param page the page
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderIdInAndRead(Long dataflowId,
      Collection<Long> providerIds, Boolean read, Pageable page);

  /**
   * Find by dataflow id and provider ids.
   *
   * @param dataflowId the dataflow id
   * @param providerIds the provider ids
   * @param page the page
   * @return the page
   */
  Page<Message> findByDataflowIdAndProviderIdIn(Long dataflowId, Collection<Long> providerIds,
      Pageable page);

  /**
   * Find by dataflow id and id in.
   *
   * @param dataflowId the dataflow id
   * @param messageIds the message ids
   * @return the list
   */
  List<Message> findByDataflowIdAndIdIn(Long dataflowId, Collection<Long> messageIds);
}
