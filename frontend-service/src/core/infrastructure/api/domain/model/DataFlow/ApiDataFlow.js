import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';
import dataCustodianDashboards from './dashboardData.json';

export const apiDataFlow = {
  accept: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(config.acceptDataFlow.url, { dataFlowId, type: 'ACCEPTED' }),
      data: { id: dataFlowId },
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
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
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
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
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
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  dashboards: async dataFlowId => {
    // const tokens = userStorage.get();
    // const response = await HTTPRequester.get({
    //   url: '/jsons/dataCustodianDashboards.json',
    //   queryString: {},
    //   headers: {
    //     Authorization: `Bearer ${tokens.accessToken}`
    //   }
    // });
    // return response.data;
    const hardcodedDashboardTest = dataCustodianDashboards;
    return hardcodedDashboardTest;
  },
  datasetReleasedStatus: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(config.datasetReleasedStatus.url, { dataFlowId: dataFlowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    // return response.data;

    const responseData = [
      {
        id: 1,
        dataSetName: 'Belgium',
        creationDate: 1565088304601,
        isReleased: false
      },
      {
        id: 2,
        dataSetName: 'Italy',
        creationDate: 1565088304000,
        isReleased: false
      },
      {
        id: 6,
        dataSetName: 'Spain',
        creationDate: 1567520549026,
        isReleased: true
      }
    ];
    //HARDCODED RESPONSE responseData SHOULD BE CHANGED TO response.data
    return responseData;
  },
  pending: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? '/jsons/DataFlows2.json' : getUrl(config.loadDataFlowTaskPendingAcceptedAPI.url),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  reject: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(config.rejectDataFlow.url, { dataFlowId, type: 'REJECTED' }),
      data: { id: dataFlowId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
  },
  reporting: async dataFlowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_DataflowById.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: dataFlowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
