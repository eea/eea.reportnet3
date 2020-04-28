import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
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
      url: getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  },
  allSchemas: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.allSchemas, { dataflowId }),
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
      url: getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
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
      url: getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  create: async (name, description, obligationId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON ? '/dataflow' : getUrl(DataflowConfig.createDataflow),
      data: { name, description, obligation: { obligationId } },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  datasetsValidationStatistics: async datasetSchemaId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.globalStatistics, { datasetSchemaId }),
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
  deleteById: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(DataflowConfig.deleteDataflow, { dataflowId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  getApiKey: async (dataflowId, dataProviderId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  },
  generateApiKey: async (dataflowId, dataProviderId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.generateApiKey, { dataflowId, dataProviderId }),
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
      url: getUrl(DataflowConfig.loadDataflowTaskPendingAccepted),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  },
  newEmptyDatasetSchema: async (dataflowId, datasetSchemaName) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(DataflowConfig.newEmptyDatasetSchema, {
        dataflowId,
        datasetSchemaName
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.status;
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
      url: getUrl(DataflowConfig.loadDatasetsByDataflowId, {
        dataflowId: dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  schemasValidation: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.dataSchemasValidation, {
        dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  },
  update: async (dataflowId, name, description, obligationId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.createDataflow),
      data: { id: dataflowId, name, description, obligation: { obligationId } },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  }
};
