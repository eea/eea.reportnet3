import { DatasetConfig } from 'conf/domain/model/DataSet';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataset = {
  addRecordsById: async (datasetId, tableSchemaId, datasetTableRecords) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/table/${tableSchemaId}/record`
          : getUrl(DatasetConfig.addNewRecord, {
              datasetId: datasetId,
              tableSchemaId: tableSchemaId
            }),
        data: datasetTableRecords,
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error adding record to dataset data: ${error}`);
      return false;
    }
  },
  deleteDataById: async datasetId => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/deleteImportData`
          : getUrl(DatasetConfig.deleteImportData, {
              datasetId: datasetId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset data: ${error}`);
      return false;
    }
  },
  deleteRecordById: async (datasetId, recordId) => {
    try {
      const tokens = userStorage.get();
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/record/${recordId}`
          : getUrl(DatasetConfig.deleteRecord, {
              datasetId,
              recordId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table record: ${error}`);
      return false;
    }
  },
  deleteSchemaById: async (datasetId, datasetSchemaId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteDataSchema, {
        datasetId,
        datasetSchemaId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  deleteTableDataById: async (datasetId, tableId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/deleteImportTable/${tableId}`
          : getUrl(DatasetConfig.deleteImportTable, {
              datasetId: datasetId,
              tableId: tableId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table data: ${error}`);
      return false;
    }
  },
  errorPositionByObjectId: async (objectId, datasetId, entityType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_getTableFromAnyObjectId.json'
        : getUrl(DatasetConfig.validationViewer, {
            objectId: objectId,
            datasetId: datasetId,
            entityType: entityType
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  errorsById: async (datasetId, pageNum, pageSize, sortField, asc) => {
    const tokens = userStorage.get();
    if (asc === -1) {
      asc = 0;
    }
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-errors.json'
        : getUrl(DatasetConfig.listValidations, {
            datasetId: datasetId,
            pageNum: pageNum,
            pageSize: pageSize,
            sortField: sortField,
            asc: asc
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  },
  exportDataById: async (datasetId, fileType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, {
        datasetId: datasetId,
        fileType: fileType
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  exportTableDataById: async (datasetId, tableSchemaId, fileType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetTableData, {
        datasetId: datasetId,
        tableSchemaId: tableSchemaId,
        fileType: fileType
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`,
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  schemaById: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/datosDataSchema2.json'
        : getUrl(DatasetConfig.dataSchema, {
            dataflowId: dataflowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  statisticsById: async datasetId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/error-statistics.json'
        : getUrl(DatasetConfig.loadStatistics, {
            datasetId: datasetId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  tableDataById: async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError) => {
    levelError = levelError.join(',');
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(DatasetConfig.dataViewer, {
            datasetId: datasetId,
            tableSchemaId: tableSchemaId,
            pageNum: pageNum,
            pageSize: pageSize,
            fields: fields,
            levelError: levelError
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  webFormDataById: async (datasetId, tableSchemaId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(DatasetConfig.webFormDataViewer, {
            datasetId: datasetId,
            tableSchemaId: tableSchemaId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  updateFieldById: async (datasetId, datasetTableRecords) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateField`
          : getUrl(DatasetConfig.updateTableDataField, {
              datasetId: datasetId
            }),
        data: datasetTableRecords,
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset field: ${error}`);
      return false;
    }
  },
  updateRecordsById: async (datasetId, datasetTableRecords) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateRecord`
          : getUrl(DatasetConfig.updateTableDataRecord, {
              datasetId: datasetId
            }),
        data: datasetTableRecords,
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset record data: ${error}`);
      return false;
    }
  },
  updateSchemaNameById: async (datasetId, datasetSchemaName) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDataSchemaName, {
        datasetId,
        datasetSchemaName
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  validateById: async datasetId => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/jsons/list-of-errors.json`
          : getUrl(DatasetConfig.validateDataset, {
              datasetId: datasetId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error calling dataset data validation: ${error}`);
      return false;
    }
  }
};
