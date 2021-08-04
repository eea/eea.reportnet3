export const DataflowConfig = {
  allSchemas: '/dataschema/getSchemas/dataflow/{:dataflowId}',
  cloneDatasetSchemas: '/dataschema/copy?sourceDataflow={:sourceDataflowId}&targetDataflow={:targetDataflowId}',
  createDataflow: '/dataflow',
  dataflowDetails: '/dataflow/{:dataflowId}/getmetabase',
  dataSchemasValidation: '/dataschema/validate/dataflow/{:dataflowId}',
  datasetsFinalFeedback: '/datasetmetabase/dataflow/{:dataflowId}',
  datasetsReleasedStatus: '/datasetmetabase/dataflow/{:dataflowId}',
  deleteDataflow: '/dataflow/{:dataflowId}',
  exportSchema: '/dataschema/export?dataflowId={:dataflowId}',
  generateApiKey: '/user/createApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  generateApiKeyCustodian: '/user/createApiKey?dataflowId={:dataflowId}',
  getApiKey: '/user/getApiKey?dataflowId={:dataflowId}&dataProvider={:dataProviderId}',
  getApiKeyCustodian: '/user/getApiKey?dataflowId={:dataflowId}',
  getDataflows: '/dataflow/getDataflows',
  getAllDataflowsUserList: '/dataflow/getUserRolesAllDataflows',
  getRepresentativesUsersList: '/user/userRoles/dataflow/{:dataflowId}',
  getUserList: '/user/getUserRolesByDataflow/{:dataflowId}/dataProviderId/{:representativeId}',
  getPublicDataflowData: '/dataflow/getPublicDataflow/{:dataflowId}',
  getPublicDataflowsByCountryCode:
    '/dataflow/public/country/{:country}?asc={:asc}&pageNum={:pageNum}&pageSize={:pageSize}&sortField={:sortField}',
  globalStatistics: '/datasetmetabase/globalStatistics/dataflow/{:dataflowId}/dataSchema/{:datasetSchemaId}',
  importSchema: '/dataschema/import?dataflowId={:dataflowId}',
  loadDatasetsByDataflowId: '/dataflow/{:dataflowId}',
  newEmptyDatasetSchema:
    '/dataschema/createEmptyDatasetSchema?dataflowId={:dataflowId}&datasetSchemaName={:datasetSchemaName}',
  publicData: '/dataflow/getPublicDataflows'
};
