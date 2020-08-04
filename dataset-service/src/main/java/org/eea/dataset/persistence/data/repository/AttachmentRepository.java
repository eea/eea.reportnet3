package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * The Interface AttachmentRepository.
 */
public interface AttachmentRepository extends PagingAndSortingRepository<AttachmentValue, Integer> {


  /**
   * Find by field value id.
   *
   * @param idField the id field
   * @return the attachment value
   */
  AttachmentValue findByFieldValueId(String idField);


  /**
   * Delete by field value id.
   *
   * @param idField the id field
   */
  void deleteByFieldValueId(String idField);


}
