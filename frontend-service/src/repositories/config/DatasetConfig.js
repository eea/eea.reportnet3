export const DatasetConfig = {
  uploadAttachment:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&recordId={:recordId}&previousFileName={:previousFileName}',
  uploadAttachmentWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&recordId={:recordId}&previousFileName={:previousFileName}',
  convertIcebergToParquet:
    '/dataset/convertIcebergToParquetTable/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}',
  convertParquetToIceberg:
    '/dataset/convertParquetToIcebergTable/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}',
  createRecord: '/dataset/{:datasetId}/table/{:tableSchemaId}/record',
  createRecordDesign: '/dataschema/{:datasetId}/fieldSchema',
  createTableDesign: '/dataschema/{:datasetId}/tableSchema',
  getIsIcebergTableCreated: '/dataset/isIcebergTableCreated/{:datasetId}/{:tableSchemaId}',
  getSchema: '/dataschema/v1/datasetId/{:datasetId}',
  getMetadata: '/datasetmetabase/{:datasetId}',
  getTableData:
    '/dataset/TableValueDataset/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&idRules={:idRules}',
  getTableDataDL:
    '/dataset/TableValueDatasetDL/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&qcCodes={:qcCodes}',
  downloadTableDefinitions: '/dataschema/v1/dataset/{:datasetSchemaId}/exportFieldSchemas',
  deleteSchema: '/dataschema/dataset/{:datasetId}',
  deleteAttachment:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}',
  deleteAttachmentWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}',
  deleteData: '/dataset/v1/{:datasetId}/deleteDatasetData?deletePrefilledTables={:deletePrefilledTables}',
  deleteTableData: '/dataset/v1/{:datasetId}/deleteTableData/{:tableId}',
  deleteRecord:
    '/dataset/{:datasetId}/record/{:recordId}?deleteCascadePK={:deleteInCascade}&tableSchemaId={:tableSchemaId}',
  deleteFieldDesign: '/dataschema/{:datasetId}/fieldSchema/{:fieldSchemaId}',
  deleteTableDesign: '/dataschema/{:datasetId}/tableSchema/{:tableSchemaId}',
  downloadPublicDatasetFile:
    '/dataset/exportPublicFile/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}?fileName={:fileName}',
  downloadExportDatasetFile: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadExportDatasetFileDL: '/dataset/{:datasetId}/downloadFileDL?fileName={:fileName}',
  downloadExportFile: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}',
  downloadExportFileWithProviderId:
    '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}&providerId={:providerId}',
  downloadFileData:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}',
  downloadFileDataWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}',
  downloadPublicReferenceDatasetFileData: '/dataset/exportPublicFile/dataflow/{:dataflowId}?fileName={:fileName}',
  downloadTableData: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadTableDataDL: '/dataset/{:datasetId}/downloadFileDL?fileName={:fileName}',
  exportDatasetData: '/dataset/{:datasetId}/exportDatasetFile?mimeType={:fileType}',
  exportDatasetDataDL: '/dataset/{:datasetId}/exportDatasetFileDL?mimeType={:fileType}',
  exportDatasetDataExternal:
    '/dataset/exportFileThroughIntegration?datasetId={:datasetId}&integrationId={:integrationId}',
  exportTableData: '/dataset/exportFile?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  exportTableDataDL: '/dataset/exportFileDL?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  exportTableSchema:
    '/dataschema/v1/{:datasetSchemaId}/exportFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  importFileDataset: '/dataset/v2/importFileData/{:datasetId}?delimiter={:delimiter}',
  importFileDatasetUpd: '/dataset/v2/importFileData/{:datasetId}?delimiter={:delimiter}&dataflowId={:dataflowId}',

  importFileDatasetExternal: '/dataset/v2/importFileData/{:datasetId}?integrationId={:integrationId}',
  importFileTable: '/dataset/v2/importFileData/{:datasetId}?tableSchemaId={:tableSchemaId}&delimiter={:delimiter}',
  importFileTableUpd:
    '/dataset/v2/importFileData/{:datasetId}?tableSchemaId={:tableSchemaId}&delimiter={:delimiter}&dataflowId={:dataflowId}',
  importTableSchema:
    '/dataschema/v1/{:datasetSchemaId}/importFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}',
  importTableFileWithS3:
    '/dataset/v2/importFileData/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}&replace={:replace}&integrationId={:integrationId}&delimiter={:delimiter}&jobId={:jobId}',
  getPresignedUrl:
    '/dataset/{:datasetId}/generateImportPresignedUrl?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}&replace={:replace}&integrationId={:integrationId}&delimiter={:delimiter}&fileName={:fileName}',
  getShowValidationErrors:
    '/validation/listGroupValidations/{:datasetId}?asc={:asc}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}',
  getShowValidationErrorsDL:
    '/validation/listGroupValidationsDL/{:datasetId}?asc={:asc}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}',
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
  updateField:
    '/dataset/{:datasetId}/updateField?updateCascadePK={:updateInCascade}&recordId={:recordId}&tableSchemaId={:tableSchemaId}',
  updateRecord: '/dataset/{:datasetId}/updateRecord?updateCascadePK={:updateInCascade}&tableSchemaId={:tableSchemaId}',
  updateTableDesign: '/dataschema/{:datasetId}/tableSchema',
  validate: '/orchestrator/jobs/addValidationJob/{:datasetId}',
  validateAllSql: '/rules/validateAllRules?datasetId={:datasetId}',
  validateSql: '/rules/validateSqlRules?datasetId={:datasetId}&datasetSchemaId={:datasetSchemaId}',
  validationViewer: '/dataset/findPositionFromAnyObject/{:objectId}?datasetId={:datasetId}&type={:entityType}',
  testImportProcess: '/dataset/checkImportProcess/{:datasetId}'
};
