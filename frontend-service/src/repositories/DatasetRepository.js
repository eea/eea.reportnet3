import isEmpty from 'lodash/isEmpty';
import { DatasetConfig } from './config/DatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DatasetRepository = {
  createRecordDesign: async (datasetId, datasetTableRecordField) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.createRecordDesign, { datasetId }),
      data: datasetTableRecordField
    }),

  createRecord: async (datasetId, tableSchemaId, datasetTableRecords) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.createRecord, { datasetId, tableSchemaId }),
      data: datasetTableRecords
    }),

  createTableDesign: async (datasetId, tableSchemaName) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.createTableDesign, { datasetId }),
      data: { nameTableSchema: tableSchemaName, notEmpty: true }
    }),

  deleteData: async (datasetId, deletePrefilledTables) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteData, { datasetId, deletePrefilledTables }) }),

  deleteAttachment: async (dataflowId, datasetId, fieldId, dataProviderId = null) =>
    await HTTPRequester.delete({
      url: dataProviderId
        ? getUrl(DatasetConfig.deleteAttachmentWithProviderId, {
            dataflowId,
            datasetId,
            fieldId,
            providerId: dataProviderId
          })
        : getUrl(DatasetConfig.deleteAttachment, { dataflowId, datasetId, fieldId })
    }),

  deleteRecord: async (datasetId, recordId, deleteInCascade = false) =>
    await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecord, { datasetId, deleteInCascade, recordId })
    }),

  deleteFieldDesign: async (datasetId, fieldSchemaId) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteFieldDesign, { datasetId, fieldSchemaId }) }),

  deleteSchema: async datasetId =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteSchema, { datasetId }) }),

  deleteTableData: async (datasetId, tableId) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteTableData, { datasetId, tableId }) }),

  deleteTableDesign: async (datasetId, tableSchemaId) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteTableDesign, { datasetId, tableSchemaId }) }),

  downloadPublicDatasetFile: async (dataflowId, dataProviderId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadPublicDatasetFile, { dataflowId, dataProviderId, fileName })
    }),

  downloadExportDatasetFile: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadExportDatasetFile, { datasetId, fileName })
    }),

  downloadExportFile: async (datasetId, fileName, providerId = null) =>
    await HTTPRequester.download({
      url: providerId
        ? getUrl(DatasetConfig.downloadExportFileWithProviderId, { datasetId, fileName, providerId })
        : getUrl(DatasetConfig.downloadExportFile, { datasetId, fileName })
    }),

  downloadFileData: async (dataflowId, datasetId, fieldId, dataProviderId = null) =>
    await HTTPRequester.download({
      url: dataProviderId
        ? getUrl(DatasetConfig.downloadFileDataWithProviderId, {
            dataflowId,
            datasetId,
            fieldId,
            providerId: dataProviderId
          })
        : getUrl(DatasetConfig.downloadFileData, { dataflowId, datasetId, fieldId })
    }),

  downloadPublicReferenceDatasetFileData: async (dataflowId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadPublicReferenceDatasetFileData, { dataflowId, fileName })
    }),

  downloadTableData: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadTableData, { datasetId, fileName })
    }),

  downloadTableDefinitions: async datasetSchemaId =>
    await HTTPRequester.download({ url: getUrl(DatasetConfig.downloadTableDefinitions, { datasetSchemaId }) }),

  exportDatasetData: async (datasetId, fileType) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, { datasetId, fileType }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  exportDatasetDataExternal: async (datasetId, integrationId) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataExternal, { datasetId, integrationId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  exportTableData: async (
    datasetId,
    tableSchemaId,
    fileType,
    filterValue,
    isFilterValidationsActive,
    levelErrorValidations,
    selectedRuleId
  ) => {
    // if (isEmpty(filterValue) && isEmpty(selectedRuleId) && !isFilterValidationsActive && fileType !== 'csv+filters') {
    if (fileType !== 'csv+filters') {
      return await HTTPRequester.download({
        url: getUrl(DatasetConfig.exportTableData, { datasetId, fileType, tableSchemaId }),
        headers: { 'Content-Type': 'application/octet-stream' }
      });
    } else {
      return await HTTPRequester.post({
        url: getUrl(DatasetConfig.exportTableDataFiltered, { datasetId, fileType, tableSchemaId }),
        data: {
          fieldValue: filterValue,
          idRules: selectedRuleId,
          levelError: levelErrorValidations
        },
        headers: { 'Content-Type': 'application/octet-stream' }
      });
    }
  },

  exportTableSchema: async (datasetId, datasetSchemaId, tableSchemaId, fileType) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportTableSchema, { datasetId, datasetSchemaId, fileType, tableSchemaId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  getMetadata: async datasetId => await HTTPRequester.get({ url: getUrl(DatasetConfig.getMetadata, { datasetId }) }),

  getReferencedFieldValues: async (
    datasetId,
    fieldSchemaId,
    searchToken,
    conditionalValue = '',
    datasetSchemaId = '',
    resultsNumber = ''
  ) =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getReferencedFieldValues, {
        conditionalValue,
        datasetId,
        datasetSchemaId,
        fieldSchemaId,
        resultsNumber: resultsNumber !== '' ? resultsNumber : undefined,
        searchToken: searchToken !== '' ? searchToken : undefined
      })
    }),

  getShowValidationErrors: async (
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
      url: getUrl(DatasetConfig.getShowValidationErrors, {
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

  updateFieldOrder: async (datasetId, position, fieldSchemaId) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateFieldOrder, { datasetId, position }),
      data: { id: fieldSchemaId, position }
    }),

  updateTableOrder: async (datasetId, position, tableSchemaId) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableOrder, { datasetId, position }),
      data: { id: tableSchemaId, position }
    }),

  getSchema: async datasetId => await HTTPRequester.get({ url: getUrl(DatasetConfig.getSchema, { datasetId }) }),

  getStatistics: async datasetId =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getStatistics, { datasetId }) }),

  getTableData: async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError, ruleId, fieldSchemaId, value) =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getTableData, {
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
    }),

  updateDatasetFeedbackStatus: async (dataflowId, datasetId, message, feedbackStatus) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetFeedbackStatus),
      data: { dataflowId, datasetId, message, status: feedbackStatus }
    }),

  updateDatasetDesign: async (datasetId, datasetSchema) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetDesign, { datasetId }),
      data: datasetSchema
    }),

  updateField: async (datasetId, datasetTableRecords, updateInCascade = false) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateField, { datasetId, updateInCascade }),
      data: datasetTableRecords
    }),

  updateFieldDesign: async (datasetId, datasetTableRecordField) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateFieldDesign, { datasetId }),
      data: datasetTableRecordField
    }),

  updateRecord: async (datasetId, datasetTableRecords, updateInCascade = false) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateRecord, { datasetId, updateInCascade }),
      data: datasetTableRecords
    }),

  updateReferenceDatasetStatus: async (datasetId, updatable) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateReferenceDatasetStatus, { datasetId, updatable })
    }),

  updateDatasetNameDesign: async (datasetId, datasetSchemaName) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetNameDesign, { datasetId, datasetSchemaName })
    }),

  updateTableDesign: async (
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber
  ) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: {
        description: tableSchemaDescription,
        fixedNumber: tableSchemaFixedNumber,
        idTableSchema: tableSchemaId,
        notEmpty: tableSchemaNotEmpty,
        readOnly: tableSchemaIsReadOnly,
        toPrefill: tableSchemaToPrefill
      }
    }),

  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: { idTableSchema: tableSchemaId, nameTableSchema: tableSchemaName }
    }),

  validate: async datasetId => await HTTPRequester.update({ url: getUrl(DatasetConfig.validate, { datasetId }) }),

  validateSqlRules: async (datasetId, datasetSchemaId) =>
    await HTTPRequester.post({ url: getUrl(DatasetConfig.validateSql, { datasetId, datasetSchemaId }) })
};
