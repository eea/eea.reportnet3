import { DatasetConfig } from './config/DatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DatasetRepository = {
  testImportProcess: async datasetId =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.testImportProcess, { datasetId })
    }),

  enableEditing: async ({ datasetId }) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.enableEditing, { datasetId })
    }),

  disableEditing: async ({ datasetId }) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.disableEditing, { datasetId })
    }),

  convertParquetsToIcebergs: async ({ dataflowId, datasetId, providerId }) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.convertParquetsToIcebergs, { dataflowId, datasetId, providerId })
    }),

  convertIcebergsToParquets: async ({ dataflowId, datasetId, providerId }) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.convertIcebergsToParquets, { dataflowId, datasetId, providerId })
    }),

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

  deleteData: async ({ datasetId, deletePrefilledTables, preparationCode }) =>
    await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteData, { datasetId, deletePrefilledTables, preparationCode })
    }),

  deleteAttachment: async ({
    dataflowId,
    datasetId,
    fieldId,
    dataProviderId = null,
    tableSchemaName,
    fieldName,
    fileName,
    recordId
  }) => {
    const encodedFileName = encodeURIComponent(fileName);
    return await HTTPRequester.delete({
      url: dataProviderId
        ? getUrl(DatasetConfig.deleteAttachmentWithProviderId, {
            dataflowId,
            datasetId,
            fieldId,
            providerId: dataProviderId,
            tableSchemaName,
            fieldName,
            fileName: encodedFileName,
            recordId
          })
        : getUrl(DatasetConfig.deleteAttachment, {
            dataflowId,
            datasetId,
            fieldId,
            tableSchemaName,
            fieldName,
            fileName: encodedFileName,
            recordId
          })
    });
  },

  deleteRecord: async ({ datasetId, selectedRecordId, tableId, updateInCascade = false }) =>
    await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecord, { datasetId, updateInCascade, selectedRecordId, tableId })
    }),

  deleteFieldDesign: async (datasetId, fieldSchemaId) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteFieldDesign, { datasetId, fieldSchemaId }) }),

  deleteSchema: async datasetId =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteSchema, { datasetId }) }),

  deleteTableData: async ({ datasetId, tableId, preparationCode }) =>
    await HTTPRequester.delete({ url: getUrl(DatasetConfig.deleteTableData, { datasetId, tableId, preparationCode }) }),

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

  downloadExportDatasetFileDL: async (datasetId, fileName, preparationCode) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadExportDatasetFileDL, { datasetId, fileName, preparationCode })
    }),

  downloadExportFile: async (datasetId, fileName, providerId = null) =>
    await HTTPRequester.download({
      url: providerId
        ? getUrl(DatasetConfig.downloadExportFileWithProviderId, { datasetId, fileName, providerId })
        : getUrl(DatasetConfig.downloadExportFile, { datasetId, fileName })
    }),

  downloadFileData: async ({
    dataflowId,
    datasetId,
    fieldId,
    dataProviderId = null,
    fileName,
    recordId,
    tableSchemaName,
    fieldName,
    providerCode
  }) => {
    const encodedFileName = encodeURIComponent(fileName);

    return await HTTPRequester.download({
      url: dataProviderId
        ? getUrl(DatasetConfig.downloadFileDataWithProviderId, {
            dataflowId,
            datasetId,
            fieldId,
            providerId: dataProviderId,
            fileName: encodedFileName,
            recordId,
            tableSchemaName,
            fieldName,
            providerCode
          })
        : getUrl(DatasetConfig.downloadFileData, {
            dataflowId,
            datasetId,
            fieldId,
            fileName,
            recordId,
            tableSchemaName,
            fieldName,
            providerCode
          })
    });
  },

  downloadGeometry: async ({ datasetId, recordId, fieldId, dataflowId, tableSchemaId, providerId }) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadGeometry, {
        datasetId,
        recordId,
        fieldId,
        dataflowId,
        tableSchemaId,
        providerId
      })
    }),

  downloadPublicReferenceDatasetFileData: async (dataflowId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadPublicReferenceDatasetFileData, { dataflowId, fileName })
    }),

  downloadTableData: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadTableData, { datasetId, fileName })
    }),

  downloadTableDataDL: async (datasetId, fileName, preparationCode) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadTableDataDL, { datasetId, fileName, preparationCode })
    }),

  downloadTableDefinitions: async datasetSchemaId =>
    await HTTPRequester.download({ url: getUrl(DatasetConfig.downloadTableDefinitions, { datasetSchemaId }) }),

  exportDatasetData: async (datasetId, fileType) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, { datasetId, fileType }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  getImportedFiles: async datasetId =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getImportedFiles, { datasetId })
    }),

  downloadImportedFile: async (fileName, datasetId, dataflowId) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.downloadImportedFile, { fileName, datasetId, dataflowId })
    }),

  exportDatasetDataDL: async (datasetId, fileType, preparationCode) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataDL, { datasetId, fileType, preparationCode }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  exportDatasetDataExternal: async (datasetId, integrationId, preparationCode) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataExternal, { datasetId, integrationId, preparationCode }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  exportTableData: async (
    datasetId,
    tableSchemaId,
    fileType,
    filterValue,
    levelErrorValidations,
    selectedRuleId,
    isExportFilteredCsv,
    isFilterValidationsActive
  ) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.exportTableData, { datasetId, fileType, tableSchemaId }),
      data: {
        fieldValue: isExportFilteredCsv ? filterValue : '',
        idRules: isExportFilteredCsv ? selectedRuleId : '',
        levelError: isExportFilteredCsv && isFilterValidationsActive ? levelErrorValidations : []
      },
      headers: { 'Content-Type': 'application/json' }
    }),

  exportTableDataDL: async (
    datasetId,
    tableSchemaId,
    fileType,
    filterValue,
    levelErrorValidations,
    selectedShortCode,
    isExportFilteredCsv,
    isFilterValidationsActive,
    preparationCode
  ) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.exportTableDataDL, { datasetId, fileType, tableSchemaId, preparationCode }),
      data: {
        fieldValue: isExportFilteredCsv ? filterValue : '',
        qcCodes: isExportFilteredCsv ? selectedShortCode : '',
        levelError: isExportFilteredCsv && isFilterValidationsActive ? levelErrorValidations : []
      },
      headers: { 'Content-Type': 'application/json' }
    }),

  exportTableSchema: async (datasetId, datasetSchemaId, tableSchemaId, fileType) =>
    await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportTableSchema, { datasetId, datasetSchemaId, fileType, tableSchemaId }),
      headers: { 'Content-Type': 'application/octet-stream' }
    }),

  getEditingStatus: async ({ datasetId }) =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getEditingStatus, { datasetId }) }),

  getMetadata: async datasetId => await HTTPRequester.get({ url: getUrl(DatasetConfig.getMetadata, { datasetId }) }),

  getIsIcebergTableCreated: async ({ datasetId, tableSchemaId }) =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getIsIcebergTableCreated, { datasetId, tableSchemaId }) }),

  getIsAvailableForManualEditing: async ({ datasetId }) =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getIsAvailableForManualEditing, { datasetId }) }),

  getIsEdited: async ({ datasetId }) =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getIsEdited, { datasetId }) }),

  getTableImportedMetadata: async ({ datasetId }) =>
    await HTTPRequester.get({ url: getUrl(DatasetConfig.getTableImportedMetadata, { datasetId }) }),

  getAlignmentBetween: async (datasetId, selectedRepresentativesCode, selectedTable) =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getAlignmentBetween, { datasetId, selectedRepresentativesCode, selectedTable })
    }),

  getPresignedUrl: async ({
    datasetId,
    dataflowId,
    providerId,
    tableSchemaId,
    replace,
    integrationId,
    delimiter,
    fileName,
    code
  }) =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getPresignedUrl, {
        datasetId,
        dataflowId,
        providerId,
        tableSchemaId,
        replace,
        integrationId,
        delimiter,
        fileName,
        code
      })
    }),

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
        searchToken: encodeURIComponent(searchToken) !== '' ? encodeURIComponent(searchToken) : undefined
      })
    }),

  getShowValidationErrors: async (
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    shortCodeFilter,
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
        shortCodeFilter,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tableFilter: tablesFilter
      })
    });
  },

  getShowValidationErrorsDL: async (
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    shortCodeFilter,
    fieldValueFilter,
    levelErrorsFilter,
    typeEntitiesFilter,
    tablesFilter
  ) => {
    if (asc === -1) {
      asc = 0;
    }
    return await HTTPRequester.get({
      url: getUrl(DatasetConfig.getShowValidationErrorsDL, {
        datasetId,
        pageNum,
        pageSize,
        sortField,
        asc,
        shortCodeFilter,
        fieldValueFilter,
        levelErrorsFilter,
        typeEntitiesFilter,
        tableFilter: tablesFilter
      })
    });
  },

  handleStuckImportJob: async (jobId, error) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.handleStuckImportJob, { jobId }),
      data: { error }
    }),

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

  getAddUserText: async dataflowId =>
    await HTTPRequester.get({
      url: getUrl(DatasetConfig.getAddUserText, { dataflowId })
    }),

  getSchema: async datasetId => {
    const response = await HTTPRequester.get({ url: getUrl(DatasetConfig.getSchema, { datasetId }) });
    return response;
  },

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

  getTableDataDL: async (
    datasetId,
    tableSchemaId,
    pageNum,
    pageSize,
    fields,
    levelError,
    qcCodes,
    fieldSchemaId,
    value,
    code
  ) => {
    return await HTTPRequester.get({
      url: code
        ? getUrl(DatasetConfig.getPreparationsTableDataDL, {
            datasetId,
            fields,
            fieldSchemaId,
            qcCodes,
            levelError,
            pageNum,
            pageSize,
            tableSchemaId,
            value,
            code
          })
        : getUrl(DatasetConfig.getTableDataDL, {
            datasetId,
            fields,
            fieldSchemaId,
            qcCodes,
            levelError,
            pageNum,
            pageSize,
            tableSchemaId,
            value
          })
    });
  },
  checkDuplicateValues: async (datasetId, tableSchemaId, fieldVO) =>
    // When sending an object( like fieldVO ), a POST call must be sent to the backend.
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.checkDuplicateValues, { datasetId, tableSchemaId }),
      data: fieldVO
    }),

  importTableFileWithS3: async ({
    datasetId,
    dataflowId,
    providerId,
    tableSchemaId,
    replace,
    integrationId,
    delimiter,
    jobId,
    code
  }) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.importTableFileWithS3, {
        datasetId,
        dataflowId,
        providerId,
        tableSchemaId,
        replace,
        integrationId,
        delimiter,
        jobId,
        code
      })
    }),

  restorePrefilledTables: async ({ datasetId, tableSchemaId }) =>
    await HTTPRequester.post({
      url: getUrl(DatasetConfig.restorePrefilledTables, { datasetId, tableSchemaId })
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

  updateAddUserText: async note =>
    await HTTPRequester.update({
      url: DatasetConfig.updateAddUserText,
      data: note
    }),

  updateConditionalFieldsWebform: async (
    datasetId,
    recordId,
    tableSchemaId,
    datasetTableFields,
    updateInCascade = false
  ) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateConditionalFieldsWebform, {
        datasetId,
        recordId,
        tableSchemaId,
        updateInCascade
      }),
      data: datasetTableFields
    }),

  updateField: async (datasetId, recordId, tableSchemaId, datasetTableRecords, updateInCascade = false) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateField, { datasetId, recordId, tableSchemaId, updateInCascade }),
      data: datasetTableRecords
    }),

  updateFieldDesign: async (datasetId, datasetTableRecordField) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateFieldDesign, { datasetId }),
      data: datasetTableRecordField
    }),

  updateRecord: async (datasetId, datasetTableRecords, tableSchemaId, updateInCascade = false) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateRecord, { datasetId, tableSchemaId, updateInCascade }),
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
  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: { idTableSchema: tableSchemaId, nameTableSchema: tableSchemaName }
    }),

  updateTableDesign: async (
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId,
    tableSchemaNotEmpty,
    tableSchemaFixedNumber,
    dataAreManuallyEditable
  ) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDesign, { datasetId }),
      data: {
        description: tableSchemaDescription,
        fixedNumber: tableSchemaFixedNumber,
        idTableSchema: tableSchemaId,
        notEmpty: tableSchemaNotEmpty,
        readOnly: tableSchemaIsReadOnly,
        toPrefill: tableSchemaToPrefill,
        dataAreManuallyEditable
      }
    }),
  validate: async datasetId => await HTTPRequester.update({ url: getUrl(DatasetConfig.validate, { datasetId }) }),

  validateAsProvider: async (datasetId, dataflowId, providerId) =>
    await HTTPRequester.update({
      url: getUrl(DatasetConfig.validateAsProvider, { datasetId, dataflowId, providerId })
    }),

  validateAllSql: async datasetId =>
    await HTTPRequester.post({ url: getUrl(DatasetConfig.validateAllSql, { datasetId }) }),

  validateSqlRules: async (datasetId, datasetSchemaId) =>
    await HTTPRequester.post({ url: getUrl(DatasetConfig.validateSql, { datasetId, datasetSchemaId }) })
};
