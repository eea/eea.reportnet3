package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.Validation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The Interface ValidationRepositoryPaginated.
 */
public interface ValidationRepositoryPaginated {



  /**
   * Find all records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   * @return the page
   */
  Page<Validation> findAllRecordsByFilter(Long datasetId, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter,
      Pageable pageable, String headerField, Boolean asc);

  /**
   * Count records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the long
   */
  Long countRecordsByFilter(Long datasetId, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter);


  /**
   * Find group records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   * @param paged the paged
   * @return the list
   */
  List<GroupValidationVO> findGroupRecordsByFilter(Long datasetId,
      List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
      String tableFilter, String fieldValueFilter, Pageable pageable, String headerField,
      Boolean asc, boolean paged);
}
