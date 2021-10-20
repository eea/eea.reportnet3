import isEmpty from 'lodash/isEmpty';
import sortBy from 'lodash/sortBy';

import { RepresentativeRepository } from 'repositories/RepresentativeRepository';

import { RepresentativeUtils } from 'services/_utils/RepresentativeUtils';

export const RepresentativeService = {
  createDataProvider: async (dataflowId, providerAccount, dataProviderId) =>
    await RepresentativeRepository.createDataProvider(dataflowId, providerAccount, dataProviderId),

  createLeadReporter: async (leadReporterAccount, representativeId, dataflowId) =>
    await RepresentativeRepository.createLeadReporter(leadReporterAccount, representativeId, dataflowId),

  getDataProviders: async dataProviderGroup => {
    let response = [];
    const dataProvidersDTO = await RepresentativeRepository.getDataProviders(dataProviderGroup.dataProviderGroupId);
    response = dataProvidersDTO.data.map(dataProvider => {
      return { dataProviderId: dataProvider.id, label: dataProvider.label };
    });
    return response;
  },

  getRepresentatives: async dataflowId => {
    const representativesDTO = await RepresentativeRepository.getRepresentatives(dataflowId);

    const representativesList = RepresentativeUtils.parseRepresentativeListDTO(representativesDTO.data);

    const dataToConsume = {
      group: !isEmpty(representativesDTO.data)
        ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId }
        : { dataProviderGroupId: null },
      representatives: sortBy(representativesList, ['representativeId'])
    };
    return dataToConsume;
  },

  deleteRepresentative: async (representativeId, dataflowId) =>
    await RepresentativeRepository.deleteRepresentative(representativeId, dataflowId),

  deleteLeadReporter: async (leadReporterId, dataflowId) =>
    await RepresentativeRepository.deleteLeadReporter(leadReporterId, dataflowId),

  exportFile: async dataflowId => await RepresentativeRepository.exportFile(dataflowId),

  exportTemplateFile: async dataProviderGroupId =>
    await RepresentativeRepository.exportTemplateFile(dataProviderGroupId),

  getFmeUsers: async () => await RepresentativeRepository.getFmeUsers(),

  getGroupCompanies: async () => await RepresentativeRepository.getGroupCompanies(),

  getGroupCountries: async () => await RepresentativeRepository.getGroupCountries(),

  getGroupOrganizations: async () => await RepresentativeRepository.getGroupOrganizations(),

  updateDataProviderId: async (representativeId, dataProviderId) =>
    await RepresentativeRepository.updateDataProviderId(representativeId, dataProviderId),

  updateLeadReporter: async (leadReporterAccount, leadReporterId, representativeId, dataflowId) =>
    await RepresentativeRepository.updateLeadReporter(
      leadReporterAccount,
      leadReporterId,
      representativeId,
      dataflowId
    ),

  updateRestrictFromPublic: async (
    dataflowId,
    dataProviderId,
    restrictFromPublic // TODO ADD REAL ENDPOINT
  ) => await RepresentativeRepository.updateRestrictFromPublic(dataflowId, dataProviderId, restrictFromPublic)
};
