import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';

const all = async dataFlowId => {
  const contributorsDTO = await apiContributor.all(dataFlowId);
  return contributorsDTO.map(
    contributorDTO => new Contributor(contributorDTO.id, contributorDTO.login, contributorDTO.role)
  );
};

const addByLogin = async (dataFlowId, login, role) => {
  return await apiContributor.addByLogin(dataFlowId, login, role);
};

const deleteById = async (dataFlowId, contributorId) => {
  const dataDeleted = await apiContributor.deleteById(dataFlowId, contributorId);
  return dataDeleted;
};

const updateById = async (dataFlowId, contributorId, contributorRole) => {
  return await apiContributor.updateById(dataFlowId, contributorId, contributorRole);
};

export const ApiContributorRepository = {
  all,
  addByLogin,
  deleteById,
  updateById
};
