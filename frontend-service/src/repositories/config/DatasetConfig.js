export const DatasetConfig = {
  uploadAttachment:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&recordId={:recordId}&previousFileName={:previousFileName}&preparationCode={:preparationCode}',
  uploadAttachmentWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&recordId={:recordId}&previousFileName={:previousFileName}&preparationCode={:preparationCode}',
  enableEditing: '/dataset/{:datasetId}/enableEditing',
  disableEditing: '/dataset/{:datasetId}/disableEditing',
  convertIcebergsToParquets:
    '/dataset/convertIcebergToParquetTables/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&preparationCode={:preparationCode}',
  convertParquetsToIcebergs:
    '/dataset/convertParquetToIcebergTables/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&preparationCode={:preparationCode}',
  createRecord: '/dataset/{:datasetId}/table/{:tableSchemaId}/record?preparationCode={:preparationCode}',
  createRecordDesign: '/dataschema/{:datasetId}/fieldSchema',
  createTableDesign: '/dataschema/{:datasetId}/tableSchema',
  getIsIcebergTableCreated:
    '/dataset/isIcebergTableCreated/{:datasetId}/{:tableSchemaId}?preparationCode={:preparationCode}',
  getAlignmentBetween:
    '/dataset/getReleasedDatasetDataInfo?collectionDatasetId={:datasetId}&providerCode={:selectedRepresentativesCode}&tableSchemaId={:selectedTable}',
  getIsAvailableForManualEditing: '/dataset/getAvailableForManualEditingTables/{:datasetId}',
  getEditingStatus: '/dataset/{:datasetId}/editingStatus?preparationCode={:preparationCode}',
  getIsEdited: '/dataset/tablesUpdated/?datasetId={:datasetId}',
  getSchema: '/dataschema/v1/datasetId/{:datasetId}',
  getTableImportedMetadata: '/dataset/getImportRelatedStatistics/{:datasetId}',
  getMetadata: '/datasetmetabase/{:datasetId}',
  getTableData:
    '/dataset/TableValueDataset/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&idRules={:idRules}',
  getTableDataDL:
    '/dataset/TableValueDatasetDL/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&qcCodes={:qcCodes}',
  getPreparationsTableDataDL:
    '/dataset/preparations/TableValueDatasetDL/{:datasetId}?code={:code}&fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&qcCodes={:qcCodes}',
  getImportedFiles: '/dataset/list-imported-files?datasetId={:datasetId}',
  downloadTableDefinitions: '/dataschema/v1/dataset/{:datasetSchemaId}/exportFieldSchemas',
  deleteSchema: '/dataschema/dataset/{:datasetId}',
  deleteAttachment:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}&preparationCode={:preparationCode}',
  deleteAttachmentWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}&preparationCode={:preparationCode}',
  deleteData:
    '/dataset/v1/{:datasetId}/deleteDatasetData?deletePrefilledTables={:deletePrefilledTables}&preparationCode={:preparationCode}',
  deleteTableData: '/dataset/v1/{:datasetId}/deleteTableData/{:tableId}?preparationCode={:preparationCode}',
  deleteRecord:
    '/dataset/{:datasetId}/record/{:selectedRecordId}?deleteCascadePK={:updateInCascade}&tableSchemaId={:tableId}&preparationCode={:preparationCode}',
  deleteFieldDesign: '/dataschema/{:datasetId}/fieldSchema/{:fieldSchemaId}',
  deleteTableDesign: '/dataschema/{:datasetId}/tableSchema/{:tableSchemaId}',
  downloadPublicDatasetFile:
    '/dataset/exportPublicFile/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}?fileName={:fileName}',
  downloadExportDatasetFile: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadExportDatasetFileDL: '/dataset/{:datasetId}/downloadFileDL?fileName={:fileName}&code={:code}',
  downloadExportFile: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}',
  downloadExportFileWithProviderId:
    '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}&providerId={:providerId}',
  downloadFileData:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}&providerCode={:providerCode}&preparationCode={:preparationCode}',
  downloadFileDataWithProviderId:
    '/dataset/v1/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaName={:tableSchemaName}&fieldName={:fieldName}&fileName={:fileName}&recordId={:recordId}&providerCode={:providerCode}&preparationCode={:preparationCode}',
  downloadGeometry:
    '/dataset/v1/{:datasetId}/record/{:recordId}/geometry?fieldId={:fieldId}&dataflowId={:dataflowId}&idTableSchema={:tableSchemaId}&providerId={:providerId}',
  downloadPublicReferenceDatasetFileData: '/dataset/exportPublicFile/dataflow/{:dataflowId}?fileName={:fileName}',
  downloadTableData: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadTableDataDL: '/dataset/{:datasetId}/downloadFileDL?fileName={:fileName}&code={:code}',
  downloadImportedFile:
    '/dataset/download-imported-file?fileName={:fileName}&datasetId={:datasetId}&dataflowId={:dataflowId}',
  exportDatasetData: '/dataset/{:datasetId}/exportDatasetFile?mimeType={:fileType}',
  exportDatasetDataDL: '/dataset/{:datasetId}/exportDatasetFileDL?mimeType={:fileType}&code={:code}',
  exportDatasetDataExternal:
    '/dataset/exportFileThroughIntegration?datasetId={:datasetId}&integrationId={:integrationId}&code={:code}',
  exportTableData: '/dataset/exportFile?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  exportTableDataDL:
    '/dataset/exportFileDL?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}&code={:code}',
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
    '/dataset/v2/importFileData/{:datasetId}?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}&replace={:replace}&integrationId={:integrationId}&delimiter={:delimiter}&jobId={:jobId}&code={:code}',
  getAddUserText: '/release-receipts/dataflow/{:dataflowId}',
  getPresignedUrl:
    '/dataset/{:datasetId}/generateImportPresignedUrl?dataflowId={:dataflowId}&providerId={:providerId}&tableSchemaId={:tableSchemaId}&replace={:replace}&integrationId={:integrationId}&delimiter={:delimiter}&fileName={:fileName}&code={:code}',
  getFullGeometry:
    '/dataset/v1/{:datasetId}/record/{:recordId}/geometry?fieldId={:fieldId}&dataflowId={:dataflowId}&idTableSchema={:tableSchemaId}&providerId={:providerId}&downloadFile=false',
  getShowValidationErrors:
    '/validation/listGroupValidations/{:datasetId}?asc={:asc}&shortCode={:shortCodeFilter}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}',
  getShowValidationErrorsDL:
    '/validation/listGroupValidationsDL/{:datasetId}?asc={:asc}&shortCode={:shortCodeFilter}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}&code={:code}',
  getStatistics: '/datasetmetabase/{:datasetId}/loadStatistics',
  restorePrefilledTables: '/dataset/restorePrefilledTables/{:datasetId}?tableSchemaId={:tableSchemaId}',
  updateFieldOrder: '/dataschema/{:datasetId}/fieldSchema/order',
  updateTableOrder: '/dataschema/{:datasetId}/tableSchema/order',
  getReferencedFieldValues:
    '/dataset/{:datasetId}/datasetSchemaId/{:datasetSchemaId}/fieldSchemaId/{:fieldSchemaId}/getFieldsValuesReferenced?searchValue={:searchToken}&conditionalValue={:conditionalValue}&resultsNumber={:resultsNumber}',
  handleStuckImportJob: '/orchestrator/jobs/handleStuckImportJob/{:jobId}',
  updateDatasetNameDesign: '/datasetmetabase/updateDatasetName?datasetId={:datasetId}&datasetName={:datasetSchemaName}',
  updateDatasetFeedbackStatus: '/datasetmetabase/updateDatasetStatus',
  updateDatasetDesign: '/dataschema/{:datasetId}/datasetSchema',
  updateAddUserText: '/release-receipts',
  updateFieldDesign: '/dataschema/{:datasetId}/fieldSchema',
  updateReferenceDatasetStatus: '/referenceDataset/{:datasetId}?updatable={:updatable}',
  updateField:
    '/dataset/{:datasetId}/updateField?updateCascadePK={:updateInCascade}&recordId={:recordId}&tableSchemaId={:tableSchemaId}',
  updateConditionalFieldsWebform:
    '/dataset/{:datasetId}/updateWebformFields?updateCascadePK={:updateInCascade}&recordId={:recordId}&tableSchemaId={:tableSchemaId}',
  updateRecord:
    '/dataset/{:datasetId}/updateRecord?updateCascadePK={:updateInCascade}&tableSchemaId={:tableSchemaId}&preparationCode={:preparationCode}',
  updateTableDesign: '/dataschema/{:datasetId}/tableSchema',
  validate: '/orchestrator/jobs/addValidationJob/{:datasetId}?code={:code}',
  validateAsProvider:
    '/orchestrator/jobs/addValidationJob/{:datasetId}?dataflowId={:dataflowId}&validateAsProviderCode={:providerId}&code={:code}',
  validateAllSql: '/rules/validateAllRules?datasetId={:datasetId}',
  validateSql: '/rules/validateSqlRules?datasetId={:datasetId}&datasetSchemaId={:datasetSchemaId}',
  validationViewer: '/dataset/findPositionFromAnyObject/{:objectId}?datasetId={:datasetId}&type={:entityType}',
  testImportProcess: '/dataset/checkImportProcess/{:datasetId}',
  checkDuplicateValues: '/dataset/duplicateFieldValueExists/{:datasetId}?tableSchemaId={:tableSchemaId}'
};
