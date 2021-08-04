import { ApiDatasetRepository } from 'repositories/_temp/model/Dataset/ApiDatasetRepository';

export const DatasetRepository = {
  addRecordFieldDesign: () => Promise.reject('[DatasetRepository#addRecordFieldDesign] must be implemented'),
  addRecordsById: () => Promise.reject('[DatasetRepository#addRecordsById] must be implemented'),
  addTableDesign: () => Promise.reject('[DatasetRepository#addTableDesign] must be implemented'),
  createValidation: () => Promise.reject('[DatasetRepository#createValidation] must be implemented'),
  deleteDataById: () => Promise.reject('[DatasetRepository#deleteDataById] must be implemented'),
  deleteFileData: () => Promise.reject('[DatasetRepository#deleteFileData] must be implemented'),
  deleteRecordById: () => Promise.reject('[DatasetRepository#deleteRecordById] must be implemented'),
  deleteRecordFieldDesign: () => Promise.reject('[DatasetRepository#deleteRecordFieldDesign] must be implemented'),
  deleteSchemaById: () => Promise.reject('[DatasetRepository#deleteSchemaById] must be implemented'),
  deleteTableDataById: () => Promise.reject('[DatasetRepository#deleteTableDataById] must be implemented'),
  deleteTableDesign: () => Promise.reject('[DatasetRepository#deleteTableDesign] must be implemented'),
  downloadDatasetFileData: () => Promise.reject('[DatasetRepository#downloadDatasetFileData] must be implemented'),
  downloadExportDatasetFile: () => Promise.reject('[DatasetRepository#downloadExportDatasetFile] must be implemented'),
  downloadExportFile: () => Promise.reject('[DatasetRepository#downloadExportFile] must be implemented'),
  downloadFileData: () => Promise.reject('[DatasetRepository#downloadFileData] must be implemented'),
  downloadReferenceDatasetFileData: () =>
    Promise.reject('[DatasetRepository#downloadReferenceDatasetFileData] must be implemented'),
  errorStatisticsById: () => Promise.reject('[DatasetRepository#errorStatisticsById] must be implemented'),
  exportDataById: () => Promise.reject('[DatasetRepository#exportDataById] must be implemented'),
  exportDatasetDataExternal: () => Promise.reject('[DatasetRepository#exportDatasetDataExternal] must be implemented'),
  exportTableDataById: () => Promise.reject('[DatasetRepository#exportTableDataById] must be implemented'),
  exportTableSchemaById: () => Promise.reject('[DatasetRepository#exportTableSchemaById] must be implemented'),
  getMetaData: () => Promise.reject('[DatasetRepository#getMetaData] must be implemented'),
  getReferencedFieldValues: () => Promise.reject('[DatasetRepository#getReferencedFieldValues] must be implemented'),
  groupedErrorsById: () => Promise.reject('[DatasetRepository#groupedErrorsById] must be implemented'),
  orderFieldSchema: () => Promise.reject('[DatasetRepository#orderFieldSchema] must be implemented'),
  orderTableSchema: () => Promise.reject('[DatasetRepository#orderTableSchema] must be implemented'),
  schemaById: () => Promise.reject('[DatasetRepository#schemaById] must be implemented'),
  tableDataById: () => Promise.reject('[DatasetRepository#tableDataById] must be implemented'),
  updateDatasetFeedbackStatus: () =>
    Promise.reject('[DatasetRepository#updateDatasetFeedbackStatus] must be implemented'),
  updateDatasetSchemaDescriptionDesign: () =>
    Promise.reject('[DatasetRepository#updateDatasetSchemaDescriptionDesign] must be implemented'),
  updateFieldById: () => Promise.reject('[DatasetRepository#updateFieldById] must be implemented'),
  updateRecordFieldDesign: () => Promise.reject('[DatasetRepository#updateRecordFieldDesign ] must be implemented'),
  updateRecordsById: () => Promise.reject('[DatasetRepository#updateRecordsById] must be implemented'),
  updateReferenceDatasetStatus: () => '[DatasetRepository#updateReferenceDatasetStatus] must be implemented',
  updateSchemaNameById: () => Promise.reject('[DatasetRepository#updateSchemaNameById] must be implemented'),
  updateTableDescriptionDesign: () =>
    Promise.reject('[DatasetRepository#updateTableDescriptionDesign] must be implemented'),
  updateTableDesign: () => Promise.reject('[DatasetRepository#updateTableDesign] must be implemented'),
  updateTableNameDesign: () => Promise.reject('[DatasetRepository#updateTableNameDesign] must be implemented'),
  validateDataById: () => Promise.reject('[DatasetRepository#validateDataById] must be implemented'),
  validateSqlRules: () => Promise.reject('[DatasetRepository#validateSqlRules] must be implemented')
};

export const datasetRepository = Object.assign({}, DatasetRepository, ApiDatasetRepository);
