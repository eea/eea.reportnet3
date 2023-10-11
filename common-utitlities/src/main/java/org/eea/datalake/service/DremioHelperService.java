package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;
import org.eea.interfaces.vo.dremio.DremioJobStatusResponse;

public interface DremioHelperService {

    String getAuthToken();

    boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    String getFolderId(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void demoteFolderOrFile(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void removeImportRelatedTableFromDremio(S3PathResolver s3PathResolver, String folderName, Boolean importFolder);

    void deleteFileFromR3IfExists(String parquetFile) throws Exception;

    String executeSqlStatement(String sqlStatement);

    DremioJobStatusResponse pollForJobStatus(String id);
}
