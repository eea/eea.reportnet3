package org.eea.datalake.service.impl;

import feign.FeignException;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.DremioItemTypeEnum;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.vo.dremio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.eea.utils.LiteralConstants.*;

@Service
public class DremioHelperServiceImpl implements DremioHelperService {

    private static final Logger LOG = LoggerFactory.getLogger(DremioHelperServiceImpl.class);


    @Value("${dremio.username}")
    private String dremioUsername;

    @Value("${dremio.password}")
    private String dremioPassword;

    @Autowired
    private DremioApiController dremioApiController;
    @Autowired
    private S3Service s3Service;

    @Autowired
    JdbcTemplate dremioJdbcTemplate;
    private static final String PROMOTED = "PROMOTED";
    private static final String BEARER = "Bearer ";
    public static String token = null;
    public static final String DATASET_TYPE = "PHYSICAL_DATASET";
    public static final String PARQUET_FORMAT_TYPE = "Parquet";
    public static final String CSV_FORMAT_TYPE = "Text";
    public static final String ENTITY_TYPE = "dataset";
    public static final String DREMIO_CONSTANT = "dremio:/";

    public DremioHelperServiceImpl(DremioApiController dremioApiController, S3Service s3Service) {
        this.dremioApiController = dremioApiController;
        this.s3Service = s3Service;
    }

    @Override
    public String getAuthToken() {
        DremioCredentials dremioCredentials = new DremioCredentials(dremioUsername, dremioPassword);
        DremioAuthResponse response = dremioApiController.login(dremioCredentials);
        return BEARER + response.getToken();
    }

    @Override
    public boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName, Boolean importFolder) {
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver, folderName, importFolder);
        if (directoryItems!=null) {
            LOG.info("directoryItems : {}", directoryItems.toString());
            LOG.info("directoryItems getChildren : {}", directoryItems.getChildren());
            Integer itemPosition = (importFolder == true) ? 8 : 6;
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(itemPosition).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                LOG.info("itemOptional : {}", itemOptional.toString());
                DremioDirectoryItem item = itemOptional.get();
                LOG.info("item : {}", item);
                if (item.getType().equals(DremioItemTypeEnum.DATASET.getValue()) && item.getDatasetType().equals(PROMOTED)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver, String folderName, Boolean importFolder) {
        String directoryPath = null;
        if(importFolder) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver,
                S3_IMPORT_TABLE_NAME_FOLDER_PATH);
        } else if (S3_TABLE_NAME_ROOT_DC_FOLDER_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableNameRootFolderDCPath(s3PathResolver);
        } else {
            directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_CURRENT_PATH);
        }
        LOG.info("directoryPath : {}", directoryPath);
        DremioDirectoryItemsResponse directoryItems = null;
        try {
            directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
            } else {
                throw e;
            }
        }
        return directoryItems;
    }

    @Override
    public String getFolderId(S3PathResolver s3PathResolver, String folderName, Boolean importFolder) {
        String folderId = null;
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver, folderName, importFolder);
        if (directoryItems!=null) {
            Integer itemPosition = (importFolder == true) ? 8 : 6;
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(itemPosition).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                folderId = item.getId();
            }
        }
        LOG.info("Found folderId {} for folderName {}", folderId, folderName);
        return folderId;
    }

    @Override
    public void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder) {
        String directoryPath;
        String folderId;
        DremioPromotionRequestBody requestBody;
        if(importFolder) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_FILE_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioCSVPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioCSVPromotionRequestBody.Format(CSV_FORMAT_TYPE, true));
        }
        else{
            directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioParquetPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioParquetPromotionRequestBody.Format(PARQUET_FORMAT_TYPE));
        }

        if(checkFolderPromoted(s3PathResolver, folderName, importFolder)){
            LOG.info("Folder {} is already promoted", directoryPath);
            return;
        }
        folderId = getFolderId(s3PathResolver, folderName, importFolder);

        try {
            dremioApiController.promote(token, folderId, requestBody);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.promote(token, folderId, requestBody);
            } else {
                throw e;
            }
        }
        LOG.info("Promoted folder {}", directoryPath);
    }

    @Override
    public void demoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder) {
        String directoryPath;
        if(importFolder) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_FILE_PATH);
        }
        else{
            directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }
        if(!checkFolderPromoted(s3PathResolver, folderName, importFolder)){
            LOG.info("Folder {} is not promoted", directoryPath);
            return;
        }
        String folderId = getFolderId(s3PathResolver, folderName, importFolder);
        try {
            dremioApiController.demote(token, folderId);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.demote(token, folderId);
            } else {
                throw e;
            }
        }
        LOG.info("Demoted folder {}", directoryPath);
    }

    @Override
    public void removeImportRelatedTableFromDremio(S3PathResolver s3PathResolver, String folderName, Boolean importFolder){
        String tablePath = null;
        if(importFolder){
            tablePath = s3Service.getImportProviderQueryPath(s3PathResolver);
        }
        else{
            tablePath = s3Service.getTableAsFolderQueryPath(s3PathResolver);
        }

        if(!checkFolderPromoted(s3PathResolver, folderName, importFolder)){
            LOG.info("The file or folder in path {} is not promoted", tablePath);
            return;
        }
        String dropTableQuery = "DROP TABLE " + tablePath;
        try{
            dremioJdbcTemplate.execute(dropTableQuery);
        }
        catch (Exception e){
            LOG.error("Could not drop table {}", tablePath);
        }
    }
}
