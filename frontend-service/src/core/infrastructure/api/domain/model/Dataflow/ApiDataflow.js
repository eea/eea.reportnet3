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
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.dataflowDetails, { dataflowId }) });
  },

  datasetsFinalFeedback: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.datasetsFinalFeedback, { dataflowId }) });
  },

  datasetsReleasedStatus: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.datasetsReleasedStatus, { dataflowId }) });
  },

  datasetsValidationStatistics: async datasetSchemaId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.globalStatistics, { datasetSchemaId }) });
    return response.data;
  },

  deleteById: async dataflowId => {
    return await HTTPRequester.delete({ url: getUrl(DataflowConfig.deleteDataflow, { dataflowId }) });
  },

  downloadById: async dataflowId => {
    return await HTTPRequester.download({ url: getUrl(DataflowConfig.exportSchema, { dataflowId }) });
  },

  generateApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    const url = isCustodian
      ? getUrl(DataflowConfig.generateApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.generateApiKey, { dataflowId, dataProviderId });

    return await HTTPRequester.post({ url });
  },

  getApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    const url = isCustodian
      ? getUrl(DataflowConfig.getApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId });

    return await HTTPRequester.get({ url });
  },

  getPublicDataflowData: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.getPublicDataflowData, { dataflowId }) });
  },

  newEmptyDatasetSchema: async (dataflowId, datasetSchemaName) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.newEmptyDatasetSchema, { dataflowId, datasetSchemaName })
    });
  },

  publicData: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.publicData) }),

  reporting: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.loadDatasetsByDataflowId, { dataflowId }) });
  },

  schemasValidation: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.dataSchemasValidation, { dataflowId }) });
  },

  update: async (dataflowId, name, description, obligationId, isReleasable) => {
    return await HTTPRequester.update({
      url: getUrl(DataflowConfig.createDataflow),
      data: { id: dataflowId, name, description, obligation: { obligationId }, releasable: isReleasable }
    });
  }
};
