package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository
    extends JpaRepository<Dataflow, Long>, DataflowExtendedRepository {

  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  List<Dataflow> findByStatus(TypeStatusEnum status);

  /**
   * Find by name ignore case.
   *
   * @param name the name
   * @return the optional
   */
  Optional<Dataflow> findByNameIgnoreCase(String name);



  /**
   * Find dataflow by weblinks id.
   *
   * @param idLink the id link
   * @return the dataflow
   */
  Dataflow findDataflowByWeblinks_Id(Long idLink);

  /**
   * Delete a single dataflow record using native query.
   *
   * @param idDataflow the id dataflow
   */
  @Transactional
  @Modifying
  @CacheEvict(value = "dataflowVO", key = "#idDataflow")
  @Query(nativeQuery = true, value = "delete from  dataflow  where id = :idDataflow ")
  void deleteNativeDataflow(@Param("idDataflow") Long idDataflow);

  /**
   * Delete by id.
   *
   * @param idDataflow the id dataflow
   */
  @Override
  @Transactional
  @Modifying
  @CacheEvict(value = "dataflowVO", key = "#idDataflow")
  @Query("DELETE FROM Dataflow d where d.id = :idDataflow")
  void deleteById(@Param("idDataflow") Long idDataflow);


  /**
   * Find by id in order by status desc creation date desc.
   *
   * @param ids the ids
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='REPORTING' and df.id IN :ids order by status, creationDate desc")
  List<Dataflow> findByIdInOrderByStatusDescCreationDateDesc(@Param("ids") List<Long> ids);

  /**
   * Find in order by status desc creation date desc.
   *
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='REPORTING' order by status, creationDate desc")
  List<Dataflow> findInOrderByStatusDescCreationDateDesc();


  /**
   * Find reference by id in order by status desc creation date desc.
   *
   * @param status the status
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='REFERENCE' and df.status=:status order by status, creationDate desc")
  List<Dataflow> findReferenceByStatusInOrderByStatusDescCreationDateDesc(
      @Param("status") TypeStatusEnum status);


  /**
   * Find reference by status and id in order by status desc creation date desc.
   *
   * @param status the status
   * @param ids the ids
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='REFERENCE' and df.status=:status and df.id IN :ids order by status, creationDate desc")
  List<Dataflow> findReferenceByStatusAndIdInOrderByStatusDescCreationDateDesc(
      @Param("status") TypeStatusEnum status, @Param("ids") List<Long> ids);


  /**
   * Find business in order by status desc creation date desc.
   *
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='BUSINESS' order by status, creationDate desc")
  List<Dataflow> findBusinessInOrderByStatusDescCreationDateDesc();


  /**
   * Find business and id in order by status desc creation date desc.
   *
   * @param ids the ids
   * @return the list
   */
  @Modifying
  @Query("select df from Dataflow df where df.type='BUSINESS' and df.id IN :ids order by status, creationDate desc")
  List<Dataflow> findBusinessAndIdInOrderByStatusDescCreationDateDesc(@Param("ids") List<Long> ids);



  /**
   * Gets the datasets status.
   *
   * @param datasetIds the dataset ids
   * @return the datasets status
   */
  @Query(nativeQuery = true,
      value = "select  df.id as id ,ds.status as status from dataflow df join dataset ds on df.id = ds.dataflowid where ds.id IN :datasetIds")
  List<IDatasetStatus> getDatasetsStatus(@Param("datasetIds") List<Long> datasetIds);


  /**
   * Find by available true.
   *
   * @return the list
   */
  List<Dataflow> findByShowPublicInfoTrue();

  /**
   * Find public dataflows by country code.
   *
   * @param countryCode the country code
   * @return the list
   */
  @Query("select r.dataflow from Representative r where r.dataflow.showPublicInfo= true and r.dataProvider.code= :countryCode and r.dataflow.status='DRAFT' and r.hasDatasets=true")
  List<Dataflow> findPublicDataflowsByCountryCode(@Param("countryCode") String countryCode);

  /**
   * Find dataflows by dataprovider ids and dataflow ids.
   *
   * @param dataflowIds the dataflow ids
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  @Query("select r.dataflow from Representative r where r.dataflow.id IN (:dataflowIds) and r.dataProvider.id IN(:dataProviderIds) ")
  List<Dataflow> findDataflowsByDataproviderIdsAndDataflowIds(
      @Param("dataflowIds") List<Long> dataflowIds,
      @Param("dataProviderIds") List<Long> dataProviderIds);

  /**
   * Find by id and available true.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow
   */
  Dataflow findByIdAndShowPublicInfoTrue(Long dataflowId);

  /**
   * Update public status.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  @Modifying
  @Transactional
  @CacheEvict(value = "dataflowVO", key = "#dataflowId")
  @Query(nativeQuery = true,
      value = "update dataflow set show_public_info = :showPublicInfo where id = :dataflowId")
  void updatePublicStatus(@Param("dataflowId") Long dataflowId,
      @Param("showPublicInfo") boolean showPublicInfo);

  /**
   * The Interface IDatasetStatus.
   */
  public interface IDatasetStatus {

    /**
     * Gets the id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Gets the status.
     *
     * @return the status
     */
    String getStatus();
  }



}
