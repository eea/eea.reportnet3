package org.eea.dataflow.persistence.repository;

import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.UserRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface UserRequestRepository.
 */
public interface UserRequestRepository extends PagingAndSortingRepository<UserRequest, Long> {


  /**
   * Update user request status.
   *
   * @param userRequestId the user request id
   * @param typeStatus the type status
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "UPDATE user_request SET request_type=:type WHERE id=:userRequestId")
  void updateUserRequestStatus(@Param("userRequestId") String userRequestId,
      @Param("type") String typeStatus);

}
