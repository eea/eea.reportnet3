package org.eea.dataset.service.impl;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.service.DataLakeDataRetriever;
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

import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_DC_FOLDER_PATH;
import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_DC_QUERY_PATH;

@Service
public class DataCollectionDataRetrieverDL implements DataLakeDataRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDataRetrieverDL.class);
    private S3Service s3Service;
    private S3Helper s3Helper;
    private JdbcTemplate dremioJdbcTemplate;
    private DremioHelperService dremioHelperService;

    @Autowired
    public DataCollectionDataRetrieverDL(S3Service s3Service, S3Helper s3Helper, @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, DremioHelperService dremioHelperService) {
        this.s3Service = s3Service;
        this.s3Helper = s3Helper;
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.dremioHelperService = dremioHelperService;
    }

    @Override
    public TableVO getTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError,
                                  String[] qcCodes) throws EEAException {
        Long totalRecords = 0L;
        Long datasetId = dataset.getId();
        TableVO result = new TableVO();
        S3PathResolver s3PathResolver = s3Service.getS3PathResolverByDatasetType(dataset, tableSchemaVO.getNameTableSchema());
        boolean folderExist = s3Helper.checkTableNameDCFolderExist(s3PathResolver);
        LOG.info("For datasetId {} s3PathResolver : {}", datasetId, s3PathResolver);
        LOG.info("s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_DC_FOLDER_PATH) : {}", folderExist);
        if (folderExist && dremioHelperService.checkFolderPromoted(s3PathResolver,s3PathResolver.getTableName())) {
            s3PathResolver.setPath(S3_TABLE_NAME_DC_FOLDER_PATH);
            totalRecords = dremioJdbcTemplate.queryForObject(s3Helper.buildRecordsCountQueryDC(s3PathResolver), Long.class);
            result.setTotalRecords(totalRecords);
            LOG.info("For datasetId {} totalRecords : {}", datasetId, totalRecords);
            pageable = DataLakeDataRetrieverUtils.calculatePageable(pageable, totalRecords);
            Map<String, FieldSchemaVO> fieldIdMap = tableSchemaVO.getRecordSchema().getFieldSchema().stream().collect(Collectors.toMap(FieldSchemaVO::getId, Function.identity()));
            FieldSchemaVO fieldSchemaProviderCode = new FieldSchemaVO();
            fieldSchemaProviderCode.setName("data_provider_code");
            fieldIdMap.put("data_provider_code", fieldSchemaProviderCode);
            StringBuilder filteredQuery = DataLakeDataRetrieverUtils.buildFilteredQuery(dataset, fields, fieldValue, fieldIdMap, levelError, qcCodes, null);
            StringBuilder recordsCountQuery = new StringBuilder();
            recordsCountQuery.append("select count(record_id) from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH) + " t ").append(filteredQuery);
            Long totalFilteredRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery.toString(), Long.class);
            result.setTotalFilteredRecords(totalFilteredRecords);

            pageable = DataLakeDataRetrieverUtils.calculatePageable(pageable, totalFilteredRecords);
            //pagination
            if (pageable !=null) {
                DataLakeDataRetrieverUtils.buildPaginationQuery(pageable, filteredQuery);
            }

            StringBuilder dataQuery = new StringBuilder();
            dataQuery.append("select * from " + s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH) + " t ");
            dataQuery.append(filteredQuery);
            LOG.info("For datasetId {} dataQuery.toString() : {}", dataset.getId(), dataQuery);
            List<RecordVO> recordVOS = DataLakeDataRetrieverUtils.getRecordVOS(dataset.getDatasetSchema(), tableSchemaVO, dataQuery);
            result.setIdTableSchema(tableSchemaVO.getIdTableSchema());
            result.setRecords(recordVOS);
        } else {
            setEmptyResults(result);
        }
        return result;
    }

    @Override
    public boolean isApplicable(String datasetType) {
        return DatasetTypeEnum.COLLECTION.getValue().equals(datasetType);
    }
}