package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataflow.persistence.domain.Representative;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface RepresentativeRepository.
 */
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
   * @param userMail the user mail
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Query("SELECT r from Representative r WHERE (r.dataProvider.id = :dataProviderId AND r.userMail= :userMail AND r.dataflow.id= :dataflowId)")
  Optional<List<Representative>> findBydataProviderIdAnduserMailAnddataflowId(
      @Param("dataProviderId") Long dataProviderId, @Param("userMail") String userMail,
      @Param("dataflowId") Long dataflowId);
}
