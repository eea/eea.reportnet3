import { apiRepresentative } from 'core/infrastructure/api/domain/model/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';

const allRepresentatives = async dataflowId => {
  const representativesDTO = await apiRepresentative.allRepresentatives(dataflowId);

  const representativesList = representativesDTO.Representatives.map(
    representativeDTO =>
      new Representative(
        representativeDTO.representativesId,
        representativeDTO.providerAccount,
        representativeDTO.dataProviderId
      )
  );

  const dataToConsume = {
    group: representativesDTO.dataProviderGroupId,
    representatives: representativesList
  };

  return dataToConsume;
};

const allDataProviders = async dataProviderGroupId => {
  const dataProvidersDTO = await apiRepresentative.allDataProviders(dataProviderGroupId);
  //TODO Object to Entity parsing

  return dataProvidersDTO;
};

const getProviderTypes = async () => {
  const dataProviderTypesDTO = await apiRepresentative.getProviderTypes();
  //TODO Object to Consumable obj parsing

  return dataProviderTypesDTO;
};

const add = async (dataflowId, providerAccount, dataProviderId) => {
  return await apiRepresentative.add(dataflowId, providerAccount, dataProviderId);
};

const deleteById = async representativeId => {
  const dataDeleted = await apiRepresentative.deleteById(representativeId);
  return dataDeleted;
};

const update = async (dataflowId, representativeId, providerAccount, dataProviderId) => {
  return await apiRepresentative.update(dataflowId, representativeId, providerAccount, dataProviderId);
};

export const ApiRepresentativeRepository = {
  allRepresentatives,
  allDataProviders,
  add,
  deleteById,
  getProviderTypes,
  update
};
