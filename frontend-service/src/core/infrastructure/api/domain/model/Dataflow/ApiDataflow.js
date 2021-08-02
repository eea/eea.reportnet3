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

  create: async (name, description, obligationId, type) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.createDataflow),
      data: { name, description, obligation: { obligationId }, releasable: true, type }
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

  datasetsValidationStatistics: async (dataflowId, datasetSchemaId) => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.globalStatistics, { dataflowId, datasetSchemaId }) });
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

  getAllDataflowsUserList: async () => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.getAllDataflowsUserList) });
  },

  getApiKey: async (dataflowId, dataProviderId, isCustodian) => {
    const url = isCustodian
      ? getUrl(DataflowConfig.getApiKeyCustodian, { dataflowId })
      : getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId });

    return await HTTPRequester.get({ url });
  },

  getRepresentativesUsersList: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.getRepresentativesUsersList, { dataflowId }) });
  },

  getPublicDataflowData: async dataflowId => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.getPublicDataflowData, { dataflowId }) });
  },

  getPublicDataflowsByCountryCode: async (countryCode, sortOrder, pageNum, numberRows, sortField) => {
    return await HTTPRequester.get({
      url: getUrl(DataflowConfig.getPublicDataflowsByCountryCode, {
        country: countryCode,
        pageNum,
        pageSize: numberRows,
        sortField,
        asc: sortOrder
      })
    });
  },

  getUserList: async (dataflowId, representativeId) => {
    return await HTTPRequester.get({ url: getUrl(DataflowConfig.getUserList, { dataflowId, representativeId }) });
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

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo) => {
    return await HTTPRequester.update({
      url: getUrl(DataflowConfig.createDataflow),
      data: {
        id: dataflowId,
        name,
        description,
        obligation: { obligationId },
        releasable: isReleasable,
        showPublicInfo
      }
    });
  }
};
