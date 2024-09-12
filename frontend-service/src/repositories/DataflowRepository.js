import { DataflowConfig } from './config/DataflowConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const DataflowRepository = {
  countByType: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.countByType) }),

  getAll: async ({ filterBy, isAsc, numberRows, pageNum, sortBy }) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.getAll, { isAsc, numberRows, pageNum, sortBy }),
      data: { ...filterBy }
    });
  },

  getCloneableDataflows: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.getCloneableDataflows) }),

  getSchemas: async dataflowId => {
    const response = await HTTPRequester.get({ url: getUrl(DataflowConfig.getSchemas, { dataflowId }) });
    return response;
  },

  cloneSchemas: async (sourceDataflowId, targetDataflowId) =>
    await HTTPRequester.post({ url: getUrl(DataflowConfig.cloneSchemas, { sourceDataflowId, targetDataflowId }) }),

  create: async (name, description, obligationId, type, bigData) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createUpdate),
      data: { name, description, obligation: { obligationId }, releasable: true, type, bigData }
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

  generateUsersByCountryFile: async (dataflowId, dataProviderId) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.generateUsersByCountryFile, { dataflowId, dataProviderId })
    }),

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

  getPublicDataflowsByCountryCode: async ({ countryCode, sortOrder, pageNum, numberRows, sortField, filterBy }) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.getPublicDataflowsByCountryCode, {
        country: countryCode,
        pageNum,
        pageSize: numberRows,
        sortField,
        asc: sortOrder
      }),
      data: { ...filterBy }
    }),

  getUserList: async (dataflowId, representativeId) =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getUserList, { dataflowId, representativeId }) }),

  createEmptyDatasetSchema: async (dataflowId, datasetSchemaName) =>
    await HTTPRequester.post({
      url: getUrl(DataflowConfig.createEmptyDatasetSchema, { dataflowId, datasetSchemaName })
    }),

  getPublicData: async ({ filterBy, isAsc = true, numberRows, pageNum, sortByHeader = 'name' }) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.getPublicData, { isAsc, numberRows, pageNum, sortBy: sortByHeader }),
      data: { ...filterBy }
    });
  },

  get: async dataflowId => await HTTPRequester.get({ url: getUrl(DataflowConfig.get, { dataflowId }) }),

  getIcebergTables: async ({ dataflowId, providerId, datasetId }) =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getIcebergTables, { dataflowId, providerId, datasetId }) }),

  getSchemasValidation: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getSchemasValidation, { dataflowId }) }),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo, bigData, deadlineDate) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.createUpdate),
      data: {
        id: dataflowId,
        name,
        description,
        obligation: { obligationId },
        releasable: isReleasable,
        showPublicInfo,
        bigData,
        deadlineDate
      }
    }),

  updateAutomaticDelete: async (dataflowId, isAutomaticReportingDeletion) =>
    await HTTPRequester.update({
      url: getUrl(DataflowConfig.updateAutomaticDelete, { dataflowId, isAutomaticReportingDeletion })
    }),

  getDatasetsInfo: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(DataflowConfig.getDatasetsInfo, { dataflowId }) }),

  validateAllDataflowsUsers: async () =>
    await HTTPRequester.update({ url: getUrl(DataflowConfig.validateAllDataflowsUsers) })
};
