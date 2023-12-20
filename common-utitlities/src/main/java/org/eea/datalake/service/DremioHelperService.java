package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dremio.DremioDirectoryItemsResponse;
import org.eea.interfaces.vo.dremio.DremioJobStatusResponse;

public interface DremioHelperService {

    String getAuthToken();

    boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName);

    DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver, String folderName);

    String getFolderId(S3PathResolver s3PathResolver, String folderName);

    void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName);

    void demoteFolderOrFile(S3PathResolver s3PathResolver, String folderName);

    void deleteFileFromR3IfExists(String parquetFile) throws Exception;

    String executeSqlStatement(String sqlStatement);

    DremioJobStatusResponse pollForJobStatus(String id);

    String executeSqlStatementPost(String sqlStatement);

    Boolean dremioProcessFinishedSuccessfully(String processId) throws Exception;
}
