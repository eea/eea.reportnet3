package org.eea.datalake.service.impl;

import feign.FeignException;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.DremioItemTypeEnum;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.vo.dremio.DremioAuthResponse;
import org.eea.interfaces.vo.dremio.DremioCredentials;
import org.eea.interfaces.vo.dremio.DremioDirectoryItem;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.eea.utils.LiteralConstants.S3_CURRENT_PATH;
import static org.eea.utils.LiteralConstants.S3_DEFAULT_BUCKET_PATH;

@Service
public class DremioHelperServiceImpl implements DremioHelperService {

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
    public boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName) {
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
        if (directoryItems!=null) {
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(6).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                if (item.getType().equals(DremioItemTypeEnum.DATASET.getValue()) && item.getDatasetType().equals(PROMOTED)) {
                    return true;
                }
            }
        }
        return false;
    }
}
