import { api } from 'core/infrastructure/api';
import { Contributor } from 'core/domain/model/Contributor/Contributor';

const all = async dataFlowId => {
  const contributorsDTO = await api.contributors(dataFlowId);
  return contributorsDTO.map(
    contributorDTO => new Contributor(contributorDTO.id, contributorDTO.login, contributorDTO.role)
  );
};

const addByLogin = async (dataFlowId, description) => {
  return await api.createContributorByLogin(dataFlowId, description);
};

const deleteById = async (dataFlowId, contributorId) => {
  const dataDeleted = await api.deleteContributorById(dataFlowId, contributorId);
  return dataDeleted;
};

const updateById = async (dataFlowId, contributorId, contributorRole) => {
  return await api.updateContributorById(dataFlowId, contributorId, contributorRole);
};

export const ApiContributorRepository = {
  all,
  addByLogin,
  deleteById,
  updateById
};
