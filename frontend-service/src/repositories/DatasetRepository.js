import { DatasetConfig } from './config/DatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const datasetRepository = {
  addRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    return await HTTPRequester.post({
      url: getUrl(DatasetConfig.addNewRecordFieldDesign, { datasetId }),
      data: datasetTableRecordField
    });
  },

  addRecordsById: async (datasetId, tableSchemaId, datasetTableRecords) => {
    return await HTTPRequester.post({
      url: getUrl(DatasetConfig.addNewRecord, { datasetId, tableSchemaId }),
      data: datasetTableRecords
    });
  },

  addTableDesign: async (datasetId, tableSchemaName) => {
    return await HTTPRequester.post({
      url: getUrl(DatasetConfig.addTableDesign, { datasetId }),
      data: { nameTableSchema: tableSchemaName, notEmpty: true }
    });
  },

  deleteDataById: async datasetId => {
    return await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteImportData, { datasetId }) });
  },

  deleteFileData: async (datasetId, fieldId) => {
    return await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteFileData, { datasetId, fieldId }) });
  },

  deleteRecordById: async (datasetId, recordId, deleteInCascade = false) => {
    return await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecord, { datasetId, deleteInCascade, recordId })
    });
  },

  deleteRecordFieldDesign: async (datasetId, fieldSchemaId) => {
    return await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecordFieldDesign, { datasetId, fieldSchemaId })
    });
  },

  deleteSchemaById: async datasetId => {
    return await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteDataSchema, { datasetId }) });
  },

  deleteTableDataById: async (datasetId, tableId) => {
    return await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteImportTable, { datasetId, tableId }) });
  },

  deleteTableDesign: async (datasetId, tableSchemaId) => {
    return await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteTableDesign, { datasetId, tableSchemaId }) });
  },

  downloadDatasetFileData: async (dataflowId, dataProviderId, fileName) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadDatasetFileData, { dataflowId, dataProviderId, fileName })
    });
  },

  downloadExportDatasetFile: async (datasetId, fileName) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadExportDatasetFile, { datasetId, fileName })
    });
  },

  downloadExportFile: async (datasetId, fileName, providerId = null) => {
    const url = providerId
      ? getUrl(DatasetConfig.downloadExportFile, { datasetId, fileName, providerId })
      : getUrl(DatasetConfig.downloadExportFileNoProviderId, { datasetId, fileName });
    return await HTTPRequester.download({ url });
  },

  downloadFileData: async (dataflowId, datasetId, fieldId, dataProviderId = null) => {
    const url = dataProviderId
      ? getUrl(DatasetConfig.downloadFileDataWithProviderId, {
          dataflowId,
          datasetId,
          fieldId,
          providerId: dataProviderId
        })
      : getUrl(DatasetConfig.downloadFileData, { dataflowId, datasetId, fieldId });
    return await HTTPRequester.download({ url });
  },

  downloadReferenceDatasetFileData: async (dataflowId, fileName) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadReferenceDatasetFileData, { dataflowId, fileName })
    });
  },

  exportDataById: async (datasetId, fileType) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, { datasetId, fileType }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  exportDatasetDataExternal: async (datasetId, integrationId) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataExternal, { datasetId, integrationId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  exportTableDataById: async (datasetId, tableSchemaId, fileType) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetTableData, { datasetId, fileType, tableSchemaId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  exportTableSchemaById: async (datasetId, datasetSchemaId, tableSchemaId, fileType) => {
    return await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportTableSchema, { datasetId, datasetSchemaId, fileType, tableSchemaId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
  },

  getMetaData: async datasetId => {
    return await HTTPRequester.get({ url: getUrl(DatasetConfig.datasetMetaData, { datasetId }) });
  },

  getReferencedFieldValues: async (
    datasetId,
    fieldSchemaId,
    searchToken,
    conditionalValue = '',
    datasetSchemaId = '',
    resultsNumber = ''
  ) => {
    return await HTTPRequester.get({
      url: getUrl(DatasetConfig.referencedFieldValues, {
        conditionalValue,
        datasetId,
        datasetSchemaId,
        fieldSchemaId,
        resultsNumber: resultsNumber !== '' ? resultsNumber : undefined,
        searchToken: searchToken !== '' ? searchToken : undefined
      })
    });
  },

  groupedErrorsById: async (
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    fieldValueFilter,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  ) => {
    if (asc === -1) {
      asc = 0;
    }
    return await HTTPRequester.get({
      url: getUrl(DatasetConfig.listGroupedValidations, {
        datasetId,
        pageNum,
        pageSize,
        sortField,
        asc,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tableFilter: tablesFilter
      })
    });
  },

  orderFieldSchema: async (datasetId, position, fieldSchemaId) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.orderFieldSchemaDesign, { datasetId, position }),
      data: { id: fieldSchemaId, position }
    });
  },

  orderTableSchema: async (datasetId, position, tableSchemaId) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.orderTableSchemaDesign, { datasetId, position }),
      data: { id: tableSchemaId, position }
    });
  },

  schemaById: async datasetId => await HTTPRequester.get({ url: getUrl(DatasetConfig.dataSchema, { datasetId }) }),

  statisticsById: async datasetId => {
    return await HTTPRequester.get({ url: getUrl(DatasetConfig.loadStatistics, { datasetId }) });
  },

  tableDataById: async (
    datasetId,
    tableSchemaId,
    pageNum,
    pageSize,
    fields,
    levelError,
    ruleId,
    fieldSchemaId,
    value
  ) => {
    return await HTTPRequester.get({
      url: getUrl(DatasetConfig.loadTableData, {
        datasetId,
        fields,
        fieldSchemaId,
        idRules: ruleId,
        levelError,
        pageNum,
        pageSize,
        tableSchemaId,
        value
      })
    });
  },

  updateDatasetFeedbackStatus: async (dataflowId, datasetId, message, feedbackStatus) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetFeedbackStatus),
      data: { dataflowId, datasetId, message, status: feedbackStatus }
    });
  },

  updateDatasetSchemaById: async (datasetId, datasetSchema) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetSchemaDesign, { datasetId }),
      data: datasetSchema
    });
  },

  updateFieldById: async (datasetId, datasetTableRecords, updateInCascade = false) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDataField, { datasetId, updateInCascade }),
      data: datasetTableRecords
    });
  },

  updateRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateRecordFieldDesign, { datasetId }),
      data: datasetTableRecordField
    });
  },

  updateRecordsById: async (datasetId, datasetTableRecords, updateInCascade = false) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDataRecord, { datasetId, updateInCascade }),
      data: datasetTableRecords
    });
  },

  updateReferenceDatasetStatus: async (datasetId, updatable) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateReferenceDatasetStatus, { datasetId, updatable })
    });
  },

  updateSchemaNameById: async (datasetId, datasetSchemaName) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDataSchemaName, { datasetId, datasetSchemaName })
    });
  },

  updateTableDescriptionDesign: async (
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
  ) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: {
        description: tableSchemaDescription,
        fixedNumber: tableSchemaFixedNumber,
        idTableSchema: tableSchemaId,
        notEmpty: tableSchemaNotEmpty,
        readOnly: tableSchemaIsReadOnly,
        toPrefill: tableSchemaToPrefill
      }
    });
  },

  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: { idTableSchema: tableSchemaId, nameTableSchema: tableSchemaName }
    });
  },

  validateById: async datasetId => {
    return await HTTPRequester.update({ url: getUrl(DatasetConfig.validateDataset, { datasetId }) });
  },

  validateSqlRules: async (datasetId, datasetSchemaId) => {
    return await HTTPRequester.post({ url: getUrl(DatasetConfig.validateSql, { datasetId, datasetSchemaId }) });
  }
};
