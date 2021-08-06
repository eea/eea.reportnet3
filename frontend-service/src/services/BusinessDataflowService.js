import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

const create = async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
  BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId);

const edit = async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
  BusinessDataflowRepository.edit(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId);

const getAll = async userData => BusinessDataflowRepository.all(userData);

export const BusinessDataflowService = {
  create,
  edit,
  getAll
};
