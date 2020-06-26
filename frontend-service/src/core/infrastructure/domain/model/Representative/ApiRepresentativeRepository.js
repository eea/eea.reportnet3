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

const add = async (dataflowId, account, dataProviderId) => {
  return await apiRepresentative.add(dataflowId, account, dataProviderId);
};

const deleteById = async representativeId => {
  return await apiRepresentative.deleteById(representativeId);
};

const updateAccount = async (representativeId, account) => {
  return await apiRepresentative.updateAccount(representativeId, account);
};

const updateDataProviderId = async (representativeId, dataProviderId) => {
  return await apiRepresentative.updateDataProviderId(representativeId, dataProviderId);
};

const updatePermission = async (representativeId, permission) => {
  return await apiRepresentative.updatePermission(representativeId, permission);
};

export const ApiRepresentativeRepository = {
  add,
  allDataProviders,
  allRepresentatives,
  deleteById,
  getProviderTypes,
  updateAccount,
  updateDataProviderId,
  updatePermission
};
