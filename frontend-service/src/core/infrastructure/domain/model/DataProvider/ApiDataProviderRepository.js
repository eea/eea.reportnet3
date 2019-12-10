import { apiDataProvider } from 'core/infrastructure/api/domain/model/DataProvider';
import { DataProvider } from 'core/domain/model/DataProvider/DataProvider';

const all = async dataflowId => {
  const dataProvidersDTO = await apiDataProvider.all(dataflowId);
  return dataProvidersDTO.map(
    dataProviderDTO => new DataProvider(dataProviderDTO.id, dataProviderDTO.email, dataProviderDTO.name)
  );
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
  all,
  add,
  deleteById,
  update
};
