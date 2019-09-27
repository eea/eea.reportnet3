import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataset = {
  addRecordsById: async (datasetId, tableSchemaId, dataSetTableRecords) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/table/${tableSchemaId}/record`
          : getUrl(config.addNewRecord.url, {
              datasetId: datasetId,
              tableSchemaId: tableSchemaId
            }),
        data: dataSetTableRecords,
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
          : getUrl(config.deleteImportData.url, {
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
          : getUrl(config.deleteRecord.url, {
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
  deleteTableDataById: async (datasetId, tableId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/deleteImportTable/${tableId}`
          : getUrl(config.deleteImportTable.url, {
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
        : getUrl(config.validationViewerAPI.url, {
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
        : getUrl(config.listValidationsAPI.url, {
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
      url: getUrl(config.exportDataSetData.url, {
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
      url: getUrl(config.exportDataSetTableData.url, {
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
        : getUrl(config.dataSchemaAPI.url, {
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
        : getUrl(config.loadStatisticsAPI.url, {
            datasetId: datasetId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  tableDataById: async (datasetId, tableSchemaId, pageNum, pageSize, fields) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(config.dataViewerAPI.url, {
            datasetId: datasetId,
            tableSchemaId: tableSchemaId,
            pageNum: pageNum,
            pageSize: pageSize,
            fields: fields
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
        : getUrl(config.webFormDataViewerAPI.url, {
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
  updateFieldById: async (datasetId, dataSetTableField) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateField`
          : getUrl(config.updateTableDataField.url, {
              datasetId: datasetId
            }),
        data: dataSetTableField,
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
  updateRecordsById: async (datasetId, dataSetTableRecords) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateRecord`
          : getUrl(config.updateTableDataRecord.url, {
              datasetId: datasetId
            }),
        data: dataSetTableRecords,
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
  validateById: async datasetId => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/jsons/list-of-errors.json`
          : getUrl(config.validateDataSetAPI.url, {
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
