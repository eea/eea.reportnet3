package org.eea.validation.persistence.data.repository;

import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.persistence.data.domain.Validation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * The Class ValidationRepositoryPaginatedImpl.
 */
public class ValidationRepositoryPaginatedImpl implements ValidationRepositoryPaginated {


  /**
   * The entity manager.
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Find all records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param originsFilter the origins filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   * @return the page
   */
  @Override
  public Page<Validation> findAllRecordsByFilter(Long datasetId,
      List<TypeErrorEnum> levelErrorsFilter, List<TypeEntityEnum> typeEntitiesFilter,
      String originsFilter, Pageable pageable, String headerField, Boolean asc) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select v  from Validation v  where v.idRule is not null ";
    String partLevelError = "";
    String partTypeEntities = "";
    String partOriginsFilter = "";
    partLevelError = levelErrorFilter(levelErrorsFilter, partLevelError);
    partTypeEntities = typeEntities(typeEntitiesFilter, partTypeEntities);
    partOriginsFilter = originFilter(originsFilter, partOriginsFilter);

    String orderPart = "";
    if (!StringUtils.isBlank(headerField)) {
      String byDescAsc = " desc";
      if (Boolean.TRUE == asc) {
        byDescAsc = " asc";
      }
      orderPart = "order by " + headerField + byDescAsc;
    }
    String FINAL_QUERY =
        QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partOriginsFilter + orderPart;

    Query query = entityManager.createQuery(FINAL_QUERY);

    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());
    List<Validation> validationList = query.getResultList();
    Page<Validation> page = new PageImpl<>(validationList);
    return page;
  }

  /**
   * Count records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param originsFilter the origins filter
   * @return the long
   */
  @Override
  public Long countRecordsByFilter(Long datasetId, List<TypeErrorEnum> levelErrorsFilter,
      List<TypeEntityEnum> typeEntitiesFilter, String originsFilter) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select count(v)  from Validation v  where v.idRule is not null ";
    String partLevelError = "";
    String partTypeEntities = "";
    String partOriginsFilter = "";
    partLevelError = levelErrorFilter(levelErrorsFilter, partLevelError);
    partTypeEntities = typeEntities(typeEntitiesFilter, partTypeEntities);
    partOriginsFilter = originFilter(originsFilter, partOriginsFilter);

    String FINAL_QUERY = QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partOriginsFilter;

    Query query = entityManager.createQuery(FINAL_QUERY);
    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Origin filter.
   *
   * @param originsFilter the origins filter
   * @param partOriginsFilter the part origins filter
   * @return the string
   */
  private String originFilter(String originsFilter, String partOriginsFilter) {
    if (!StringUtils.isBlank(originsFilter)) {
      List<String> originsFilterList = Arrays.asList(originsFilter.split(","));
      partOriginsFilter =
          " and v.originName in( Select v.originName from Validation v where v.idRule is not  null ";
      for (int i = 0; i < originsFilterList.size(); i++) {
        partOriginsFilter =
            partOriginsFilter + " and v.originName  !='" + originsFilterList.get(i) + "' ";
      }
      partOriginsFilter = partOriginsFilter + ") ";
    }
    return partOriginsFilter;
  }

  /**
   * Type entities.
   *
   * @param typeEntitiesFilter the type entities filter
   * @param partTypeEntities the part type entities
   * @return the string
   */
  private String typeEntities(List<TypeEntityEnum> typeEntitiesFilter, String partTypeEntities) {
    if (null != typeEntitiesFilter && !typeEntitiesFilter.isEmpty()) {
      partTypeEntities =
          "and v.typeEntity in (Select v.typeEntity from Validation v where v.idRule is not null ";
      for (int i = 0; i < typeEntitiesFilter.size(); i++) {
        partTypeEntities = partTypeEntities + " and v.typeEntity !='"
            + typeEntitiesFilter.get(i).getValue() + "' ";
      }

      partTypeEntities = partTypeEntities + ") ";
    }
    return partTypeEntities;
  }

  /**
   * Level error filter.
   *
   * @param levelErrorsFilter the level errors filter
   * @param partLevelError the part level error
   * @return the string
   */
  private String levelErrorFilter(List<TypeErrorEnum> levelErrorsFilter, String partLevelError) {
    if (null != levelErrorsFilter && !levelErrorsFilter.isEmpty()) {
      partLevelError =
          " and v.levelError in ( select v.levelError from Validation v where v.idRule is not null ";
      for (int i = 0; i < levelErrorsFilter.size(); i++) {
        partLevelError =
            partLevelError + " and v.levelError !='" + levelErrorsFilter.get(i).getValue() + "' ";
      }
      partLevelError = partLevelError + ") ";
    }
    return partLevelError;
  }

}
