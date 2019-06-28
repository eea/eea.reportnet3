package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository extends JpaRepository<Dataflow, Long> {


  List<Dataflow> findByStatus(TypeStatusEnum status);

  @Query("SELECT df from Dataflow df WHERE df.status in('PENDING','ACCEPTED')")
  List<Dataflow> findPendingAccepted();

}
