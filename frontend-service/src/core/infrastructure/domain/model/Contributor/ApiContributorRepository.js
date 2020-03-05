import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';

const all = async dataflowId => {
  const contributorsDTO = await apiContributor.all(dataflowId);
  return contributorsDTO.map(contributorDTO => new Contributor(contributorDTO));
};

const addByLogin = async (dataflowId, login, role) => {
  return await apiContributor.addByLogin(dataflowId, login, role);
};

const deleteById = async (dataflowId, contributorId) => {
  const dataDeleted = await apiContributor.deleteById(dataflowId, contributorId);
  return dataDeleted;
};

const updateById = async (dataflowId, contributorId, contributorRole) => {
  return await apiContributor.updateById(dataflowId, contributorId, contributorRole);
};

export const ApiContributorRepository = {
  all,
  addByLogin,
  deleteById,
  updateById
};
