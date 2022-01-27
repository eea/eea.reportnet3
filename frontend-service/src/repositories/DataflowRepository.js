import { DataflowConfig } from './config/DataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DataflowRepository = {
  countByType: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.countByType) }),

  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) => {
    return await HTTPRequester.get({
      url: getUrl(DataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    });
  },

  getCloneableDataflows: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.getCloneableDataflows) }),

  getSchemas: async dataflowId => await HTTPRequester.get({ url: getUrl(DataflowConfig.getSchemas, { dataflowId }) }),

  cloneSchemas: async (sourceDataflowId, targetDataflowId) =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.cloneSchemas, { sourceDataflowId, targetDataflowId }) }),

  create: async (name, description, obligationId, type) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: { name, description, obligation: { obligationId }, releasable: true, type }
    }),

  downloadAllSchemasInfo: async (dataflowId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DataflowConfig.downloadAllSchemasInfo, { dataflowId, fileName })
    }),

  downloadPublicAllSchemasInfoFile: async dataflowId =>
    await HTTPRequester.download({ url: getUrl(DataflowConfig.downloadPublicAllSchemasInfoFile, { dataflowId }) }),

  downloadQCRulesFile: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DataflowConfig.downloadQCRulesFile, { datasetId, fileName })
    }),

  downloadUsersListFile: async (dataflowId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(DataflowConfig.downloadUsersListFile, { dataflowId, fileName })
    }),

  generateAllSchemasInfoFile: async dataflowId =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.generateAllSchemasInfoFile, { dataflowId }) }),

  generateUsersByCountryFile: async dataflowId =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.generateUsersByCountryFile, { dataflowId }) }),

  getDetails: async dataflowId => await HTTPRequester.get({ url: getUrl(DataflowConfig.getDetails, { dataflowId }) }),

  getDatasetsFinalFeedbackAndReleasedStatus: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getDatasetsFinalFeedbackAndReleasedStatus, { dataflowId }) }),

  getDatasetsValidationStatistics: async (dataflowId, datasetSchemaId) =>
    await HTTPRequester.get({
      url: getUrl(DataflowConfig.getDatasetsValidationStatistics, { dataflowId, datasetSchemaId })
    }),

  delete: async dataflowId => await HTTPRequester.delete({ url: getUrl(DataflowConfig.delete, { dataflowId }) }),

  exportSchemas: async dataflowId =>
    await HTTPRequester.download({ url: getUrl(DataflowConfig.exportSchemas, { dataflowId }) }),

  createApiKey: async (dataflowId, dataProviderId, isCustodian) =>
    await HTTPRequester.post({
      url: isCustodian
        ? getUrl(DataflowConfig.createApiKeyCustodian, { dataflowId })
        : getUrl(DataflowConfig.createApiKey, { dataflowId, dataProviderId })
    }),

  getAllDataflowsUserList: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.getAllDataflowsUserList) }),

  getApiKey: async (dataflowId, dataProviderId, isCustodian) =>
    await HTTPRequester.get({
      url: isCustodian
        ? getUrl(DataflowConfig.getApiKeyCustodian, { dataflowId })
        : getUrl(DataflowConfig.getApiKey, { dataflowId, dataProviderId })
    }),

  getRepresentativesUsersList: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getRepresentativesUsersList, { dataflowId }) }),

  getPublicDataflowData: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getPublicDataflowData, { dataflowId }) }),

  getPublicDataflowsByCountryCode: async (countryCode, sortOrder, pageNum, numberRows, sortField) =>
    await HTTPRequester.get({
      url: getUrl(DataflowConfig.getPublicDataflowsByCountryCode, {
        country: countryCode,
        pageNum,
        pageSize: numberRows,
        sortField,
        asc: sortOrder
      })
    }),

  getUserList: async (dataflowId, representativeId) =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getUserList, { dataflowId, representativeId }) }),

  createEmptyDatasetSchema: async (dataflowId, datasetSchemaName) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createEmptyDatasetSchema, { dataflowId, datasetSchemaName })
    }),

  getPublicData: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.getPublicData) }),

  get: async dataflowId => await HTTPRequester.get({ url: getUrl(DataflowConfig.get, { dataflowId }) }),

  getSchemasValidation: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getSchemasValidation, { dataflowId }) }),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        id: dataflowId,
        name,
        description,
        obligation: { obligationId },
        releasable: isReleasable,
        showPublicInfo
      }
    }),

  getDatasetsInfo: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getDatasetsInfo, { dataflowId }) }),

  validateAllDataflowsUsers: async () =>
    await HTTPRequester.update({ url: getUrl(DataflowConfig.validateAllDataflowsUsers) })
};
