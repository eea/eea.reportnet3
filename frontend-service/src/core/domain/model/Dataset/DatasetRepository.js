import { ApiDatasetRepository } from 'core/infrastructure/domain/model/Dataset/ApiDatasetRepository';

export const DatasetRepository = {
  addRecordsById: () => Promise.reject('[DatasetRepository#addRecordsById] must be implemented'),
  addTableDesign: () => Promise.reject('[DatasetRepository#addTableDesign] must be implemented'),
  createValidation: () => Promise.reject('[DatasetRepository#createValidation] must be implemented'),
  deleteDataById: () => Promise.reject('[DatasetRepository#deleteDataById] must be implemented'),
  deleteRecordById: () => Promise.reject('[DatasetRepository#deleteRecordById] must be implemented'),
  deleteSchemaById: () => Promise.reject('[DatasetRepository#deleteSchemaById] must be implemented'),
  deleteTableDataById: () => Promise.reject('[DatasetRepository#deleteTableDataById] must be implemented'),
  deleteTableDesign: () => Promise.reject('[DatasetRepository#deleteTableDesign] must be implemented'),
  errorPositionByObjectId: () => Promise.reject('[DatasetRepository#errorPositionByObjectId] must be implemented'),
  errorsById: () => Promise.reject('[DatasetRepository#errorsById] must be implemented'),
  errorStatisticsById: () => Promise.reject('[DatasetRepository#errorStatisticsById] must be implemented'),
  exportDataById: () => Promise.reject('[DatasetRepository#exportDataById] must be implemented'),
  exportTableDataById: () => Promise.reject('[DatasetRepository#exportTableDataById] must be implemented'),
  getFileExtensions: () => Promise.reject('[DatasetRepository#getFileExtensions] must be implemented'),
  getMetaData: () => Promise.reject('[DatasetRepository#getMetaData] must be implemented'),
  getReferencedFieldValues: () => Promise.reject('[DatasetRepository#getReferencedFieldValues] must be implemented'),
  orderFieldSchema: () => Promise.reject('[DatasetRepository#orderFieldSchema] must be implemented'),
  orderTableSchema: () => Promise.reject('[DatasetRepository#orderTableSchema] must be implemented'),
  schemaById: () => Promise.reject('[DatasetRepository#schemaById] must be implemented'),
  tableDataById: () => Promise.reject('[DatasetRepository#tableDataById] must be implemented'),
  updateFieldById: () => Promise.reject('[DatasetRepository#updateFieldById] must be implemented'),
  updateRecordsById: () => Promise.reject('[DatasetRepository#updateRecordsById] must be implemented'),
  updateSchemaNameById: () => Promise.reject('[DatasetRepository#updateSchemaNameById] must be implemented'),
  updateDatasetSchemaDescriptionDesign: () =>
    Promise.reject('[DatasetRepository#updateDatasetSchemaDescriptionDesign] must be implemented'),
  updateTableDesign: () => Promise.reject('[DatasetRepository#updateTableDesign] must be implemented'),
  validateDataById: () => Promise.reject('[DatasetRepository#validateDataById] must be implemented'),
  webFormDataById: () => Promise.reject('[DatasetRepository#webFormDataById] must be implemented')
};

export const datasetRepository = Object.assign({}, DatasetRepository, ApiDatasetRepository);
