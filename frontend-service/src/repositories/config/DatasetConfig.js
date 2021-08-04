export const DatasetConfig = {
  addAttachment: '/dataset/{:datasetId}/field/{:fieldId}/attachment',
  addNewRecord: '/dataset/{:datasetId}/table/{:tableSchemaId}/record',
  addNewRecordFieldDesign: '/dataschema/{:datasetId}/fieldSchema',
  addTableDesign: '/dataschema/{:datasetId}/tableSchema',
  dataSchema: '/dataschema/datasetId/{:datasetId}',
  datasetMetaData: '/datasetmetabase/{:datasetId}',
  loadTableData:
    '/dataset/TableValueDataset/{:datasetId}?fieldSchemaId={:fieldSchemaId}&fieldValue={:value}&idTableSchema={:tableSchemaId}&pageNum={:pageNum}&pageSize={:pageSize}&fields={:fields}&levelError={:levelError}&idRules={:idRules}',
  deleteDataSchema: '/dataschema/dataset/{:datasetId}',
  deleteFileData: '/dataset/{:datasetId}/field/{:fieldId}/attachment',
  deleteImportData: '/dataset/{:datasetId}/deleteImportData',
  deleteImportTable: '/dataset/{:datasetId}/deleteImportTable/{:tableId}',
  deleteRecord: '/dataset/{:datasetId}/record/{:recordId}?deleteCascadePK={:deleteInCascade}',
  deleteRecordFieldDesign: '/dataschema/{:datasetId}/fieldSchema/{:fieldSchemaId}',
  deleteTableDesign: '/dataschema/{:datasetId}/tableSchema/{:tableSchemaId}',
  downloadDatasetFileData:
    '/dataset/exportPublicFile/dataflow/{:dataflowId}/dataProvider/{:dataProviderId}?fileName={:fileName}',
  downloadExportDatasetFile: '/dataset/{:datasetId}/downloadFile?fileName={:fileName}',
  downloadExportFile: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}&providerId={:providerId}',
  downloadExportFileNoProviderId: '/fme/downloadExportFile?datasetId={:datasetId}&fileName={:fileName}',
  downloadFileData: '/dataset/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}',
  downloadFileDataWithProviderId:
    '/dataset/{:datasetId}/field/{:fieldId}/attachment?dataflowId={:dataflowId}&providerId={:providerId}',
  downloadReferenceDatasetFileData: '/dataset/exportPublicFile/dataflow/{:dataflowId}?fileName={:fileName}',
  exportDatasetData: '/dataset/{:datasetId}/exportDatasetFile?mimeType={:fileType}',
  exportDatasetDataExternal:
    '/dataset/exportFileThroughIntegration?datasetId={:datasetId}&integrationId={:integrationId}',
  exportDatasetTableData:
    '/dataset/exportFile?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  exportTableSchema:
    '/dataschema/{:datasetSchemaId}/exportFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}&mimeType={:fileType}',
  importFileDataset: '/dataset/{:datasetId}/importFileData?delimiter={:delimiter}',
  importFileDatasetExternal: '/dataset/{:datasetId}/importFileData?integrationId={:integrationId}',
  importFileTable: '/dataset/{:datasetId}/importFileData?tableSchemaId={:tableSchemaId}&delimiter={:delimiter}',
  importTableSchema:
    '/dataschema/{:datasetSchemaId}/importFieldSchemas?datasetId={:datasetId}&tableSchemaId={:tableSchemaId}',
  listGroupedValidations:
    '/validation/listGroupValidations/{:datasetId}?asc={:asc}&fieldValueFilter={:fieldValueFilter}&headers={:sortField}&levelErrorsFilter={:levelErrorsFilter}&pageNum={:pageNum}&pageSize={:pageSize}&tableFilter={:tableFilter}&typeEntitiesFilter={:typeEntitiesFilter}',
  loadStatistics: '/datasetmetabase/{:datasetId}/loadStatistics',
  orderFieldSchemaDesign: '/dataschema/{:datasetId}/fieldSchema/order',
  orderTableSchemaDesign: '/dataschema/{:datasetId}/tableSchema/order',
  referencedFieldValues:
    '/dataset/{:datasetId}/datasetSchemaId/{:datasetSchemaId}/fieldSchemaId/{:fieldSchemaId}/getFieldsValuesReferenced?searchValue={:searchToken}&conditionalValue={:conditionalValue}&resultsNumber={:resultsNumber}',
  updateDataSchemaName: '/datasetmetabase/updateDatasetName?datasetId={:datasetId}&datasetName={:datasetSchemaName}',
  updateDatasetFeedbackStatus: '/datasetmetabase/updateDatasetStatus',
  updateDatasetSchemaDesign: '/dataschema/{:datasetId}/datasetSchema',
  updateRecordFieldDesign: '/dataschema/{:datasetId}/fieldSchema',
  updateReferenceDatasetStatus: '/referenceDataset/{:datasetId}?updatable={:updatable}',
  updateTableDataField: '/dataset/{:datasetId}/updateField?updateCascadePK={:updateInCascade}',
  updateTableDataRecord: '/dataset/{:datasetId}/updateRecord?updateCascadePK={:updateInCascade}',
  updateTableDesign: '/dataschema/{:datasetId}/tableSchema',
  validateDataset: '/validation/dataset/{:datasetId}',
  validationViewer: '/dataset/findPositionFromAnyObject/{:objectId}?datasetId={:datasetId}&type={:entityType}',
  validateSql: '/rules/validateSqlRules?datasetId={:datasetId}&datasetSchemaId={:datasetSchemaId}'
};
