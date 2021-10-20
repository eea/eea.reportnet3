import { RepresentativeConfig } from './config/RepresentativeConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const RepresentativeRepository = {
  createDataProvider: async (dataflowId, dataProviderGroupId, dataProviderId) =>
    await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.createDataProvider, { dataflowId }),
      data: { dataProviderId, dataProviderGroupId }
    }),

  createLeadReporter: async (leadReporterAccount, representativeId, dataflowId) =>
    await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.createLeadReporter, { representativeId, dataflowId }),
      data: { email: leadReporterAccount }
    }),

  getDataProviders: async dataProviderGroupId =>
    await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getDataProviders, { dataProviderGroupId }) }),

  getRepresentatives: async dataflowId =>
    await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getRepresentatives, { dataflowId: dataflowId }) }),

  deleteRepresentative: async (representativeId, dataflowId) =>
    await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteRepresentative, { representativeId, dataflowId })
    }),

  deleteLeadReporter: async (leadReporterId, dataflowId) =>
    await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteLeadReporter, { leadReporterId, dataflowId })
    }),

  exportFile: async dataflowId =>
    await HTTPRequester.download({ url: getUrl(RepresentativeConfig.exportFile, { dataflowId }) }),

  exportTemplateFile: async dataProviderGroupId =>
    await HTTPRequester.download({ url: getUrl(RepresentativeConfig.exportTemplateFile, { dataProviderGroupId }) }),

  getFmeUsers: async () => await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getFmeUsers, {}) }),

  getGroupCompanies: async () => await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getGroupCompanies, {}) }),

  getGroupCountries: async () => await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getGroupCountries, {}) }),

  getGroupOrganizations: async () =>
    await HTTPRequester.get({ url: getUrl(RepresentativeConfig.getGroupOrganizations, {}) }),

  updateDataProviderId: async (representativeId, dataProviderId) =>
    await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateDataProviderId, {}),
      data: {
        id: representativeId,
        dataProviderId: dataProviderId
      }
    }),

  updateLeadReporter: async (leadReporterAccount, leadReporterId, representativeId, dataflowId) =>
    await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateLeadReporter, { dataflowId }),
      data: { email: leadReporterAccount, id: leadReporterId, representativeId }
    }),

  updateRestrictFromPublic: async (
    dataflowId,
    dataProviderId,
    restrictFromPublic // TODO ADD REAL ENDPOINT
  ) =>
    await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateRestrictFromPublic, { dataflowId, dataProviderId, restrictFromPublic })
    })
};
