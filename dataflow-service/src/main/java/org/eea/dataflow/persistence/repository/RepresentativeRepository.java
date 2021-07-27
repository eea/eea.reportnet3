package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.Representative;
import org.springframework.data.jpa.repository.Modifying;
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
  @Query("SELECT distinct r from Representative r left JOIN FETCH r.leadReporters rep WHERE r.dataflow.id= :dataflowId")
  List<Representative> findAllByDataflow_Id(@Param("dataflowId") Long dataflowId);

  /**
   * Exists by user mail.
   *
   * @param dataProviderId the data provider id
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Query("SELECT distinct r from Representative r left JOIN FETCH r.leadReporters WHERE r.dataProvider.id = :dataProviderId AND r.dataflow.id= :dataflowId")
  Optional<List<Representative>> findByDataProviderIdAndDataflowId(
      @Param("dataProviderId") Long dataProviderId, @Param("dataflowId") Long dataflowId);

  /**
   * Find by dataflow id and email.
   *
   * @param dataflowId the dataflow id
   * @param email the email
   * @return the list
   */
  @Query("SELECT distinct r from Representative r left JOIN FETCH r.leadReporters rep WHERE rep.email= :email AND r.dataflow.id= :dataflowId")
  List<Representative> findByDataflowIdAndEmail(@Param("dataflowId") Long dataflowId,
      @Param("email") String email);


  /**
   * Find one by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the representative
   */
  Representative findOneByDataflow_IdAndDataProvider_Id(Long dataflowId, Long dataProviderId);

  /**
   * Exists by dataflow id and data provider id and user mail.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param email the email
   * @return true, if successful
   */
  @Query("SELECT distinct r from Representative r left JOIN FETCH r.leadReporters rep WHERE rep.email= :email AND  r.dataProvider.id= :dataProviderId AND r.dataflow.id= :dataflowId")
  Representative findOneByDataflowIdAndDataProviderIdUserMail(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId, @Param("email") String email);


  /**
   * Update representative visibility restrictions.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true,
      value = "update representative set restrict_from_public = :restrictFromPublic where dataflow_id = :dataflowId and data_provider_id = :dataProviderId ")
  void updateRepresentativeVisibilityRestrictions(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId,
      @Param("restrictFromPublic") boolean restrictFromPublic);


  /**
   * Find by dataflow and data provider id in.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderIdList the data provider id list
   * @return the list
   */
  List<Representative> findByDataflowIdAndDataProviderIdIn(Long dataflowId,
      List<Long> dataProviderIdList);

}
