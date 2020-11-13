package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
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
   * Find pending accepted.
   *
   * @param userIdRequester the user id requester
   * @return the list
   */
  @Query("SELECT df as dataflow, ur.requestType as typeRequestEnum, ur.id as requestId from Dataflow df "
      + "JOIN df.userRequests ur WHERE ur.requestType in ('PENDING') "
      + " AND ur.userRequester = :idRequester AND df.status not in ('COMPLETED') ORDER BY df.deadlineDate ASC")
  List<DataflowWithRequestType> findPending(@Param("idRequester") String userIdRequester);



  /**
   * Find by status and user requester.
   *
   * @param typeRequest the type request
   * @param userIdRequester the user id requester
   * @return the list
   */
  @Query("SELECT df from Dataflow df JOIN df.userRequests ur WHERE ur.requestType = :type "
      + " AND ur.userRequester = :idRequester ORDER BY df.deadlineDate ASC")
  List<Dataflow> findByStatusAndUserRequester(@Param("type") TypeRequestEnum typeRequest,
      @Param("idRequester") String userIdRequester);


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
   * The Interface IDatasetStatus.
   */
  public interface IDatasetStatus {
    Long getId();

    String getStatus();
  }


}
