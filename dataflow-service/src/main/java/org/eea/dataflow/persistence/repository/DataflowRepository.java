package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository extends JpaRepository<Dataflow, Long> {


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
  @Query("SELECT df from Dataflow df JOIN df.userRequests ur WHERE ur.requestType in ('PENDING','ACCEPTED') "
      + " AND ur.userRequester = :idRequester AND df.status not in ('COMPLETED') ORDER BY df.deadlineDate ASC")
  List<Dataflow> findPendingAccepted(@Param("idRequester") Long userIdRequester);



  /**
   * Find completed.
   *
   * @param userIdRequester the user id requester
   * @return the list
   */
  @Query("SELECT df from Dataflow df INNER JOIN FETCH df.userRequests ur WHERE ur.requestType = 'ACCEPTED' "
      + " AND ur.userRequester = :idRequester AND df.status = 'COMPLETED' ORDER BY df.deadlineDate ASC")
  List<Dataflow> findCompleted(@Param("idRequester") Long userIdRequester);



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

}
