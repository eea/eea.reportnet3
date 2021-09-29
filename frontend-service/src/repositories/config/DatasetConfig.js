export const DatasetConfig = {
  uploadAttachment: '/dataset/{:datasetId}/field/{:fieldId}/attachment',
  createRecord: '/dataset/{:datasetId}/table/{:tableSchemaId}/record',
  createRecordDesign: '/dataschema/{:datasetId}/fieldSchema',
  createTableDesign: '/dataschema/{:datasetId}/tableSchema',
  getSchema: '/dataschema/datasetId/{:datasetId}',
  getMetadata: '/datasetmetabase/{:datasetId}',
  getTableData:
    '/dataset/TableValueDataset/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&idRules={:idRules}',
  downloadTableDefinitions: '/dataschema/dataset/{:datasetSchemaId}/exportFieldSchemas',
  deleteSchema: '/dataschema/dataset/{:datasetId}',
  deleteAttachment: '/dataset/{:datasetId}/field/{:fieldId}/attachment',
  deleteData: '/dataset/{:datasetId}/deleteImportData?deletePrefilledTables={:deletePrefilledTables}',
  deleteTableData: '/dataset/{:datasetId}/deleteImportTable/{:tableId}',
  deleteRecord: '/dataset/{:datasetId}/record/{:recordId}?deleteCascadePK={:deleteInCascade}',
  deleteFieldDesign: '/dataschema/{:datasetId}/fieldSchema/{:fieldSchemaId}',
  deleteTableDesign: '/dataschema/{:datasetId}/tableSchema/{:tableSchemaId}',
  downloadPublicDatasetFile:
    '/dataset/exportPublicFile/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}?fileName={:fileName}',
  downloadExportDatasetFile: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadExportFile: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}&providerId={:providerId}',
  downloadExportFileNoProviderId: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}',
  downloadFileData: '/dataset/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}',
  downloadFileDataWithProviderId:
    '/dataset/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}',
  downloadPublicReferenceDatasetFileData: '/dataset/exportPublicFile/dataflow/{:dataflowId}?fileName={:fileName}',
  exportDatasetData: '/dataset/{:datasetId}/exportDatasetFile?mimeType={:fileType}',
  exportDatasetDataExternal:
    '/dataset/exportFileThroughIntegration?datasetId={:datasetId}&integrationId={:integrationId}',
  exportTableData: '/dataset/exportFile?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  exportTableSchema:
    '/dataschema/{:datasetSchemaId}/exportFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  importFileDataset: '/dataset/{:datasetId}/importFileData?delimiter={:delimiter}',
  importFileDatasetExternal: '/dataset/{:datasetId}/importFileData?integrationId={:integrationId}',
  importFileTable: '/dataset/{:datasetId}/importFileData?tableSchemaId={:tableSchemaId}&delimiter={:delimiter}',
  importTableSchema:
    '/dataschema/{:datasetSchemaId}/importFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}',
  getShowValidationErrors:
    '/validation/listGroupValidations/{:datasetId}?asc={:asc}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}',
  getStatistics: '/datasetmetabase/{:datasetId}/loadStatistics',
  updateFieldOrder: '/dataschema/{:datasetId}/fieldSchema/order',
  updateTableOrder: '/dataschema/{:datasetId}/tableSchema/order',
  getReferencedFieldValues:
    '/dataset/{:datasetId}/datasetSchemaId/{:datasetSchemaId}/fieldSchemaId/{:fieldSchemaId}/getFieldsValuesReferenced?searchValue={:searchToken}&conditionalValue={:conditionalValue}&resultsNumber={:resultsNumber}',
  updateDatasetNameDesign: '/datasetmetabase/updateDatasetName?datasetId={:datasetId}&datasetName={:datasetSchemaName}',
  updateDatasetFeedbackStatus: '/datasetmetabase/updateDatasetStatus',
  updateDatasetDesign: '/dataschema/{:datasetId}/datasetSchema',
  updateFieldDesign: '/dataschema/{:datasetId}/fieldSchema',
  updateReferenceDatasetStatus: '/referenceDataset/{:datasetId}?updatable={:updatable}',
  updateField: '/dataset/{:datasetId}/updateField?updateCascadePK={:updateInCascade}',
  updateRecord: '/dataset/{:datasetId}/updateRecord?updateCascadePK={:updateInCascade}',
  updateTableDesign: '/dataschema/{:datasetId}/tableSchema',
  validate: '/validation/dataset/{:datasetId}',
  validateSql: '/rules/validateSqlRules?datasetId={:datasetId}&datasetSchemaId={:datasetSchemaId}',
  validationViewer: '/dataset/findPositionFromAnyObject/{:objectId}?datasetId={:datasetId}&type={:entityType}'
};
