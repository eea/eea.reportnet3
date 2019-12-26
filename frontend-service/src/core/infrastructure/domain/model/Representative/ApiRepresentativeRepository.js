import { apiRepresentative } from 'core/infrastructure/api/domain/model/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';
import { isEmpty, isNull } from 'lodash';

const allRepresentatives = async dataflowId => {
  const representativesDTO = await apiRepresentative.allRepresentatives(dataflowId);

  const representativesList = !isEmpty(representativesDTO.data)
    ? representativesDTO.data.map(
        representativeDTO =>
          new Representative(representativeDTO.id, representativeDTO.providerAccount, representativeDTO.dataProviderId)
      )
    : [];

  const dataToConsume = {
    group: !isEmpty(representativesDTO.data)
      ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId, label: 'Country' } // !solve label problem
      : { dataProviderGroupId: null },
    representatives: representativesList
  };

  return dataToConsume;
};

const allDataProviders = async dataProviderGroup => {
  let response = [];

  if (!isNull(dataProviderGroup)) {
    const dataProvidersDTO = await apiRepresentative.allDataProviders(dataProviderGroup.dataProviderGroupId);

    response = dataProvidersDTO.data.map(dataProvider => {
      return { dataProviderId: dataProvider.id, label: dataProvider.label };
    });

    response.unshift({ dataProviderId: '', label: 'Select...' });
  } else {
    const dataProvidersDTO = await apiRepresentative.allDataProviders(1); //hardcoded

    response = dataProvidersDTO.data.map(dataProvider => {
      return { dataProviderId: dataProvider.id, label: dataProvider.label };
    });
  }

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
