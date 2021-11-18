package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface TempUserRepository.
 */
public interface TempUserRepository extends JpaRepository<TempUser, Long> {

  @Query("select temp from TempUser temp where temp.email = :email and temp.dataflowId = :dataflowid and temp.dataProviderId = :dataproviderid")
  TempUser findTempUserByAccountAndDataflow(@Param("email") String email,
      @Param("dataflowid") Long dataflowId, @Param("dataproviderid") Long dataproviderId);

  @Query("select temp from TempUser temp where temp.role = :role and temp.dataflowId = :dataflowid and temp.dataProviderId = :dataproviderid")
  List<TempUser> findTempUserByRoleAndDataflow(@Param("role") String role,
      @Param("dataflowid") Long dataflowId, @Param("dataproviderid") Long dataproviderId);

}
