package org.eea.dataset.service.impl;

import org.bson.Document;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.DremioRecordMapper;
import org.eea.dataset.mapper.DremioValidationMapper;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DataLakeService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.validation.DremioValidationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ImportDataLakeCommons
@Service
public class DataLakeServiceImpl implements DataLakeService {

    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private JdbcTemplate dremioJdbcTemplate;
    private FileCommonUtils fileCommon;
    private S3Service s3Service;
    private SchemasRepository schemasRepository;

    @Autowired
    public DataLakeServiceImpl(DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate,
                               FileCommonUtils fileCommon, S3Service s3Service, SchemasRepository schemasRepository) {
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.fileCommon = fileCommon;
        this.s3Service = s3Service;
        this.schemasRepository = schemasRepository;
    }

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeServiceImpl.class);

    @Override
    @Transactional
    public TableVO getTableValuesDLById(final Long datasetId, final String idTableSchema,
                                        Pageable pageable, final String fields, ErrorTypeEnum[] levelError, String[] idRules,
                                        String fieldSchema, String fieldValue) throws EEAException {
        TableVO result = new TableVO();
        StringBuilder dataCountQuery = new StringBuilder();
        DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        String datasetSchemaId = dataset.getDatasetSchema();
        Map<String, FieldSchemaVO> fieldIdMap;
        TableSchemaVO tableSchemaVO = getTableSchemaVO(idTableSchema, datasetSchemaId);

        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, datasetId, tableSchemaVO.getNameTableSchema());
        dataCountQuery.append("select count(*) from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, false));
        Long totalRecords = dremioJdbcTemplate.queryForObject(dataCountQuery.toString(), Long.class);
        if (totalRecords == 0) {
            result.setTotalFilteredRecords(0L);
            result.setTableValidations(new ArrayList<>());
            result.setTotalRecords(0L);
            result.setRecords(new ArrayList<>());
        } else {
            pageable = calculatePageable(pageable, totalRecords);
            fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            DremioRecordMapper recordMapper = new DremioRecordMapper();
            recordMapper.setRecordSchemaVO(tableSchemaVO.getRecordSchema()).setDatasetSchemaId(datasetSchemaId).setTableSchemaId(idTableSchema);
            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, false));
            //filter value
            if (!fieldValue.equals("")) {
                buildFilterQuery(fieldValue, fieldIdMap, dataQuery);
            }
            //sorting
            if (fields!=null) {
                buildSortQuery(fields, datasetSchemaId, fieldIdMap, dataQuery);
            }
            //pagination
            if (pageable!=null) {
                buildPaginationQuery(pageable, dataQuery);
            }
            List<RecordVO> recordVOS = dremioJdbcTemplate.query(dataQuery.toString(), recordMapper);
            result.setIdTableSchema(idTableSchema);
            result.setRecords(recordVOS);
            result.setTotalFilteredRecords(Long.valueOf(recordVOS.size()));
            retrieveValidations(recordVOS, s3PathResolver);
        }
        result.setTotalRecords(totalRecords);
        return result;
    }

    private void retrieveValidations(List<RecordVO> recordVOS, S3PathResolver s3PathResolver) {
        StringBuilder validationQuery = new StringBuilder();
        validationQuery.append("select * from " + s3Service.getTableAsFolderQueryPath(s3PathResolver, true));
        List<DremioValidationVO> dremioValidationsVOS = dremioJdbcTemplate.query(validationQuery.toString(), new DremioValidationMapper());
        for (DremioValidationVO dv : dremioValidationsVOS) {
           List<RecordVO> valRecords =  recordVOS.stream().filter(recordVO -> recordVO.getId().equals(dv.getRecordId())).collect(Collectors.toList());
           valRecords.forEach(vr -> vr.getFields().parallelStream().forEach(fieldVO -> {
               if (fieldVO.getName().equals(dv.getFieldName())) {
                   FieldValidationVO fieldValidationVO = new FieldValidationVO();
                   List<FieldValidationVO> fieldValidations = new ArrayList<>();
                   ValidationVO validationVO = new ValidationVO();
                   validationVO.setId(dv.getPk());
                   validationVO.setLevelError(dv.getValidationLevel());
                   validationVO.setMessage(dv.getMessage());
                   validationVO.setTypeEntity(dv.getValidationArea());
                   fieldValidationVO.setValidation(validationVO);
                   fieldValidations.add(fieldValidationVO);
                   fieldVO.setFieldValidations(fieldValidations);
               }
           }));
        }
    }

    private static void buildPaginationQuery(Pageable pageable, StringBuilder dataQuery) {
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

    private void buildSortQuery(String fields, String datasetSchemaId, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        String[] sort = fields.split(":");
        String sortFieldId = sort[0];
        FieldSchemaVO sortField = fieldIdMap.get(sortFieldId);
        if (DataType.LINK.equals(sortField.getType())
                || DataType.EXTERNAL_LINK.equals(sortField.getType())) {
            Document documentField =
                    schemasRepository.findFieldSchema(datasetSchemaId, sortField.getName());
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
                dataQuery.append(" order by CAST(").append(sortField.getName()).append(" as DECIMAL) ");
                break;
            case DATE:
                dataQuery.append(" order by CAST(").append(sortField.getName()).append(" as DATE) ");
                break;
            default:
                dataQuery.append(" order by ").append(sortField.getName());
                break;
        }
        dataQuery.append(sort[1].equals("1") ? "asc" : "desc");
    }

    private static void buildFilterQuery(String fieldValue, Map<String, FieldSchemaVO> fieldIdMap, StringBuilder dataQuery) {
        dataQuery.append(" where ");
        List<String> headers = fieldIdMap.values().stream().map(fieldSchemaVO -> fieldSchemaVO.getName()).collect(Collectors.toList());
        dataQuery.append(headers.get(0)).append(" like '%").append(fieldValue).append("%'");
        headers.remove(headers.get(0));
        headers.forEach(header -> dataQuery.append(" OR ").append(header).append(" like '%").append(fieldValue).append("%'"));
    }

    private TableSchemaVO getTableSchemaVO(String idTableSchema, String datasetSchemaId) throws EEAException {
        DataSetSchemaVO dataSetSchemaVO;
        try {
            dataSetSchemaVO = fileCommon.getDataSetSchemaVO(datasetSchemaId);
        } catch (EEAException e) {
            LOG.error("Error retrieving dataset schema for datasetSchemaId {}", datasetSchemaId);
            throw new EEAException(e);
        }
        Optional<TableSchemaVO> tableSchemaOptional = dataSetSchemaVO.getTableSchemas().stream().filter(t -> t.getIdTableSchema().equals(idTableSchema)).findFirst();
        TableSchemaVO tableSchemaVO = null;
        if (!tableSchemaOptional.isPresent()) {
            LOG.error("table with id " + idTableSchema + " not found in mongo results");
            throw new EEAException("Error retrieving table with id " + idTableSchema);
        }
        tableSchemaVO = tableSchemaOptional.get();
        return tableSchemaVO;
    }

    private Pageable calculatePageable(Pageable pageable, Long totalRecords) {
        if (pageable == null && totalRecords > 0) {
            pageable = PageRequest.of(0, totalRecords.intValue());
        }
        return pageable;
    }
}
