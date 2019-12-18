import { apiDataProvider } from 'core/infrastructure/api/domain/model/DataProvider';
import { DataProvider } from 'core/domain/model/DataProvider/DataProvider';

const allRepresentatives = async dataflowId => {
  const dataProvidersDTO = await apiDataProvider.allRepresentatives(dataflowId);

  const dataProvidersList = dataProvidersDTO.dataProviders.map(
    dataProviderDTO =>
      new DataProvider(
        dataProviderDTO.representativesId,
        dataProviderDTO.dataProviderAccount,
        dataProviderDTO.dataProviderId
      )
  );

  const dataToConsume = { representativesOf: dataProvidersDTO.representativesOf, dataProviders: dataProvidersList };

  return dataToConsume;
};

const allDataProviders = async type => {
  const dataProvidersDTO = await apiDataProvider.allDataProviders(type);
  return dataProvidersDTO;
};

const add = async (dataflowId, dataProviderAccount, dataProviderId) => {
  return await apiDataProvider.add(dataflowId, dataProviderAccount, dataProviderId);
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
