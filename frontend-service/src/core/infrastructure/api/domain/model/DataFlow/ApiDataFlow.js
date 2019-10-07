import { DataflowConfig } from 'conf/domain/model/DataFlow';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';
import dataCustodianDashboards from './response_GlobalStatsDataflow.json';

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
  datasetValidationStatistics: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.globalStatistics, { dataflowId: dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
    // const hardcodedDashboardTest = dataCustodianDashboards;
    // return hardcodedDashboardTest;
  },
  datasetReleasedStatus: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.datasetReleasedStatus, { dataflowId: dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
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
