export const DataflowConfig = {
  cloneSchemas: '/dataschema/copy?sourceDataflow={:sourceDataflowId}&targetDataflow={:targetDataflowId}',
  create: '/dataflow',
  createApiKey: '/user/createApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  createApiKeyCustodian: '/user/createApiKey?dataflowId={:dataflowId}',
  createEmptyDatasetSchema:
    '/dataschema/createEmptyDatasetSchema?dataflowId={:dataflowId}&datasetSchemaName={:datasetSchemaName}',
  delete: '/dataflow/{:dataflowId}',
  exportSchemas: '/dataschema/export?dataflowId={:dataflowId}',
  getAll: '/dataflow/getDataflows',
  getAllDataflowsUserList: '/dataflow/getUserRolesAllDataflows',
  getApiKey: '/user/getApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  getApiKeyCustodian: '/user/getApiKey?dataflowId={:dataflowId}',
  getDataflowDetails: '/dataflow/{:dataflowId}/getmetabase',
  getDatasetsFinalFeedback: '/datasetmetabase/dataflow/{:dataflowId}',
  getDatasetsReleasedStatus: '/datasetmetabase/dataflow/{:dataflowId}',
  getDatasetsValidationStatistics:
    '/datasetmetabase/globalStatistics/dataflow/{:dataflowId}/dataSchema/{:datasetSchemaId}',
  getPublicData: '/dataflow/getPublicDataflows',
  getPublicDataflowData: '/dataflow/getPublicDataflow/{:dataflowId}',
  getPublicDataflowsByCountryCode:
    '/dataflow/public/country/{:country}?asc={:asc}&pageNum={:pageNum}&pageSize={:pageSize}&sortField={:sortField}',
  getReportingDatasets: '/dataflow/{:dataflowId}',
  getRepresentativesUsersList: '/user/userRoles/dataflow/{:dataflowId}',
  getSchemas: '/dataschema/getSchemas/dataflow/{:dataflowId}',
  getSchemasValidation: '/dataschema/validate/dataflow/{:dataflowId}',
  getUserList: '/user/getUserRolesByDataflow/{:dataflowId}/dataProviderId/{:representativeId}',
  importSchema: '/dataschema/import?dataflowId={:dataflowId}'
};
