import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataSet = {
  addRecordById: async (dataSetId, tableSchemaId, dataSetTableRecords) => {
    console.log(JSON.stringify(dataSetTableRecords[0]));
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/table/${tableSchemaId}/record`
          : getUrl(config.addNewRecord.url, {
              dataSetId: dataSetId,
              tableSchemaId: tableSchemaId
            }),
        data: {
          records: JSON.stringify(dataSetTableRecords[0])
        },
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataSet data: ${error}`);
      return false;
    }
  },
  deleteDataById: async dataSetId => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/deleteImportData`
          : getUrl(config.deleteImportData.url, {
              dataSetId: dataSetId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataSet data: ${error}`);
      return false;
    }
  },
  deleteRecordByIds: async (dataSetId, rowIds) => {
    try {
      const tokens = userStorage.get();
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/record/`
          : getUrl(config.deleteRecord.url, {
              dataSetId: dataSetId
            }),
        data: {
          rowIds: rowIds
        },
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataSet table data: ${error}`);
      return false;
    }
  },
  deleteTableDataById: async (dataSetId, tableId) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/deleteImportTable/${tableId}`
          : getUrl(config.deleteImportTable.url, {
              dataSetId: dataSetId,
              tableId: tableId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataSet table data: ${error}`);
      return false;
    }
  },
  errorPositionByObjectId: async (objectId, dataSetId, entityType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_getTableFromAnyObjectId.json'
        : getUrl(config.validationViewerAPI.url, {
            objectId: objectId,
            dataSetId: dataSetId,
            entityType: entityType
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  errorsById: async (dataSetId, pageNum, pageSize, sortField, asc) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-errors.json'
        : getUrl(config.listValidationsAPI.url, {
            dataSetId: dataSetId,
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
  exportDataById: async (dataSetId, fileType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: getUrl(config.exportDataSetData.url, {
        dataSetId: dataSetId,
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
  exportTableDataById: async (dataSetId, tableSchemaId, fileType) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.download({
      url: getUrl(config.exportDataSetTableData.url, {
        dataSetId: dataSetId,
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
  schemaById: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/datosDataSchema2.json'
        : getUrl(config.dataSchemaAPI.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  statisticsById: async dataSetId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/error-statistics.json'
        : getUrl(config.loadStatisticsAPI.url, {
            dataSetId: dataSetId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  tableDataById: async (dataSetId, tableSchemaId, pageNum, pageSize, fields) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(config.dataviewerAPI.url, {
            dataSetId: dataSetId,
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
  updateFieldById: async (dataSetId, dataSetTableField) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/updateField`
          : getUrl(config.updateTableDataField.url, {
              dataSetId: dataSetId
            }),
        data: {
          field: JSON.stringify(dataSetTableField)
        },
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataSet field: ${error}`);
      return false;
    }
  },
  validateById: async dataSetId => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/jsons/list-of-errors.json`
          : getUrl(config.validateDataSetAPI.url, {
              dataSetId: dataSetId
            }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error calling dataSet data validation: ${error}`);
      return false;
    }
  }
};
