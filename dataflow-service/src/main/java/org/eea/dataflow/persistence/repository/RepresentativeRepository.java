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
  List<Representative> findAllByDataflow_Id(Long dataflowId);

  /**
   * Exists by user mail.
   *
   * @param dataProviderId the data provider id
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Query("SELECT r from Representative r WHERE (r.dataProvider.id = :dataProviderId AND r.dataflow.id= :dataflowId)")
  Optional<List<Representative>> findByDataProviderIdAndDataflowId(
      @Param("dataProviderId") Long dataProviderId, @Param("dataflowId") Long dataflowId);

  /**
   * Find by dataflow id and email.
   *
   * @param dataflowId the dataflow id
   * @param email the email
   * @return the list
   */
  @Query("SELECT r from Representative r WHERE (r.userMail = :email AND r.dataflow.id= :dataflowId)")
  List<Representative> findByDataflowIdAndEmail(@Param("dataflowId") Long dataflowId,
      @Param("email") String email);


  boolean existsByDataflow_IdAndDataProvider_IdAndUserMail(Long dataflowId, Long dataProviderId,
      String userMail);
}
