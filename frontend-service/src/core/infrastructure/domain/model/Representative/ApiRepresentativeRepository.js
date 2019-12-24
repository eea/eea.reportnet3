import { apiRepresentative } from 'core/infrastructure/api/domain/model/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';
import { isEmpty } from 'lodash';

const allRepresentatives = async dataflowId => {
  const representativesDTO = await apiRepresentative.allRepresentatives(dataflowId);

  const objectExampleDTO = [
    {
      id: 1,
      dataProviderId: 5,
      providerAccount: 'pablo@amo.puto',
      dataProviderGroupId: 1
    },
    {
      id: 2,
      dataProviderId: 2,
      providerAccount: 'igor@amo.puto',
      dataProviderGroupId: 1
    }
  ];

  const representativesList = !isEmpty(representativesDTO)
    ? objectExampleDTO.map(
        representativeDTO =>
          new Representative(representativeDTO.id, representativeDTO.providerAccount, representativeDTO.dataProviderId)
      )
    : [];
  /*   const representativesList = !isEmpty(representativesDTO.data)
    ? representativesDTO.data.map(
        representativeDTO =>
          new Representative(
            representativeDTO.representativesId,
            representativeDTO.providerAccount,
            representativeDTO.dataProviderId
          )
      )
    : []; */

  /*  const dataToConsume = {
    group: !isEmpty(representativesDTO.data)
      ? { dataProviderGroupId: representativesDTO.data[0].dataProviderGroupId }
      : { dataProviderGroupId: null },
    representatives: representativesList
  }; */
  const dataToConsume = {
    group: !isEmpty(objectExampleDTO)
      ? { dataProviderGroupId: objectExampleDTO[0].dataProviderGroupId, label: 'Country' }
      : { dataProviderGroupId: null },
    representatives: representativesList
  };

  return dataToConsume;
};

const allDataProviders = async dataProviderGroup => {
  console.log('Recived Group', dataProviderGroup);
  const dataProvidersDTO = await apiRepresentative.allDataProviders(1); // hard coded dataProviderGroupId

  const response = dataProvidersDTO.data.map(dataProvider => {
    return { dataProviderId: dataProvider.id, label: dataProvider.label };
  });

  response.unshift({ dataProviderId: '', label: 'Select...' });

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
