import { RepresentativeConfig } from 'conf/domain/model/Representative';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiRepresentative = {
  add: async (dataflowId, dataProviderGroupId, dataProviderId) =>
    await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.add, {
        dataflowId
      }),
      data: {
        dataProviderId,
        dataProviderGroupId
      }
    }),
  addLeadReporter: async (leadReporterAccount, representativeId, dataflowId) =>
    await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.addLeadReporter, { representativeId, dataflowId }),
      data: { email: leadReporterAccount }
    }),
  allDataProviders: async dataProviderGroupId =>
    await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allDataProviders, {
        dataProviderGroupId
      })
    }),
  allRepresentatives: async dataflowId =>
    await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allRepresentatives, {
        dataflowId: dataflowId
      })
    }),
  deleteById: async (representativeId, dataflowId) =>
    await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteById, {
        representativeId,
        dataflowId
      })
    }),
  deleteLeadReporter: async (leadReporterId, dataflowId) =>
    await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteLeadReporter, { leadReporterId, dataflowId })
    }),
  downloadById: async dataflowId =>
    await HTTPRequester.download({
      url: getUrl(RepresentativeConfig.exportRepresentatives, { dataflowId })
    }),
  downloadTemplateById: async dataProviderGroupId =>
    await HTTPRequester.download({
      url: getUrl(RepresentativeConfig.exportRepresentativesTemplate, { dataProviderGroupId })
    }),
  getProviderTypes: async () =>
    await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.getProviderTypes, {})
    }),
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
    })
};

export { apiRepresentative };
