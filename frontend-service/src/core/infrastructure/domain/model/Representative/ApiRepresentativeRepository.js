import { apiRepresentative } from 'core/infrastructure/api/domain/model/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';
import isEmpty from 'lodash/isEmpty';
import sortBy from 'lodash/sortBy';

const add = async (dataflowId, providerAccount, dataProviderId) => {
  return await apiRepresentative.add(dataflowId, providerAccount, dataProviderId);
};

const addLeadReporter = async (leadReporterAccount, representativeId) => {
  return await apiRepresentative.addLeadReporter(leadReporterAccount, representativeId);
};

const allDataProviders = async dataProviderGroup => {
  let response = [];
  const dataProvidersDTO = await apiRepresentative.allDataProviders(dataProviderGroup.dataProviderGroupId);
  response = dataProvidersDTO.data.map(dataProvider => {
    return { dataProviderId: dataProvider.id, label: dataProvider.label };
  });
  return response;
};

const allRepresentatives = async dataflowId => {
  const representativesDTO = await apiRepresentative.allRepresentatives(dataflowId);
  const representativesList = !isEmpty(representativesDTO.data)
    ? representativesDTO.data.map(
        representativeDTO =>
          new Representative({
            dataProviderGroupId: representativeDTO.dataProviderGroupId,
            dataProviderId: representativeDTO.dataProviderId,
            id: representativeDTO.id,
            isReceiptDownloaded: representativeDTO.receiptDownloaded,
            isReceiptOutdated: representativeDTO.receiptOutdated,
            // leadReporters: representativeDTO.providerAccounts.unshift({ id: 777, account: 'pablo@man.com' }),
            leadReporters: [
              { account: representativeDTO.providerAccounts[0], id: 50 },
              { id: 777, account: 'pablo@man.com' }
            ],
            hasDatasets: representativeDTO.hasDatasets
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

const deleteById = async representativeId => await apiRepresentative.deleteById(representativeId);

const deleteLeadReporter = async leadReporterId => await apiRepresentative.deleteLeadReporter(leadReporterId);

const getProviderTypes = async () => {
  const dataProviderTypesDTO = await apiRepresentative.getProviderTypes();
  return dataProviderTypesDTO.data;
};

const updateDataProviderId = async (representativeId, dataProviderId) =>
  await apiRepresentative.updateDataProviderId(representativeId, dataProviderId);

const updateLeadReporter = async (leadReporterAccount, leadReporterId, representativeId) =>
  await apiRepresentative.updateLeadReporter(leadReporterAccount, leadReporterId, representativeId);

const updateProviderAccount = async (representativeId, providerAccount) =>
  await apiRepresentative.updateProviderAccount(representativeId, providerAccount);

export const ApiRepresentativeRepository = {
  add,
  addLeadReporter,
  allDataProviders,
  allRepresentatives,
  deleteById,
  deleteLeadReporter,
  getProviderTypes,
  updateDataProviderId,
  updateLeadReporter,
  updateProviderAccount
};
