import { DatasetConfig } from 'conf/domain/model/Dataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDataset = {
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
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadDatasetFileData, { dataflowId, dataProviderId, fileName })
    });
    return response.data;
  },

  downloadExportFile: async (datasetId, fileName, providerId = null) => {
    const url = providerId
      ? getUrl(DatasetConfig.downloadExportFile, { datasetId, fileName, providerId })
      : getUrl(DatasetConfig.downloadExportFileNoProviderId, { datasetId, fileName });
    const response = await HTTPRequester.download({ url });
    return response.data;
  },

  downloadFileData: async (datasetId, fieldId) => {
    return await HTTPRequester.download({ url: getUrl(DatasetConfig.downloadFileData, { datasetId, fieldId }) });
  },

  errorPositionByObjectId: async (objectId, datasetId, entityType) => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.validationViewer, { objectId: objectId, datasetId: datasetId, entityType: entityType })
    });
    return response.data;
  },

  errorsById: async (
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
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.listValidations, {
        datasetId: datasetId,
        pageNum: pageNum,
        pageSize: pageSize,
        sortField: sortField,
        asc: asc,
        fieldValueFilter: fieldValueFilter,
        levelErrorsFilter: levelErrorsFilter,
        typeEntitiesFilter: typeEntitiesFilter,
        tableFilter: tablesFilter
      })
    });
    return response.data;
  },

  exportDataById: async (datasetId, fileType) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, { datasetId: datasetId, fileType: fileType }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
    return response.data;
  },

  exportDatasetDataExternal: async (datasetId, integrationId) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataExternal, { datasetId, integrationId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
    return response.data;
  },

  exportTableDataById: async (datasetId, tableSchemaId, fileType) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetTableData, { datasetId, tableSchemaId, fileType }),
      headers: { 'Content-Type': 'application/octet-stream' }
    });
    return response.data;
  },

  getMetaData: async datasetId => {
    const response = await HTTPRequester.get({ url: getUrl(DatasetConfig.datasetMetaData, { datasetId }) });
    return response.data;
  },

  getReferencedFieldValues: async (
    datasetId,
    fieldSchemaId,
    searchToken,
    conditionalValue = '',
    datasetSchemaId = '',
    resultsNumber = ''
  ) => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.referencedFieldValues, {
        conditionalValue,
        datasetId,
        datasetSchemaId,
        fieldSchemaId,
        resultsNumber: resultsNumber !== '' ? resultsNumber : undefined,
        searchToken: searchToken !== '' ? searchToken : undefined
      })
    });
    return response.data;
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
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.listGroupedValidations, {
        datasetId: datasetId,
        pageNum: pageNum,
        pageSize: pageSize,
        sortField: sortField,
        asc: asc,
        fieldValueFilter: fieldValueFilter,
        levelErrorsFilter: levelErrorsFilter,
        typeEntitiesFilter: typeEntitiesFilter,
        tableFilter: tablesFilter
      })
    });
    return response.data;
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
    const response = await HTTPRequester.get({ url: getUrl(DatasetConfig.loadStatistics, { datasetId: datasetId }) });
    return response.data;
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
    try {
      const response = await HTTPRequester.update({
        url: getUrl(DatasetConfig.updateDatasetSchemaDesign, { datasetId }),
        data: datasetSchema
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
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
