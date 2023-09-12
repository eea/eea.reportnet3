package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;

public interface DremioHelperService {

    String getAuthToken();

    boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    String getFolderId(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void demoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void removeImportRelatedTableFromDremio(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void executeSqlStatement(String sqlStatement);
}
