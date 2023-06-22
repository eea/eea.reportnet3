package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;

public interface DremioHelperService {

    String getAuthToken();
    boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName);
}
