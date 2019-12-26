package org.eea.dataflow.persistence.repository;

import java.util.List;
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
   * @param userMail the user mail
   * @return true, if successful
   */
  @Query(nativeQuery = true,
      value = "SELECT exists (SELECT * from representative where data_provider_id=:dataProviderId and user_mail=:userMail)")
  boolean existsByDataProviderIdAndUserMail(@Param("dataProviderId") Long dataProviderId,
      @Param("userMail") String userMail);
}
