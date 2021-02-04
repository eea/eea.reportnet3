import { apiRepresentative } from 'core/infrastructure/api/domain/model/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';
import isEmpty from 'lodash/isEmpty';
import sortBy from 'lodash/sortBy';

const allRepresentatives = async dataflowId => {
  const representativesDTO = await apiRepresentative.allRepresentatives(dataflowId);

  const representativesList = !isEmpty(representativesDTO.data)
    ? representativesDTO.data.map(representativeDTO => new Representative(representativeDTO))
    : [];

  const dataToConsume = {
    group: !isEmpty(representativesDTO.data)
      ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId }
      : { dataProviderGroupId: null },
    representatives: sortBy(representativesList, ['representativeId'])
  };

  return dataToConsume;
};

const allDataProviders = async dataProviderGroup => {
  let response = [];

  const dataProvidersDTO = await apiRepresentative.allDataProviders(dataProviderGroup.dataProviderGroupId);

  response = dataProvidersDTO.data.map(dataProvider => {
    return { dataProviderId: dataProvider.id, label: dataProvider.label };
  });

  return response;
};

const getProviderTypes = async () => {
  const dataProviderTypesDTO = await apiRepresentative.getProviderTypes();

  return dataProviderTypesDTO.data;
};

const add = async (dataflowId, providerAccount, dataProviderId) => {
  return await apiRepresentative.add(dataflowId, providerAccount, dataProviderId);
};

const deleteById = async representativeId => {
  return await apiRepresentative.deleteById(representativeId);
};

const downloadById = async dataflowId => {
  return await apiRepresentative.downloadById(dataflowId);
};

const updateProviderAccount = async (representativeId, providerAccount) => {
  return await apiRepresentative.updateProviderAccount(representativeId, providerAccount);
};

const updateDataProviderId = async (representativeId, dataProviderId) => {
  return await apiRepresentative.updateDataProviderId(representativeId, dataProviderId);
};

export const ApiRepresentativeRepository = {
  allRepresentatives,
  allDataProviders,
  add,
  deleteById,
  downloadById,
  getProviderTypes,
  updateProviderAccount,
  updateDataProviderId
};
