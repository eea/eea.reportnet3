import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.getDataflows) }),

  allSchemas: async dataflowId => await HTTPRequester.get({ url: getUrl(DataflowConfig.allSchemas, { dataflowId }) }),

  cloneDatasetSchemas: async (sourceDataflowId, targetDataflowId) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.cloneDatasetSchemas, { sourceDataflowId, targetDataflowId })
    });
  },

  create: async (name, description, obligationId) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.createDataflow),
      data: { name, description, obligation: { obligationId }, releasable: true }
    });
  },

  dataflowDetails: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.dataflowDetails, { dataflowId }) });
    return response.data;
  },

  datasetsFinalFeedback: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.datasetsFinalFeedback, { dataflowId }) });
    return response.data;
  },

  datasetsReleasedStatus: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.datasetsReleasedStatus, { dataflowId }) });
    return response.data;
  },

  datasetsValidationStatistics: async datasetSchemaId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.globalStatistics, { datasetSchemaId }) });
    return response.data;
  },

  deleteById: async dataflowId => {
    return await HTTPRequester.delete({ url: getUrl(DataflowConfig.deleteDataflow, { dataflowId }) });
  },

  downloadById: async dataflowId => {
    const response = await HTTPRequester.download({ url: getUrl(DataflowConfig.exportSchema, { dataflowId }) });
    return response.data;
  },

  generateApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    const url = isCustodian
      ? getUrl(DataflowConfig.generateApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.generateApiKey, { dataflowId, dataProviderId });

    const response = await HTTPRequester.post({ url });
    return response.data;
  },

  getApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    const url = isCustodian
      ? getUrl(DataflowConfig.getApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId });

    const response = await HTTPRequester.get({ url });
    return response.data;
  },

  getPublicDataflowData: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.getPublicDataflowData, { dataflowId }) });
    return response.data;
  },

  newEmptyDatasetSchema: async (dataflowId, datasetSchemaName) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.newEmptyDatasetSchema, { dataflowId, datasetSchemaName })
    });
  },

  publicData: async () => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.publicData) });
    return response.data;
  },

  reporting: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.loadDatasetsByDataflowId, { dataflowId }) });
    return response.data;
  },

  schemasValidation: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.dataSchemasValidation, { dataflowId }) });
    return response.data;
  },

  update: async (dataflowId, name, description, obligationId, isReleasable) => {
    return await HTTPRequester.update({
      url: getUrl(DataflowConfig.createDataflow),
      data: { id: dataflowId, name, description, obligation: { obligationId }, releasable: isReleasable }
    });
  }
};
