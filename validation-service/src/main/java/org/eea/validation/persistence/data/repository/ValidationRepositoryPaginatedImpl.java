package org.eea.validation.persistence.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.persistence.data.domain.Validation;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
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
  @PersistenceContext(unitName = "dataSetsEntityManagerFactory")
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
   *
   * @return the page
   */
  @Override
  public Page<Validation> findAllRecordsByFilter(Long datasetId,
      List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
      String originsFilter, Pageable pageable, String headerField, Boolean asc) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select v  from Validation v  where v.idRule is not null ";
    String partLevelError = levelErrorFilter(levelErrorsFilter, false);
    String partTypeEntities = typeEntities(typeEntitiesFilter, false);
    String partOriginsFilter = originFilter(originsFilter, false);

    String orderPart = "";
    orderPart = addOrderBy(headerField, asc, orderPart);
    String FINAL_QUERY =
        QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partOriginsFilter + orderPart;

    Query query = entityManager.createQuery(FINAL_QUERY);

    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());
    List<Validation> validationList = query.getResultList();
    return new PageImpl<>(validationList);
  }

  /**
   * Find group records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param originsFilter the origins filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   * @param paged the paged
   * @return the list
   */
  @Transactional
  @Override
  public List<GroupValidationVO> findGroupRecordsByFilter(Long datasetId,
      List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
      String originsFilter, Pageable pageable, String headerField, Boolean asc, boolean paged) {
    Session session = (Session) entityManager.getDelegate();
    String basicQuery = String.format(
        "select v.message as message, v.id_rule as idRule, v.level_error as levelError, v.type_entity as typeEntity,v.origin_name as originName, count(*) as numberOfRecords from dataset_%s.Validation v  where v.id is not null ",
        datasetId);
    String orderPart = "";
    orderPart = addOrderBy(headerField, asc, orderPart);
    String partLevelError = levelErrorFilter(levelErrorsFilter, true);
    String partTypeEntities = typeEntities(typeEntitiesFilter, true);
    String partOriginsFilter = originFilter(originsFilter, true);
    String groupBy = "group by v.message,v.level_error, v.id_rule, v.type_entity,v.origin_name ";
    String page =
        paged ? " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset() : "";
    String finalQuery = basicQuery + partLevelError + partTypeEntities + partOriginsFilter + groupBy
        + orderPart + page;
    return session.doReturningWork(new ReturningWork<List<GroupValidationVO>>() {
      public List<GroupValidationVO> execute(Connection conn) throws SQLException {
        List<GroupValidationVO> groupsValidations = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(finalQuery)) {
          ResultSet rs = stmt.executeQuery();
          while (rs.next()) {
            GroupValidationVO validation = new GroupValidationVO();
            validation.setMessage(rs.getString("message"));
            validation.setIdRule(rs.getString("idRule"));
            validation.setLevelError(ErrorTypeEnum.valueOf(rs.getString("levelError")));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString("typeEntity")));
            validation.setNumberOfRecords(rs.getInt("numberOfRecords"));
            validation.setOriginName(rs.getString("originName"));
            groupsValidations.add(validation);
          }
        }
        return groupsValidations;
      }
    });
  }

  /**
   * Adds the order by.
   *
   * @param headerField the header field
   * @param asc the asc
   * @param orderPart the order part
   * @return the string
   */
  private String addOrderBy(String headerField, Boolean asc, String orderPart) {
    if (!StringUtils.isBlank(headerField)) {
      String byDescAsc = " desc";
      if (asc) {
        byDescAsc = " asc";
      }
      orderPart = "order by " + headerField + byDescAsc;
    }
    return orderPart;
  }

  /**
   * Count records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param originsFilter the origins filter
   *
   * @return the long
   */
  @Override
  public Long countRecordsByFilter(Long datasetId, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String originsFilter) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select count(v)  from Validation v  where v.idRule is not null ";
    String partLevelError = levelErrorFilter(levelErrorsFilter, false);
    String partTypeEntities = typeEntities(typeEntitiesFilter, false);
    String partOriginsFilter = originFilter(originsFilter, false);

    String FINAL_QUERY = QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partOriginsFilter;

    Query query = entityManager.createQuery(FINAL_QUERY);
    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Origin filter.
   *
   * @param originsFilter the origins filter
   * @param group the group
   * @return the string
   */
  private String originFilter(String originsFilter, boolean group) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (!StringUtils.isBlank(originsFilter)) {
      List<String> originsFilterList = Arrays.asList(originsFilter.split(","));
      for (int i = 0; i < originsFilterList.size(); i++) {
        stringBuilder.append(group ? "and v.origin_name <>'" + originsFilterList.get(i) + "' "
            : " and v.originName  !='" + originsFilterList.get(i) + "' ");
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Type entities.
   *
   * @param typeEntitiesFilter the type entities filter
   * @param group the group
   * @return the string
   */
  private String typeEntities(List<EntityTypeEnum> typeEntitiesFilter, boolean group) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (null != typeEntitiesFilter && !typeEntitiesFilter.isEmpty()) {
      for (int i = 0; i < typeEntitiesFilter.size(); i++) {
        stringBuilder.append(group ? " and v.type_entity !='" : " and v.typeEntity !='")
            .append(typeEntitiesFilter.get(i).getValue()).append("' ");
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Level error filter.
   *
   * @param levelErrorsFilter the level errors filter
   *
   * @return the string
   */
  private String levelErrorFilter(List<ErrorTypeEnum> levelErrorsFilter, boolean group) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (null != levelErrorsFilter && !levelErrorsFilter.isEmpty()) {
      for (int i = 0; i < levelErrorsFilter.size(); i++) {
        stringBuilder.append(group ? " and v.level_error !='" : " and v.levelError !='")
            .append(levelErrorsFilter.get(i).getValue()).append("' ");
      }
    }
    return stringBuilder.toString();
  }

}
