import { DatasetConfig } from 'conf/domain/model/Dataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

import isNil from 'lodash/isNil';

export const apiDataset = {
  addRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    try {
      const response = await HTTPRequester.post({
        url: getUrl(DatasetConfig.addNewRecordFieldDesign, {
          datasetId
        }),
        data: datasetTableRecordField
      });

      return response;
    } catch (error) {
      console.error(`Error adding record to dataset design data: ${error}`);
      return false;
    }
  },
  addRecordsById: async (datasetId, tableSchemaId, datasetTableRecords) => {
    const response = await HTTPRequester.post({
      url: getUrl(DatasetConfig.addNewRecord, {
        datasetId: datasetId,
        tableSchemaId: tableSchemaId
      }),
      data: datasetTableRecords
    });

    return response;
  },
  addTableDesign: async (datasetId, tableSchemaName) => {
    try {
      const response = await HTTPRequester.post({
        url: getUrl(DatasetConfig.addTableDesign, {
          datasetId
        }),
        data: { nameTableSchema: tableSchemaName, notEmpty: true }
      });
      return response;
    } catch (error) {
      console.error(`Error adding table to dataset design data: ${error}`);
      return false;
    }
  },
  deleteDataById: async datasetId => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteImportData, {
        datasetId: datasetId
      })
    });

    return response;
  },
  deleteFileData: async (datasetId, fieldId) => {
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(DatasetConfig.deleteFileData, {
          datasetId,
          fieldId
        })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting file data: ${error}`);
      return false;
    }
  },
  deleteRecordById: async (datasetId, recordId, deleteInCascade = false) => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecord, { datasetId, deleteInCascade, recordId })
    });
    return response;
  },
  deleteRecordFieldDesign: async (datasetId, fieldSchemaId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteRecordFieldDesign, {
        datasetId,
        fieldSchemaId
      })
    });
    return response;
  },
  deleteSchemaById: async datasetId => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteDataSchema, {
        datasetId
      })
    });
    return response.status;
  },
  deleteTableDataById: async (datasetId, tableId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteImportTable, {
        datasetId: datasetId,
        tableId: tableId
      })
    });

    return response;
  },

  deleteTableDesign: async (datasetId, tableSchemaId) => {
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(DatasetConfig.deleteTableDesign, {
          datasetId,
          tableSchemaId
        })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table design data: ${error}`);
      return false;
    }
  },

  downloadExportFile: async (datasetId, fileName, providerId = null) => {
    const url = providerId
      ? getUrl(DatasetConfig.downloadExportFile, {
          datasetId,
          fileName,
          providerId
        })
      : getUrl(DatasetConfig.downloadExportFileNoProviderId, {
          datasetId,
          fileName
        });

    const response = await HTTPRequester.download({
      url
    });

    return response.data;
  },

  downloadFileData: async (datasetId, fieldId) => {
    try {
      const response = await HTTPRequester.download({
        url: getUrl(DatasetConfig.downloadFileData, {
          datasetId,
          fieldId
        })
      });

      return response.data;
    } catch (error) {
      console.error(`Error getting file data: ${error}`);
      return false;
    }
  },
  errorPositionByObjectId: async (objectId, datasetId, entityType) => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.validationViewer, {
        objectId: objectId,
        datasetId: datasetId,
        entityType: entityType
      })
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
      url: getUrl(DatasetConfig.exportDatasetData, {
        datasetId: datasetId,
        fileType: fileType
      }),
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },

  exportDatasetDataExternal: async (datasetId, integrationId) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetDataExternal, {
        datasetId,
        integrationId
      }),
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  exportTableDataById: async (datasetId, tableSchemaId, fileType) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetTableData, {
        datasetId: datasetId,
        tableSchemaId: tableSchemaId,
        fileType: fileType
      }),
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  getMetaData: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.datasetMetaData, {
        datasetId
      })
    });
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
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.orderFieldSchemaDesign, {
        datasetId,
        position
      }),
      data: { id: fieldSchemaId, position }
    });
    return response.status >= 200 && response.status <= 299;
  },
  orderTableSchema: async (datasetId, position, tableSchemaId) => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.orderTableSchemaDesign, {
        datasetId,
        position
      }),
      data: { id: tableSchemaId, position }
    });
    return response.status >= 200 && response.status <= 299;
  },
  schemaById: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.dataSchema, {
        datasetId
      })
    });
    return response.data;
  },
  statisticsById: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.loadStatistics, {
        datasetId: datasetId
      })
    });
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
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.dataViewer, {
        datasetId: datasetId,
        fields: fields,
        fieldSchemaId,
        idRules: ruleId,
        levelError: levelError,
        pageNum: pageNum,
        pageSize: pageSize,
        tableSchemaId: tableSchemaId,
        value
      })
    });

    return response.data;
  },
  updateDatasetFeedbackStatus: async (dataflowId, datasetId, message, feedbackStatus) => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDatasetFeedbackStatus),
      data: { dataflowId, datasetId, message, status: feedbackStatus }
    });

    return response;
  },
  updateFieldById: async (datasetId, datasetTableRecords, updateInCascade = false) => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDataField, { datasetId, updateInCascade }),
      data: datasetTableRecords
    });

    return response;
  },

  updateRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    try {
      const response = await HTTPRequester.update({
        url: getUrl(DatasetConfig.updateRecordFieldDesign, {
          datasetId
        }),
        data: datasetTableRecordField
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset record design: ${error}`);
      return false;
    }
  },
  updateRecordsById: async (datasetId, datasetTableRecords, updateInCascade = false) => {
    return await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateTableDataRecord, { datasetId, updateInCascade }),
      data: datasetTableRecords
    });
  },
  updateDatasetSchemaById: async (datasetId, datasetSchema) => {
    try {
      const response = await HTTPRequester.update({
        url: getUrl(DatasetConfig.updateDatasetSchemaDesign, {
          datasetId
        }),
        data: datasetSchema
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  updateSchemaNameById: async (datasetId, datasetSchemaName) => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDataSchemaName, {
        datasetId,
        datasetSchemaName
      })
    });
    return response.status;
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
    try {
      const response = await HTTPRequester.update({
        url: getUrl(DatasetConfig.updateTableDesign, {
          datasetId
        }),
        data: {
          idTableSchema: tableSchemaId,
          description: tableSchemaDescription,
          fixedNumber: tableSchemaFixedNumber,
          notEmpty: tableSchemaNotEmpty,
          readOnly: tableSchemaIsReadOnly,
          toPrefill: tableSchemaToPrefill
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) => {
    try {
      const response = await HTTPRequester.update({
        url: getUrl(DatasetConfig.updateTableDesign, {
          datasetId
        }),
        data: {
          idTableSchema: tableSchemaId,
          nameTableSchema: tableSchemaName
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  validateById: async datasetId => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.validateDataset, {
        datasetId: datasetId
      })
    });

    return response;
  },
  validateSqlRules: async (datasetId, datasetSchemaId) => {
    try {
      const response = await HTTPRequester.post({
        url: getUrl(DatasetConfig.validateSql, {
          datasetId,
          datasetSchemaId
        })
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error calling sql rules validation: ${error}`);
      return false;
    }
  }
};
