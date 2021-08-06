import isEmpty from 'lodash/isEmpty';
import sortBy from 'lodash/sortBy';

import { RepresentativeRepository } from 'repositories/RepresentativeRepository';
import { Representative } from 'entities/Representative';
import { LeadReporter } from 'entities/LeadReporter';

const add = async (dataflowId, providerAccount, dataProviderId) => {
  return await RepresentativeRepository.add(dataflowId, providerAccount, dataProviderId);
};

const addLeadReporter = async (leadReporterAccount, representativeId, dataflowId) => {
  return await RepresentativeRepository.addLeadReporter(leadReporterAccount, representativeId, dataflowId);
};

const allDataProviders = async dataProviderGroup => {
  let response = [];
  const dataProvidersDTO = await RepresentativeRepository.allDataProviders(dataProviderGroup.dataProviderGroupId);
  response = dataProvidersDTO.data.map(dataProvider => {
    return { dataProviderId: dataProvider.id, label: dataProvider.label };
  });
  return response;
};

const allRepresentatives = async dataflowId => {
  const representativesDTO = await RepresentativeRepository.allRepresentatives(dataflowId);

  const representativesList = !isEmpty(representativesDTO.data)
    ? representativesDTO.data.map(
        representativeDTO =>
          new Representative({
            dataProviderGroupId: representativeDTO.dataProviderGroupId,
            dataProviderId: representativeDTO.dataProviderId,
            hasDatasets: representativeDTO.hasDatasets,
            id: representativeDTO.id,
            isReceiptDownloaded: representativeDTO.receiptDownloaded,
            isReceiptOutdated: representativeDTO.receiptOutdated,
            leadReporters: parseLeadReporters(representativeDTO.leadReporters)
          })
      )
    : [];

  const dataToConsume = {
    group: !isEmpty(representativesDTO.data)
      ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId }
      : { dataProviderGroupId: null },
    representatives: sortBy(representativesList, ['representativeId'])
  };
  return dataToConsume;
};

const parseLeadReporters = (leadReporters = []) =>
  leadReporters.map(
    leadReporter =>
      new LeadReporter({
        account: leadReporter.email,
        id: leadReporter.id,
        representativeId: leadReporter.representativeId
      })
  );

const deleteById = async (representativeId, dataflowId) =>
  await RepresentativeRepository.deleteById(representativeId, dataflowId);

const deleteLeadReporter = async (leadReporterId, dataflowId) =>
  await RepresentativeRepository.deleteLeadReporter(leadReporterId, dataflowId);

const downloadById = async dataflowId => {
  return await RepresentativeRepository.downloadById(dataflowId);
};

const downloadTemplateById = async dataProviderGroupId =>
  await RepresentativeRepository.downloadTemplateById(dataProviderGroupId);

const getFmeUsers = async () => await RepresentativeRepository.getFmeUsers();

const getGroupCompanies = async () => await RepresentativeRepository.getGroupCompanies();

const getGroupProviders = async () => await RepresentativeRepository.getGroupProviders();

const updateDataProviderId = async (representativeId, dataProviderId) =>
  await RepresentativeRepository.updateDataProviderId(representativeId, dataProviderId);

const updateLeadReporter = async (leadReporterAccount, leadReporterId, representativeId, dataflowId) =>
  await RepresentativeRepository.updateLeadReporter(leadReporterAccount, leadReporterId, representativeId, dataflowId);

export const RepresentativeService = {
  add,
  addLeadReporter,
  allDataProviders,
  allRepresentatives,
  deleteById,
  deleteLeadReporter,
  downloadById,
  downloadTemplateById,
  getFmeUsers,
  getGroupCompanies,
  getGroupProviders,
  updateDataProviderId,
  updateLeadReporter
};
