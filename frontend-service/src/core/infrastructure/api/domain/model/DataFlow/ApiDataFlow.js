import { DataflowConfig } from 'conf/domain/model/DataFlow';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiDataflow = {
  accept: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.acceptDataflow, { dataflowId, type: 'ACCEPTED' }),
      data: { id: dataflowId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  all: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/DataFlows2.json'
        : getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  accepted: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/DataFlows2.json'
        : getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  completed: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/DataFlows2.json'
        : getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  datasetsValidationStatistics: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.globalStatistics, { dataflowId: dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  datasetsReleasedStatus: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.datasetsReleasedStatus, { dataflowId: dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  dataflowDetails: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.dataflowDetails, { dataflowId: dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
    // return metadataJson;
  },
  pending: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/DataFlows2.json'
        : getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  newEmptyDatasetSchema: async (dataflowId, datasetSchemaName) => {
    const tokens = userStorage.get();
    try {
      const response = await HTTPRequester.post({
        url: getUrl(DataflowConfig.newEmptyDatasetSchema, {
          dataflowId: dataflowId,
          datasetSchemaName: datasetSchemaName
        }),
        queryString: {},
        headers: {
          Authorization: `Bearer ${tokens.accessToken}`
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error adding new dataset schema: ${error}`);
      return false;
    }
  },
  reject: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.rejectDataflow, { dataflowId, type: 'REJECTED' }),
      data: { id: dataflowId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  reporting: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_DataflowById.json'
        : getUrl(DataflowConfig.loadDatasetsByDataflowId, {
            dataflowId: dataflowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
