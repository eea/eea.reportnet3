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
  addLeadReporter: async (leadReporterAccount, representativeId) =>
    await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.addLeadReporter, { representativeId }),
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
  deleteById: async representativeId =>
    await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteById, {
        representativeId
      })
    }),
  deleteLeadReporter: async leadReporterId =>
    await HTTPRequester.delete({ url: getUrl(RepresentativeConfig.deleteLeadReporter, { leadReporterId }) }),
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
  updateLeadReporter: async (leadReporterAccount, leadReporterId, representativeId) =>
    await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateLeadReporter, {}),
      data: { email: leadReporterAccount, id: leadReporterId, representativeId }
    })
};

export { apiRepresentative };
