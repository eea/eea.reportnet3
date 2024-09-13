export const DataflowConfig = {
  cloneSchemas: '/dataschema/copy?sourceDataflow={:sourceDataflowId}&targetDataflow={:targetDataflowId}',
  countByType: '/dataflow/countByType',
  createApiKey: '/user/createApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  createApiKeyCustodian: '/user/createApiKey?dataflowId={:dataflowId}',
  createEmptyDatasetSchema:
    '/dataschema/createEmptyDatasetSchema?dataflowId={:dataflowId}&datasetSchemaName={:datasetSchemaName}',
  createUpdate: '/dataflow',
  delete: '/dataflow/{:dataflowId}',
  downloadAllSchemasInfo: '/dataflow/downloadSchemaInformation/{:dataflowId}?fileName={:fileName}',
  downloadPublicAllSchemasInfoFile: '/dataflow/downloadPublicSchemaInformation/{:dataflowId}',
  downloadUsersListFile: '/user/downloadUsersByCountry/{:dataflowId}/?fileName={:fileName}',
  exportSchemas: '/dataschema/export?dataflowId={:dataflowId}',
  generateAllSchemasInfoFile: '/dataflow/exportSchemaInformation/{:dataflowId}',
  generateUsersByCountryFile: '/user/exportUsersByCountry/dataflow/{:dataflowId}/?dataProviderId={:dataProviderId}',
  get: '/dataflow/v1/{:dataflowId}',
  getAll: '/dataflow/getDataflows?asc={:isAsc}&pageNum={:pageNum}&orderHeader={:sortBy}&pageSize={:numberRows}',
  getAllDataflowsUserList: '/dataflow/getUserRolesAllDataflows',
  getApiKey: '/user/getApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  getApiKeyCustodian: '/user/getApiKey?dataflowId={:dataflowId}',
  getCloneableDataflows: '/dataflow/cloneableDataflows',
  getDatasetsFinalFeedbackAndReleasedStatus: '/datasetmetabase/dataflow/{:dataflowId}',
  getDatasetsInfo: '/dataflow/{:dataflowId}/datasetsSummary',
  getDatasetsValidationStatistics:
    '/datasetmetabase/globalStatistics/dataflow/{:dataflowId}/dataSchema/{:datasetSchemaId}',
  getDetails: '/dataflow/v1/{:dataflowId}/getmetabase',
  getIcebergTables:
    '/dataset/getIcebergTables?dataflowId={:dataflowId}&providerId={:providerId}&datasetId={:datasetId}',
  getPublicData:
    '/dataflow/getPublicDataflows?asc={:isAsc}&pageNum={:pageNum}&orderHeader={:sortBy}&pageSize={:numberRows}',
  getPublicDataflowData: '/dataflow/getPublicDataflow/{:dataflowId}',
  getPublicDataflowsByCountryCode:
    '/dataflow/public/country/{:country}?asc={:asc}&pageNum={:pageNum}&pageSize={:pageSize}&sortField={:sortField}',
  getRepresentativesUsersList: '/user/userRoles/dataflow/{:dataflowId}',
  getSchemas: '/dataschema/getSchemas/dataflow/{:dataflowId}',
  getSchemasValidation: '/dataschema/validate/dataflow/{:dataflowId}',
  getUserList: '/user/getUserRolesByDataflow/{:dataflowId}/dataProviderId/{:representativeId}',
  importSchema: '/dataschema/import?dataflowId={:dataflowId}',
  updateAutomaticDelete:
    '/dataflow/{:dataflowId}/updateAutomaticDelete?automaticDelete={:isAutomaticReportingDeletion}',
  updateGroupId:
    '/dataflow/updateDataProviderGroupIdById/{:dataflowId}?dataProviderGroupId={:dataProviderGroupId}',
  validateAllDataflowsUsers: '/dataflow/validateAllReporters'
};
