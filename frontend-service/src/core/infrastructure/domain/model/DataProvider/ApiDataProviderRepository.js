import { apiDataProvider } from 'core/infrastructure/api/domain/model/DataProvider';
import { DataProvider } from 'core/domain/model/DataProvider/DataProvider';

const allRepresentatives = async dataflowId => {
  const dataProvidersDTO = await apiDataProvider.all(dataflowId);

  const dataProvidersList = dataProvidersDTO.dataProviders.map(
    dataProviderDTO => new DataProvider(dataProviderDTO.id, dataProviderDTO.email, dataProviderDTO.name)
  );

  const dataToConsume = { representativesOf: dataProvidersDTO.representativesOf, dataProviders: dataProvidersList };

  console.log('dataToConsume', dataToConsume);

  return dataToConsume;
};

const allDataProviders = async type => {
  const representativesDTO = await apiDataProvider.allDataProviders(type);
  return representativesDTO;
};

const add = async (dataflowId, email, name) => {
  return await apiDataProvider.add(dataflowId, email, name);
};

const deleteById = async (dataflowId, dataProviderId) => {
  const dataDeleted = await apiDataProvider.deleteById(dataflowId, dataProviderId);
  return dataDeleted;
};

const update = async (dataflowId, dataProviderId, dataProviderEmail, dataProviderName) => {
  return await apiDataProvider.update(dataflowId, dataProviderId, dataProviderEmail, dataProviderName);
};

export const ApiDataProviderRepository = {
  allRepresentatives,
  allDataProviders,
  add,
  deleteById,
  update
};
