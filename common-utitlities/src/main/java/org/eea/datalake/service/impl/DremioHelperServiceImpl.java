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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private static final String PROMOTED = "PROMOTED";
    private static final String BEARER = "Bearer ";
    public static String token = null;
    public static final String DATASET_TYPE = "PHYSICAL_DATASET";
    public static final String FORMAT_TYPE = "Parquet";
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
    public boolean checkFolderPromoted(S3PathResolver s3PathResolver) {
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver);
        if (directoryItems!=null) {
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(6).equals(s3PathResolver.getTableName())).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                if (item.getType().equals(DremioItemTypeEnum.DATASET.getValue()) && item.getDatasetType().equals(PROMOTED)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver) {
        String directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_CURRENT_PATH);
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
    public String getFolderId(S3PathResolver s3PathResolver, String folderName) {
        String folderId = null;
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver);
        if (directoryItems!=null) {
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(6).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                folderId = item.getId();
            }
        }
        LOG.info("Found folderId {} for folderName {}", folderId, folderName);
        return folderId;
    }

    @Override
    public void promoteFolder(S3PathResolver s3PathResolver, String folderName) {
        String directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
        if(checkFolderPromoted(s3PathResolver)){
            LOG.info("Folder {} is already promoted", directoryPath);
            return;
        }
        String[] path = directoryPath.split("/");
        DremioFolderPromotionRequestBody requestBody = new DremioFolderPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioFolderPromotionRequestBody.Format(FORMAT_TYPE));
        String folderId = getFolderId(s3PathResolver, folderName);
        try {
            dremioApiController.promoteFolder(token, folderId, requestBody);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.promoteFolder(token, folderId, requestBody);
            } else {
                throw e;
            }
        }
        LOG.info("Promoted folder {}", directoryPath);
    }

    @Override
    public void demoteFolder(S3PathResolver s3PathResolver, String folderName) {
        String directoryPath = S3_DEFAULT_BUCKET_PATH + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
        if(!checkFolderPromoted(s3PathResolver)){
            LOG.info("Folder {} is not promoted", directoryPath);
            return;
        }
        String folderId = getFolderId(s3PathResolver, folderName);
        try {
            dremioApiController.demoteFolder(token, folderId);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.demoteFolder(token, folderId);
            } else {
                throw e;
            }
        }
        LOG.info("Demoted folder {}", directoryPath);
    }
}
