import { businessDataflowRepository } from 'repositories/BusinessDataflowRepository';

const create = async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId);

const edit = async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
  businessDataflowRepository.edit(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId);

const getAll = async userData => businessDataflowRepository.all(userData);

export const BusinessDataflowService = {
  create,
  edit,
  getAll
};
