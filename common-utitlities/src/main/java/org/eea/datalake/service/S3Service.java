package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;

public interface S3Service {

    /**
     * @return provider query path
     */
    String getProviderQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return provider path
     */
    String getProviderPath(S3PathResolver s3PathResolver);

    /**
     * @return DC path
     */
    String getDCPath(S3PathResolver s3PathResolver);

    /**
     * @return DC query path
     */
    String getDCQueryPath(S3PathResolver s3PathResolver);

    /**
     * @param s3PathResolver
     * @return table name query path
     */
    String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path);
}
