package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository
    extends PagingAndSortingRepository<Dataflow, Long>, DataflowExtendedRepository {


  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  List<Dataflow> findByStatus(TypeStatusEnum status);

  /**
   * Find pending accepted.
   *
   * @param userIdRequester the user id requester
   * @return the list
   */
  @Query("SELECT df as dataflow, ur.requestType as typeRequestEnum, ur.id as requestId from Dataflow df "
      + "JOIN df.userRequests ur WHERE ur.requestType in ('PENDING','ACCEPTED') "
      + " AND ur.userRequester = :idRequester AND df.status not in ('COMPLETED') ORDER BY df.deadlineDate ASC")
  List<DataflowWithRequestType> findPendingAccepted(@Param("idRequester") Long userIdRequester);



  /**
   * Find by status and user requester.
   *
   * @param typeRequest the type request
   * @param userIdRequester the user id requester
   * @return the list
   */
  @Query("SELECT df from Dataflow df JOIN df.userRequests ur WHERE ur.requestType = :type "
      + " AND ur.userRequester = :idRequester ORDER BY df.deadlineDate ASC")
  List<Dataflow> findByStatusAndUserRequester(@Param("type") TypeRequestEnum typeRequest,
      @Param("idRequester") Long userIdRequester);

  Optional<Dataflow> findByName(String name);
}
