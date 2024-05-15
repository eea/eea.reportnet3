package org.eea.dataset.util;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DremioRecordMapper;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.impl.SpatialDataHandlingImpl;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataLakeDataRetrieverUtils {

    private static final int MAX_FILTERS = 5;

    private static SchemasRepository schemasRepository;
    private static JdbcTemplate dremioJdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeDataRetrieverUtils.class);

    public DataLakeDataRetrieverUtils(SchemasRepository schemasRepository, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate) {
        this.schemasRepository = schemasRepository;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
    }

    public static Pageable calculatePageable(Pageable pageable, Long totalRecords) {
        if (pageable == null && totalRecords > 0) {
            pageable = PageRequest.of(0, totalRecords.intValue());
        }
        return pageable;
    }

    /**
     * builds query according to pagination options
     * @param pageable
     * @param dataQuery
     */
    public static void buildPaginationQuery(Pageable pageable, StringBuilder dataQuery) {
        int limit;
        int offset;
        limit = pageable.getPageSize();
        offset = pageable.getPageNumber() * pageable.getPageSize();
        if (limit!=0) {
            dataQuery.append(" LIMIT ").append(limit);
        }
        if (offset!=0) {
            dataQuery.append(" OFFSET ").append(offset);
        }
    }

    /**
     * builds query according to sorting options
     * @param fields
     * @param datasetSchemaId
     * @param fieldIdMap
     * @param dataQuery
     */
    public static void buildSortQuery(String fields, String datasetSchemaId, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        String[] sort = fields.split(":");
        String sortFieldId = sort[0];
        FieldSchemaVO sortField = fieldIdMap.get(sortFieldId);
        if (DataType.LINK.equals(sortField.getType())
                || DataType.EXTERNAL_LINK.equals(sortField.getType())) {
            Document documentField =
                    schemasRepository.findFieldSchema(datasetSchemaId, sortField.getId());
            Document documentReference = (Document) documentField.get("referencedField");
            Document documentFieldReferenced =
                    schemasRepository.findFieldSchema(documentReference.get("idDatasetSchema").toString(),
                            documentReference.get("idPk").toString());

            DataType typeData = DataType.valueOf(documentFieldReferenced.get("typeData").toString());
            sortField.setType(typeData);
        }
        switch (sortField.getType()) {
            case NUMBER_INTEGER:
            case NUMBER_DECIMAL:
                dataQuery.append(" order by CASE when ").append(sortField.getName()).append(" like '' THEN 0 ELSE CAST(").append(sortField.getName()).append(" as NUMERIC) END");
                break;
            case DATE:
                dataQuery.append(" order by CASE when ").append(sortField.getName()).append(" like '' THEN '0000-00-00' ELSE CAST(").append(sortField.getName()).append(" as DATE) END");
                break;
            default:
                dataQuery.append(" order by ").append(sortField.getName());
                break;
        }
        dataQuery.append(sort[1].equals("1") ? " asc" : " desc");
    }

    /**
     * builds query according to filtering options
     * @param fieldValue
     * @param fieldIdMap
     * @param dataQuery
     */
    public static void buildFieldValueFilterQuery(String fieldValue, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        dataQuery.append(" where (");
        List<String> headers = fieldIdMap.values().stream().map(FieldSchemaVO::getName).collect(Collectors.toList());
        LOG.info("headers : {}", headers);
        dataQuery.append(headers.get(0)).append(" like '%").append(fieldValue).append("%'");
        LOG.info("headers.get(0) : {}", headers.get(0));
        headers.remove(headers.get(0));
        LOG.info("headers : {}", headers);
        headers.forEach(header -> dataQuery.append(" OR ").append(header).append(" like '%").append(fieldValue).append("%'"));
        dataQuery.append(")");
    }

    public static StringBuilder buildFilteredQuery(DataSetMetabaseVO dataset, String fields, String fieldValue, Map<String, FieldSchemaVO> fieldIdMap,
                                            ErrorTypeEnum[] levelError, String[] qcCodes, String validationTablePath) {
        StringBuilder query = new StringBuilder();
        boolean levelErrorNotEmpty = levelError!=null && levelError.length>0 && levelError.length!=MAX_FILTERS;
        boolean qcCodesNotEmpty = qcCodes!=null && qcCodes.length>0;
        //filter value
        if (!fieldValue.equals("")) {
            buildFieldValueFilterQuery(fieldValue, fieldIdMap, query);
        }
        //filter by levelError
        if (levelErrorNotEmpty && validationTablePath!=null) {
            buildLevelErrorQueryFilter(fieldValue, query, levelError, validationTablePath);
        }
        //filter by qc_code
        if (qcCodesNotEmpty && validationTablePath!=null) {
            buildQcCodeFilterQuery(fieldValue, query, levelErrorNotEmpty, qcCodes, validationTablePath);
        }
        //sorting
        if (fields !=null) {
            buildSortQuery(fields, dataset.getDatasetSchema(), fieldIdMap, query);
        }
        return query;
    }

    /**
     * Builds query for filtering records by qcCodes
     * @param fieldValue
     * @param dataQuery
     * @param levelErrorNotEmpty
     * @param qcCodes
     * @param validationTablePath
     */
    private static void buildQcCodeFilterQuery(String fieldValue, StringBuilder dataQuery, boolean levelErrorNotEmpty, String[] qcCodes, String validationTablePath) {
        List<String> qcCodesList = Arrays.asList(qcCodes);
        String qcCodesValues = qcCodesList.stream() .map(s -> "\'" + s + "\'") .collect(Collectors.joining(", "));
        if (!fieldValue.equals("") || levelErrorNotEmpty) {
            dataQuery.append(" AND ");
        } else {
            dataQuery.append(" WHERE ");
        }
        dataQuery.append("(EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v WHERE t.RECORD_ID=v.RECORD_ID AND QC_CODE IN (").append(qcCodesValues).append(")))");
    }

    /**
     * Builds query for filtering records by level error
     * @param fieldValue
     * @param dataQuery
     * @param levelError
     * @param validationTablePath
     */
    private static void buildLevelErrorQueryFilter(String fieldValue, StringBuilder dataQuery, ErrorTypeEnum[] levelError, String validationTablePath) {
        List<ErrorTypeEnum> levelErrorList = Arrays.asList(levelError);
        String levelErrorValues = levelErrorList.stream().map(s -> "\'" + s + "\'").collect(Collectors.joining(", "));
        if (!fieldValue.equals("")) {
            dataQuery.append(" AND ");
        } else {
            dataQuery.append(" WHERE ");
        }
        if (levelErrorList.size() == 1 && levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
            dataQuery.append("(NOT EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v ").append(" WHERE t.RECORD_ID=v.RECORD_ID))");
        } else {
            dataQuery.append("(EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v WHERE t.RECORD_ID=v.RECORD_ID AND VALIDATION_LEVEL IN (").append(levelErrorValues).append("))");
            if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
                dataQuery.append(" OR NOT EXISTS (SELECT DISTINCT v.RECORD_ID FROM ").append(validationTablePath).append(" v ").append(" WHERE t.RECORD_ID=v.RECORD_ID)");
            }
            dataQuery.append(")");
        }
    }

    public static List<RecordVO> getRecordVOS(String datasetSchema , TableSchemaVO tableSchemaVO, StringBuilder dataQuery) {
        DremioRecordMapper recordMapper = new DremioRecordMapper();
        recordMapper.setRecordSchemaVO(tableSchemaVO.getRecordSchema()).setDatasetSchemaId(datasetSchema).setTableSchemaId(tableSchemaVO.getIdTableSchema());

        SpatialDataHandling spatialDataHandling = new SpatialDataHandlingImpl(tableSchemaVO);

        List<RecordVO> recordVOS;
        if (spatialDataHandling.geoJsonHeadersIsNotEmpty(true)) {
            String newString =  String.format(dataQuery.toString(), spatialDataHandling.getSimpleHeaders(), "," ,spatialDataHandling.convertToJson());
            recordVOS = dremioJdbcTemplate.query(newString, recordMapper);
        } else {
            String newString =  String.format(dataQuery.toString(), spatialDataHandling.getSimpleHeaders(), StringUtils.EMPTY, StringUtils.EMPTY);
            recordVOS = dremioJdbcTemplate.query(newString, recordMapper);
        }
        return recordVOS;
    }
}
