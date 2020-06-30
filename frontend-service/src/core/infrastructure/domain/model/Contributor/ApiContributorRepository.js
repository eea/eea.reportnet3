import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';
import isEmpty from 'lodash/isEmpty';

const all = async dataflowId => {
  const contributorsDTO = await apiContributor.all(dataflowId);

  const contributors = !isEmpty(contributorsDTO)
    ? contributorsDTO.map(contributorDTO => new Contributor(contributorDTO))
    : [];

  console.log('contributors', contributors);
  return contributors;
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
