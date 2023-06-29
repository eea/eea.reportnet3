package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;

public interface S3Service {
    /**
     * @return validation provider path
     */
    String getValidationProviderPath(S3PathResolver s3PathResolver);

    /**
     * @return validation provider query path
     */
    String getValidationProviderQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return import provider path
     */
    String getImportProviderPath(S3PathResolver s3PathResolver);

    /**
     * @return import provider query path
     */
    String getImportProviderQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return table name provider path
     */
    String getTableNameProviderPath(S3PathResolver s3PathResolver);

    /**
     * @return table name provider query path
     */
    String getTableNameProviderQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return table name validation provider path
     */
    String getTableNameValidateProviderPath(S3PathResolver s3PathResolver);

    /**
     * @return table name validation provider query path
     */
    String getTableNameValidateProviderQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return validation DC path
     */
    String getValidationDCPath(S3PathResolver s3PathResolver);

    /**
     * @return validation DC query path
     */
    String getValidationDCQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return table name DC path
     */
    String getTableNameDCPath(S3PathResolver s3PathResolver);

    /**
     * @return table name DC query path
     */
    String getTableNameDCQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return table name validation DC path
     */
    String getTableNameValidateDCPath(S3PathResolver s3PathResolver);

    /**
     * @return table name validation DC query path
     */
    String getTableNameValidateDCQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return dataflow reference path
     */
    String getDataflowReferencePath(S3PathResolver s3PathResolver);

    /**
     * @return dataflow reference query path
     */
    String getDataflowReferenceQueryPath(S3PathResolver s3PathResolver);

    /**
     * @return export DC path
     */
    String getExportDCPath(S3PathResolver s3PathResolver);

    /**
     * @return export DC query path
     */
    String getExportDCQueryPath(S3PathResolver s3PathResolver);

    /**
     * @param s3PathResolver
     * @return table name query path
     */
    String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path);

    /**
     * @return export Folder path
     */
    String getExportFolderPath(S3PathResolver s3PathResolver);
}
