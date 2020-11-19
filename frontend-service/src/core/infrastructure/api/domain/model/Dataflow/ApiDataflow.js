import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDataflow = {
  accept: async dataflowId => {
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.acceptDataflow, { dataflowId, type: 'ACCEPTED' }),
      data: { id: dataflowId }
    });
    return response.status;
  },
  all: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.getDataflows)
    });

    return response.data;
  },
  allSchemas: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.allSchemas, { dataflowId })
    });
    return response.data;
  },
  accepted: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.getDataflows)
    });
    return response.data;
  },
  cloneDatasetSchemas: async (sourceDataflowId, targetDataflowId) => {
    const response = await HTTPRequester.post({
      url: getUrl(DataflowConfig.cloneDatasetSchemas, {
        sourceDataflowId,
        targetDataflowId
      })
    });
    return response;
  },
  completed: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.getDataflows)
    });
    return response.data;
  },
  create: async (name, description, obligationId) => {
    const response = await HTTPRequester.post({
      url: getUrl(DataflowConfig.createDataflow),
      data: { name, description, obligation: { obligationId } }
    });
    return response;
  },
  datasetsFinalFeedback: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.datasetsFinalFeedback, { dataflowId: dataflowId })
    });
    return response.data;
  },
  datasetsValidationStatistics: async datasetSchemaId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.globalStatistics, { datasetSchemaId })
    });
    return response.data;
  },
  datasetsReleasedStatus: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.datasetsReleasedStatus, { dataflowId: dataflowId })
    });
    return response.data;
  },
  dataflowDetails: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.dataflowDetails, { dataflowId: dataflowId })
    });
    return response.data;
  },
  deleteById: async dataflowId => {
    const response = await HTTPRequester.delete({
      url: getUrl(DataflowConfig.deleteDataflow, { dataflowId })
    });
    return response;
  },
  getApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    let url = isCustodian
      ? getUrl(DataflowConfig.getApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId });
    const response = await HTTPRequester.get({
      url: url
    });
    return response.data;
  },
  generateApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    let url = isCustodian
      ? getUrl(DataflowConfig.generateApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.generateApiKey, { dataflowId, dataProviderId });
    const response = await HTTPRequester.post({
      url: url
    });
    return response.data;
  },
  pending: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.getDataflows)
    });

    return response.data;
  },
  newEmptyDatasetSchema: async (dataflowId, datasetSchemaName) => {
    const response = await HTTPRequester.post({
      url: getUrl(DataflowConfig.newEmptyDatasetSchema, {
        dataflowId,
        datasetSchemaName
      })
    });
    return response.status;
  },
  reject: async dataflowId => {
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.rejectDataflow, { dataflowId, type: 'REJECTED' }),
      data: { id: dataflowId }
    });
    return response.status;
  },
  reporting: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.loadDatasetsByDataflowId, {
        dataflowId: dataflowId
      })
    });
    return response.data;
  },
  schemasValidation: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(DataflowConfig.dataSchemasValidation, {
        dataflowId
      })
    });
    return response.data;
  },
  update: async (dataflowId, name, description, obligationId) => {
    const response = await HTTPRequester.update({
      url: getUrl(DataflowConfig.createDataflow),
      data: { id: dataflowId, name, description, obligation: { obligationId } }
    });
    return response;
  }
};
