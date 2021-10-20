package org.eea.validation.persistence.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

  private static final String TABLE = "table";
  private static final String FIELD = "field";

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
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   *
   * @return the page
   */
  @Override
  public Page<Validation> findAllRecordsByFilter(Long datasetId,
      List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
      String tableFilter, String fieldValueFilter, Pageable pageable, String headerField,
      Boolean asc) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select v  from Validation v  where v.idRule is not null ";
    String partLevelError = levelErrorFilter(levelErrorsFilter);
    String partTypeEntities = typeEntities(typeEntitiesFilter);
    String partTableFilter = originFilter(tableFilter, TABLE);
    String partFieldFilter = originFilter(fieldValueFilter, FIELD);
    String orderPart = addOrderBy(headerField, asc);

    String FINAL_QUERY = QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partTableFilter
        + partFieldFilter + orderPart;

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
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   * @param paged the paged
   *
   * @return the list
   */
  @Transactional
  @Override
  public List<GroupValidationVO> findGroupRecordsByFilter(Long datasetId,
      List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
      String tableFilter, String fieldValueFilter, Pageable pageable, String headerField,
      Boolean asc, boolean paged) {
    Session session = (Session) entityManager.getDelegate();
    String basicQuery = String.format("select * from("
        + "select v.id_rule as idRule, v.level_error as levelError, v.type_entity as typeEntity, v.table_name as tableName, v.short_code as shortCode, v.field_name as fieldName, '' as message, count(*) as numberOfRecords from dataset_%s.Validation v"
        + "  where v.id is not null and type_entity != 'TABLE'  group by v.level_error, v.id_rule, v.type_entity, v.table_name, v.short_code, v.field_name "
        + "union "
        + "select v.id_rule as idRule, v.level_error as levelError, v.type_entity as typeEntity, v.table_name as tableName, v.short_code as shortCode, v.field_name as fieldName, v.message, count(*) as numberOfRecords from dataset_%s.Validation v"
        + "  where v.id is not null and type_entity not in ('RECORD', 'FIELD') group by v.level_error, v.id_rule, v.type_entity, v.table_name, v.short_code, v.field_name, v.message"
        + " ) tableaux where idRule is not null ", datasetId, datasetId);
    String partLevelError = levelErrorFilter(levelErrorsFilter);
    String partTypeEntities = typeEntities(typeEntitiesFilter);
    String partTableFilter = originFilter(tableFilter, TABLE);
    String partFieldFilter = originFilter(fieldValueFilter, FIELD);
    String orderPart = addOrderBy(headerField, asc);
    String page =
        paged ? " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset() : "";

    String finalQuery = basicQuery + partLevelError + partTypeEntities + partTableFilter
        + partFieldFilter + orderPart + page;
    System.out.println(finalQuery);
    return session.doReturningWork(new ReturningWork<List<GroupValidationVO>>() {
      @Override
      public List<GroupValidationVO> execute(Connection conn) throws SQLException {
        List<GroupValidationVO> groupsValidations = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(finalQuery);
            ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            GroupValidationVO validation = new GroupValidationVO();
            validation.setIdRule(rs.getString("idRule"));
            validation.setLevelError(ErrorTypeEnum.valueOf(rs.getString("levelError")));
            validation.setTypeEntity(EntityTypeEnum.valueOf(rs.getString("typeEntity")));
            validation.setNumberOfRecords(rs.getInt("numberOfRecords"));
            validation.setNameTableSchema(rs.getString("tableName"));
            validation.setShortCode(rs.getString("shortCode"));
            validation.setNameFieldSchema(rs.getString("fieldName"));
            validation.setMessage(rs.getString("message"));
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
   * @return the string
   */
  private String addOrderBy(String headerField, Boolean asc) {
    return StringUtils.isBlank(headerField) ? ""
        : "order by " + headerField + (asc ? " asc" : " desc");
  }

  /**
   * Count records by filter.
   *
   * @param datasetId the dataset id
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   *
   * @return the long
   */
  @Override
  public Long countRecordsByFilter(Long datasetId, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    TenantResolver.getTenantName();
    String QUERY_FILTER_BASIC = "select count(v)  from Validation v  where v.idRule is not null ";
    String partLevelError = levelErrorFilter(levelErrorsFilter);
    String partTypeEntities = typeEntities(typeEntitiesFilter);
    String partTableFilter = originFilter(tableFilter, TABLE);
    String partFieldFilter = originFilter(fieldValueFilter, FIELD);

    String FINAL_QUERY =
        QUERY_FILTER_BASIC + partLevelError + partTypeEntities + partTableFilter + partFieldFilter;

    Query query = entityManager.createQuery(FINAL_QUERY);
    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Origin filter.
   *
   * @param originsFilter the origins filter
   * @param entity the entity
   * @return the string
   */
  private String originFilter(String originsFilter, String entity) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (!StringUtils.isBlank(originsFilter)) {
      stringBuilder.append(" and " + entity + "Name  in ").append(composeListQuery(originsFilter));
    }
    return stringBuilder.toString();
  }

  /**
   * Compose list query.
   *
   * @param originsFilter the origins filter
   *
   * @return the string
   */
  private String composeListQuery(String originsFilter) {
    return "('" + originsFilter.replace(",", "','") + "')";
  }

  /**
   * Removes the spaces generated with automatic toString.
   *
   * @param listFormatted the list formatted
   *
   * @return the string
   */
  private String removeSpacesEnum(String listFormatted) {
    return listFormatted.replace(", ", ",").replace("[", "").replace("]", "");
  }

  /**
   * Type entities.
   *
   * @param typeEntitiesFilter the type entities filter
   * @return the string
   */
  private String typeEntities(List<EntityTypeEnum> typeEntitiesFilter) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (null != typeEntitiesFilter && !typeEntitiesFilter.isEmpty()) {
      stringBuilder.append(" and typeEntity in ")
          .append(composeListQuery(removeSpacesEnum(typeEntitiesFilter.toString())));
    }
    return stringBuilder.toString();
  }

  /**
   * Level error filter.
   *
   * @param levelErrorsFilter the level errors filter
   * @return the string
   */
  private String levelErrorFilter(List<ErrorTypeEnum> levelErrorsFilter) {
    StringBuilder stringBuilder = new StringBuilder("");
    if (null != levelErrorsFilter && !levelErrorsFilter.isEmpty()) {
      stringBuilder.append(" and levelError in ")
          .append(composeListQuery(removeSpacesEnum(levelErrorsFilter.toString())));
    }
    return stringBuilder.toString();
  }

}
