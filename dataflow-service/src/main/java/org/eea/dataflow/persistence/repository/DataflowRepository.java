package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
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
  @Query("DELETE FROM Dataflow d where d.id = :idDataflow")
  void deleteById(@Param("idDataflow") Long idDataflow);


  /**
   * Find by id in order by status desc creation date desc.
   *
   * @param ids the ids
   * @return the list
   */
  List<Dataflow> findByIdInOrderByStatusDescCreationDateDesc(List<Long> ids);

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
   * Find by id and available true.
   *
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
  @Query(nativeQuery = true,
      value = "update dataflow set show_public_info = :showPublicInfo where id = :dataflowId")
  void updatePublicStatus(@Param("dataflowId") Long dataflowId,
      @Param("showPublicInfo") boolean showPublicInfo);

  /**
   * The Interface IDatasetStatus.
   */
  public interface IDatasetStatus {
    Long getId();

    String getStatus();
  }



}
