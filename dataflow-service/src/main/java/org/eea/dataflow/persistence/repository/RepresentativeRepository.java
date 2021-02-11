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


  /**
   * Find one by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the representative
   */
  @Query
  Representative findOneByDataflow_IdAndDataProvider_Id(Long dataflowId, Long dataProviderId);

  /**
   * Exists by dataflow id and data provider id and user mail.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param userMail the user mail
   * @return true, if successful
   */
  @Query("SELECT distinct r from Representative r JOIN FETCH r.reporters rep WHERE rep.userMail= :userMail AND  r.dataProvider.id= :dataProviderId AND r.dataflow.id= :dataflowId")
  Representative findOneByDataflowIdAndDataProviderIdUserMail(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId, @Param("userMail") String userMail);
}
