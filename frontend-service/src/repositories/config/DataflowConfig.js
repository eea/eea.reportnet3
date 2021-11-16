export const DataflowConfig = {
  cloneSchemas: '/dataschema/copy?sourceDataflow={:sourceDataflowId}&targetDataflow={:targetDataflowId}',
  countByType: '/dataflow/countByType',
  createUpdate: '/dataflow',
  createApiKey: '/user/createApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  createApiKeyCustodian: '/user/createApiKey?dataflowId={:dataflowId}',
  createEmptyDatasetSchema:
    '/dataschema/createEmptyDatasetSchema?dataflowId={:dataflowId}&datasetSchemaName={:datasetSchemaName}',
  delete: '/dataflow/{:dataflowId}',
  downloadAllSchemasInfo: '/dataflow/downloadSchemaInformation/{:dataflowId}?fileName={:fileName}',
  downloadPublicAllSchemasInfoFile: '/dataflow/downloadPublicSchemaInformation/{:dataflowId}',
  downloadUsersListFile: '/user/downloadUsersByCountry/{:dataflowId}?fileName={:fileName}',
  exportSchemas: '/dataschema/export?dataflowId={:dataflowId}',
  getAll: '/dataflow/getDataflows',
  getCloneableDataflows: '/dataflow/cloneableDataflows',
  getAllDataflowsUserList: '/dataflow/getUserRolesAllDataflows',
  generateAllSchemasInfoFile: '/dataflow/exportSchemaInformation/{:dataflowId}',
  generateUsersByCountryFile: '/user/exportUsersByCountry/dataflow/{:dataflowId}',
  getApiKey: '/user/getApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  getApiKeyCustodian: '/user/getApiKey?dataflowId={:dataflowId}',
  getDetails: '/dataflow/v1/{:dataflowId}/getmetabase',
  getDatasetsFinalFeedbackAndReleasedStatus: '/datasetmetabase/dataflow/{:dataflowId}',
  getDatasetsInfo: '/dataflow/{:dataflowId}/datasetsSummary',
  getDatasetsValidationStatistics:
    '/datasetmetabase/globalStatistics/dataflow/{:dataflowId}/dataSchema/{:datasetSchemaId}',
  getPublicData: '/dataflow/getPublicDataflows',
  getPublicDataflowData: '/dataflow/getPublicDataflow/{:dataflowId}',
  getPublicDataflowsByCountryCode:
    '/dataflow/public/country/{:country}?asc={:asc}&pageNum={:pageNum}&pageSize={:pageSize}&sortField={:sortField}',
  get: '/dataflow/v1/{:dataflowId}',
  getRepresentativesUsersList: '/user/userRoles/dataflow/{:dataflowId}',
  getSchemas: '/dataschema/getSchemas/dataflow/{:dataflowId}',
  getSchemasValidation: '/dataschema/validate/dataflow/{:dataflowId}',
  getUserList: '/user/getUserRolesByDataflow/{:dataflowId}/dataProviderId/{:representativeId}',
  importSchema: '/dataschema/import?dataflowId={:dataflowId}'
};
