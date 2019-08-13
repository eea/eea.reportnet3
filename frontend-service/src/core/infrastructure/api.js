import { config } from 'assets/conf';
import { getUrl } from 'core/infrastructure/getUrl';
import { HTTPRequester } from './HTTPRequester';

export const api = {
  /* #region Dataset */
  dataSetErrorsById: async (dataSetId, pageNum, pageSize, sortField, asc) => {
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
      queryString: {}
    });

    return response.data;
  },
  dataSetStatisticsById: async dataSetId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/error-statistics.json'
        : getUrl(config.loadStatisticsAPI.url, {
            dataSetId: dataSetId
          }),
      queryString: {}
    });
    return response.data;
  },
  dataSetSchemaById: async dataFlowId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/datosDataSchema2.json'
        : getUrl(config.dataSchemaAPI.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {}
    });
    return response.data;
  },
  deleteDataSetDataById: async dataSetId => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/deleteImportData`
          : getUrl(config.deleteImportData.url, {
              dataSetId: dataSetId
            }),
        queryString: {}
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error deleting dataSet data: ${error}`);
      return false;
    }
  },
  deleteDataSetTableDataById: async (dataSetId, tableId) => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${dataSetId}/deleteImportTable/${tableId}`
          : getUrl(config.deleteImportTable.url, {
              dataSetId: dataSetId,
              tableId: tableId
            }),
        queryString: {}
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error deleting dataSet table data: ${error}`);
      return false;
    }
  },
  errorPositionByObjectId: async (objectId, dataSetId, entityType) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_getTableFromAnyObjectId.json'
        : getUrl(config.validationViewerAPI.url, {
            objectId: objectId,
            dataSetId: dataSetId,
            entityType: entityType
          }),
      queryString: {}
    });
    return response.data;
  },
  validateDataSetById: async dataSetId => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/jsons/list-of-errors.json`
          : getUrl(config.validateDataSetAPI.url, {
              dataSetId: dataSetId
            }),
        queryString: {}
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error calling dataSet data validation: ${error}`);
      return false;
    }
  },
  /* #endregion */
  /* #region Documents */
  documents: async dataFlowId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {}
    });
    return response.data.documents;
  },
  downloadDocumentById: async documentId => {
    const response = await HTTPRequester.download({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.downloadDocumentByIdAPI.url, {
            documentId: documentId
          }),
      queryString: {}
    });
    return response.data;
  },
  uploadDocument: async (dataFlowId, description, language, file) => {
    const formData = new FormData();
    formData.append('file', file, file.name);
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(config.uploadDocumentAPI.url, {
        dataFlowId: dataFlowId,
        description: description,
        language: language
      }),
      queryString: {},
      data: formData
    });
    return response.status;
  },
  /* #endregion */
  /* #region Snapshots */
  createSnapshotById: async (dataSetId, description) => {
    try {
      const response = await HTTPRequester.post({
        url: getUrl(config.createSnapshot.url, {
          dataSetId,
          description: description
        }),
        data: {
          description: description
        }
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error creating the snapshot: ${error}`);
      return false;
    }
  },
  deleteSnapshotById: async (dataSetId, snapshotId) => {
    try {
      const response = await HTTPRequester.delete({
        url: getUrl(config.deleteSnapshotByID.url, {
          dataSetId,
          snapshotId: snapshotId
        })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error deleting snapshot data: ${error}`);
      return false;
    }
  },
  restoreSnapshotById: async (dataFlowId, dataSetId, snapshotId) => {
    try {
      const response = await HTTPRequester.update({
        url: getUrl(config.restoreSnaphost.url, {
          dataFlowId,
          dataSetId,
          snapshotId: snapshotId
        })
      });
      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.log(`Error restoring the snapshot: ${error}`);
      return false;
    }
  },
  snapshots: async dataSetId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/snapshots.json'
        : getUrl(config.loadSnapshotsListAPI.url, {
            dataSetId: dataSetId
          }),
      queryString: {}
    });
    return response.data;
  },
  /* #endregion */
  /* #region WebLinks */
  webLinks: async dataFlowId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {}
    });
    return response.data.weblinks;
  }
  /* #endregion */
};
