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
    const dataProvidersDTO = await RepresentativeRepository.getDataProviders(dataProviderGroup.dataProviderGroupId);
    return dataProvidersDTO.data.map(dataProvider => ({
      dataProviderId: dataProvider.id,
      label: dataProvider.label,
      code: dataProvider.code
    }));
  },

  getRepresentatives: async dataflowId => {
    const representativesDTO = await RepresentativeRepository.getRepresentatives(dataflowId);
    const representativesList = RepresentativeUtils.parseRepresentativeListDTO(representativesDTO.data);

    return {
      group: !isEmpty(representativesDTO.data)
        ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId }
        : { dataProviderGroupId: null },
      representatives: sortBy(representativesList, ['representativeId'])
    };
  },

  deleteRepresentative: async (representativeId, dataflowId) =>
    await RepresentativeRepository.deleteRepresentative(representativeId, dataflowId),

  deleteLeadReporter: async (leadReporterId, dataflowId) =>
    await RepresentativeRepository.deleteLeadReporter(leadReporterId, dataflowId),

  deleteAllLeadReporters: async dataflowId => await RepresentativeRepository.deleteAllLeadReporters(dataflowId),

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

  updateRestrictFromPublic: async (dataflowId, dataProviderId, restrictFromPublic) =>
    await RepresentativeRepository.updateRestrictFromPublic(dataflowId, dataProviderId, restrictFromPublic),

  validateLeadReporters: async dataflowId => await RepresentativeRepository.validateLeadReporters(dataflowId)
};
