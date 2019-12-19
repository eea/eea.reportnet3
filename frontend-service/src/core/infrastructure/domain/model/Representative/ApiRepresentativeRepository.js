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
    group: representativesDTO.groupId,
    representatives: representativesList
  };

  return dataToConsume;
};

const allDataProviders = async type => {
  const dataProvidersDTO = await apiRepresentative.allDataProviders(type);
  return dataProvidersDTO;
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
  update
};
