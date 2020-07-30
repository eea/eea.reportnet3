package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface AttachmentRepository extends PagingAndSortingRepository<AttachmentValue, Integer> {


  AttachmentValue findByFieldValueId(String idField);


  void deleteByFieldValueId(String idField);


}
