import { RepresentativeConfig } from 'conf/domain/model/Representative';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiRepresentative = {
  add: async (dataflowId, dataProviderGroupId, dataProviderId) => {
    const response = await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.add, {
        dataflowId
      }),
      data: {
        dataProviderId,
        dataProviderGroupId
      }
    });
    return response;
  },

  addLeadReporter: async (leadReporterAccount, representativeId) => {
    return await HTTPRequester.post({
      url: getUrl(RepresentativeConfig.addLeadReporter, { representativeId }),
      data: { email: leadReporterAccount }
    });
  },

  allDataProviders: async dataProviderGroupId => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allDataProviders, {
        dataProviderGroupId
      })
    });
    return response;
  },

  allRepresentatives: async dataflowId => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.allRepresentatives, {
        dataflowId: dataflowId
      })
    });
    return response;
  },

  deleteById: async representativeId => {
    const response = await HTTPRequester.delete({
      url: getUrl(RepresentativeConfig.deleteById, {
        representativeId
      })
    });

    return response;
  },

  deleteLeadReporter: async leadReporterId => {
    return await HTTPRequester.delete({ url: getUrl(RepresentativeConfig.deleteLeadReporter, { leadReporterId }) });
  },

  getProviderTypes: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.getProviderTypes, {})
    });

    return response.data;
  },
  
  downloadById: async dataflowId => {
    const response = await HTTPRequester.download({
      url: getUrl(RepresentativeConfig.exportRepresentatives, { dataflowId })

    });

    return response.data;
  },


  downloadTemplateById: async dataProviderGroupId => {
    const response = await HTTPRequester.download({
      url: getUrl(RepresentativeConfig.exportRepresentativesTemplate, { dataProviderGroupId })
    });

    return response.data;
  },

  getProviderTypes: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(RepresentativeConfig.getProviderTypes, {})
    });
    return response;
  },

  updateDataProviderId: async (representativeId, dataProviderId) => {
    const response = await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateDataProviderId, {}),
      data: {
        id: representativeId,
        dataProviderId: dataProviderId
      }
    });
    return response;
  },

  updateLeadReporter: async (leadReporterAccount, leadReporterId, representativeId) => {
    return await HTTPRequester.update({
      url: getUrl(RepresentativeConfig.updateLeadReporter, {}),
      data: { email: leadReporterAccount, id: leadReporterId, representativeId }
    });
  }
};

export { apiRepresentative };
