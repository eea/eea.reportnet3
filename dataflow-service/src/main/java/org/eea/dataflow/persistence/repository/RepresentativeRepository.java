package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataflow.persistence.domain.Representative;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** The Interface RepresentativeRepository. */
public interface RepresentativeRepository extends CrudRepository<Representative, Long> {

  /**
   * Find all by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Query("SELECT distinct r from Representative r JOIN FETCH r.reporters rep WHERE r.dataflow.id= :dataflowId")
  List<Representative> findAllByDataflow_Id(@Param("dataflowId") Long dataflowId);

  /**
   * Exists by user mail.
   *
   * @param dataProviderId the data provider id
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Query("SELECT r from Representative r JOIN FETCH r.reporters WHERE r.dataProvider.id = :dataProviderId AND r.dataflow.id= :dataflowId")
  Optional<List<Representative>> findByDataProviderIdAndDataflowId(
      @Param("dataProviderId") Long dataProviderId, @Param("dataflowId") Long dataflowId);

  /**
   * Find by dataflow id and email.
   *
   * @param dataflowId the dataflow id
   * @param email the email
   * @return the list
   */
  @Query("SELECT r from Representative r JOIN FETCH r.reporters rep WHERE rep.userMail= :email AND r.dataflow.id= :dataflowId")
  List<Representative> findByDataflowIdAndEmail(@Param("dataflowId") Long dataflowId,
      @Param("email") String email);

  @Query(" SELECT count (distinct r )>0  from Representative r JOIN FETCH r.reporters rep WHERE r.dataflow.id= :dataflowId and r.dataProvider.id = :dataProviderId and rep.userMail= :userMail")
  boolean existsByDataflowIdAndDataProviderIdAndUserMail(Long dataflowId, Long dataProviderId,
      String userMail);
}
