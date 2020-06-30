import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';
import isEmpty from 'lodash/isEmpty';
import sortBy from 'lodash/sortBy';

const all = async dataflowId => {
  console.log('TEST');

  console.log('dataflowId', dataflowId);
  // console.log('dataProviderId', dataProviderId);

  const contributorsDTO = await apiContributor.all(dataflowId);
  console.log('contributorsDTO', contributorsDTO);
  const contributorsList = !isEmpty(contributorsDTO.data)
    ? contributorsDTO.data.map(contributorDTO => new Contributor(contributorDTO))
    : [];

  return sortBy(contributorsList, ['account']);
};

const add = async (Contributor, dataflowId) => {
  return await apiContributor.add(Contributor, dataflowId);
};

const deleteContributor = async (Contributor, dataflowId) => {
  return await apiContributor.deleteContributor(Contributor, dataflowId);
};

const updateWritePermission = async (Contributor, dataflowId) => {
  return await apiContributor.updateWritePermission(Contributor, dataflowId);
};

export const ApiContributorRepository = {
  all,
  add,
  deleteContributor,
  updateWritePermission
};
