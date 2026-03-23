package org.eea.dataset.service.impl;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.service.DataLakeDataRetriever;
import org.eea.dataset.service.DremioAutoPromotionService;
import org.eea.dataset.util.DataLakeDataRetrieverUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_EU_QUERY_PATH;

@Service
public class EuDatasetDataRetrieverDL implements DataLakeDataRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(EuDatasetDataRetrieverDL.class);
    private S3Service s3Service;
    private S3Helper s3Helper;
    private JdbcTemplate dremioJdbcTemplate;
    private DremioHelperService dremioHelperService;
    private DremioAutoPromotionService dremioAutoPromotionService;

    @Autowired
    public EuDatasetDataRetrieverDL(S3Service s3Service, S3Helper s3Helper, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, DremioHelperService dremioHelperService, DremioAutoPromotionService dremioAutoPromotionService) {
        this.s3Service = s3Service;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.dremioHelperService = dremioHelperService;
        this.dremioAutoPromotionService = dremioAutoPromotionService;
    }


    @Override
    public TableVO getTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldSchemaId,
                                  String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) throws EEAException {
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();

        // ROOT resolver for demoting/promoting.
        S3PathResolver s3RootResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema(), false, null);
        s3RootResolver.setIsIcebergTable(false);

        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3RootResolver);

        if (folderExist) {
            // Try to auto promote if it’s safe and not already promoted.
            dremioAutoPromotionService.ensureSafeFolderPromotion(dataset, s3RootResolver);

            if (dremioHelperService.checkFolderPromoted(s3RootResolver, s3RootResolver.getTableName())) {
                // Path resolver for dremio sql.
                S3PathResolver s3QueryResolver = new S3PathResolver(dataset.getDataflowId(), datasetId, tableSchemaVO.getNameTableSchema(), S3_TABLE_NAME_EU_QUERY_PATH);
                s3QueryResolver.setIsIcebergTable(false);

                loadTableWithRetry(dataset, tableSchemaVO, pageable, result, s3RootResolver, s3QueryResolver, fields,
                    fieldSchemaId, fieldValue, levelError, qcCodes);
            } else {
                setEmptyResults(result);
            }
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    @Override
    public TableVO getPreparationTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldSchemaId, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes, String preparationCode) throws EEAException {
        return null; // no preparation for eu dataset
    }

    /**
     * Helper that makes a second try to fetch the table data if it fails the first time.
     */
    private void loadTableWithRetry(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result, S3PathResolver s3RootResolver,
                                    S3PathResolver s3QueryResolver, String fields, String fieldSchemaId, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) {
        String tablePathForRefresh = s3Service.getTableAsFolderQueryPath(s3QueryResolver);

        try {
            // First attempt to get the table results.
            executeEuDatasetQuery(dataset, tableSchemaVO, pageable, result, s3QueryResolver, fields, fieldSchemaId, fieldValue, levelError, qcCodes);
        } catch (Exception e) {
            LOG.warn("First attempt to retrieve table data failed for datasetId {} table {}. Error: {}", dataset.getId(), tableSchemaVO.getNameTableSchema(), e.getMessage(), e);
            LOG.info("Trying to demote, refresh metadata, promote and retry retrieve table data once more.");
            dremioAutoPromotionService.demoteAndRefreshMetadataAndPromote(dataset, tablePathForRefresh, s3RootResolver);
            // Second and last try to get the table results.
            executeEuDatasetQuery(dataset, tableSchemaVO, pageable, result, s3QueryResolver, fields, fieldSchemaId, fieldValue, levelError, qcCodes);
        }
    }

    /**
     * Core Dremio queries: filtered count + pagination + data retrieval.
     */
    private void executeEuDatasetQuery(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, TableVO result, S3PathResolver s3QueryResolver,
                                       String fields, String fieldSchemaId, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) {
        Long totalRecords;

        // Make sure path is the EU dataset table path.
        s3QueryResolver.setPath(S3_TABLE_NAME_EU_QUERY_PATH);

        // Total records.
        totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.getRecordsCountQuery(s3QueryResolver), Long.class);
        result.setTotalRecords(totalRecords);

        Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream()
            .collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
        FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
        fieldSchemaProviderCode.setName("data_provider_code");
        fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);

        StringBuilder filteredQuery = DataLakeDataRetrieverUtils.buildFilteredQuery(
            dataset, fields, fieldSchemaId, fieldValue, fieldIdMap, levelError, qcCodes, null);

        StringBuilder recordsCountQuery = new StringBuilder();
        recordsCountQuery
            .append("select count(record_id) from ")
            .append(s3Service.getTableDCAsFolderQueryPath(s3QueryResolver, S3_TABLE_NAME_EU_QUERY_PATH))
            .append(" t ")
            .append(filteredQuery);

        Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery.toString(), Long.class);
        result.setTotalFilteredRecords(totalFilteredRecords);

        pageable = DataLakeDataRetrieverUtils.calculatePageable(pageable, totalFilteredRecords);
        if (pageable != null) {
            DataLakeDataRetrieverUtils.buildPaginationQuery(pageable, filteredQuery);
        }

        StringBuilder dataQuery = new StringBuilder();
        dataQuery
            .append("select * from ")
            .append(s3Service.getTableAsFolderQueryPath(s3QueryResolver))
            .append(" t ")
            .append(filteredQuery);

        List<RecordVO> recordVOS = DataLakeDataRetrieverUtils.getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
        result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
        result.setRecords(recordVOS);
    }

    @Override
    public boolean isApplicable(String datasetType) {
        return DatasetTypeEnum.EUDATASET.getValue().equals(datasetType);
    }
}
