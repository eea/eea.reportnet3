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


  List<Dataflow> findByStatus(TypeStatusEnum status);

  @Query("SELECT df from Dataflow df WHERE df.status in('PENDING','ACCEPTED') ORDER BY df.deadlineDate ASC")
  List<Dataflow> findPendingAccepted();


  @Query("SELECT df from Dataflow df WHERE df.status='COMPLETED' ORDER BY df.deadlineDate ASC")
  List<Dataflow> findCompleted();



  @Query("SELECT df from Dataflow df JOIN df.userRequests ur WHERE ur.requestType = :type "
      + " AND ur.userRequester = :idRequester ORDER BY df.deadlineDate ASC")
  List<Dataflow> findByStatusAndUserRequester(@Param("type") TypeRequestEnum typeRequest,
      @Param("idRequester") Long userIdRequester);

}
