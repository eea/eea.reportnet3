package org.eea.validation.service.impl;

import org.apache.commons.lang.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.mapper.DremioGroupValidationMapper;
import org.eea.validation.service.DataLakeValidationService;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION;

@ImportDataLakeCommons
@Service
public class DataLakeValidationServiceImpl implements DataLakeValidationService {

    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private ValidationHelper validationHelper;
    private static final String TABLE = "table";
    private static final String FIELD = "field";

    @Autowired
    public DataLakeValidationServiceImpl(JdbcTemplate dremioJdbcTemplate, S3Service s3Service, ValidationHelper validationHelper) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.validationHelper = validationHelper;
    }

    /**
     * finds and groups validation results
     * @param s3PathResolver
     * @param levelErrorsFilter
     * @param typeEntitiesFilter
     * @param tableFilter
     * @param fieldValueFilter
     * @param pageable
     * @param headerField
     * @param asc
     * @param paged
     * @return
     */
    @Override
    public List<GroupValidationVO> findGroupRecordsByFilter(S3PathResolver s3PathResolver, List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
                                                            String tableFilter, String fieldValueFilter, Pageable pageable, String headerField, Boolean asc, boolean paged) {
        s3PathResolver.setTableName(S3_VALIDATION);
        StringBuilder validationQuery = new StringBuilder();
        validationQuery.append("SELECT MIN(v.validation_level) as levelError, MIN(v.validation_area) as typeEntity, MIN(table_name) as tableName, qc_code as shortCode, MIN(field_name) as fieldName, MIN(message) as message, count(*) as numberOfRecords FROM ");
        validationQuery.append(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
        validationQuery.append(" v where v.pk is not null ");
        String partLevelError = levelErrorFilterDL(levelErrorsFilter);
        String partTypeEntities = typeEntitiesDL(typeEntitiesFilter);
        String partTableFilter = originFilterDL(tableFilter, TABLE);
        String partFieldFilter = originFilterDL(fieldValueFilter, FIELD);
        validationQuery.append(partLevelError).append(partTypeEntities).append(partTableFilter).append(partFieldFilter);
        validationQuery.append(" group by v.qc_code, v.table_name, v.field_name ");
        String orderPart = addOrderByDL(headerField, asc);
        validationQuery.append(orderPart);
        String page = paged ? " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset() : "";
        validationQuery.append(page);
        List<GroupValidationVO> groupValidationVOS = dremioJdbcTemplate.query(validationQuery.toString(), new DremioGroupValidationMapper());
        return groupValidationVOS;
    }

    /**
     * Level error filter.
     *
     * @param levelErrorsFilter the level errors filter
     * @return the string
     */
    private String levelErrorFilterDL(List<ErrorTypeEnum> levelErrorsFilter) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (null != levelErrorsFilter && !levelErrorsFilter.isEmpty()) {
            stringBuilder.append(" and validation_level in ")
                    .append(validationHelper.composeListQuery(validationHelper.removeSpacesEnum(levelErrorsFilter.toString())));
        }
        return stringBuilder.toString();
    }

    /**
     * Type entities.
     *
     * @param typeEntitiesFilter the type entities filter
     * @return the string
     */
    private String typeEntitiesDL(List<EntityTypeEnum> typeEntitiesFilter) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (null != typeEntitiesFilter && !typeEntitiesFilter.isEmpty()) {
            stringBuilder.append(" and validation_area in ")
                    .append(validationHelper.composeListQuery(validationHelper.removeSpacesEnum(typeEntitiesFilter.toString())));
        }
        return stringBuilder.toString();
    }

    /**
     * Origin filter.
     *
     * @param originsFilter the origins filter
     * @param entity the entity
     * @return the string
     */
    private String originFilterDL(String originsFilter, String entity) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (!StringUtils.isBlank(originsFilter)) {
            stringBuilder.append(" and " + entity + "_name  in ").append(validationHelper.composeListQuery(originsFilter));
        }
        return stringBuilder.toString();
    }

    /**
     * Adds the order by.
     *
     * @param headerField the header field
     * @param asc the asc
     * @return the string
     */
    private String addOrderByDL(String headerField, Boolean asc) {
        return StringUtils.isBlank(headerField) ? "order by shortCode asc"
                : "order by " + headerField + (asc ? " asc" : " desc");
    }
}




















