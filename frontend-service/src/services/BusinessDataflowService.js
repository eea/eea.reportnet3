import { businessDataflowRepository } from 'repositories/BusinessDataflowRepository';

const create = async (name, description, obligationId, groupCompaniesId, fmeUserId) =>
  businessDataflowRepository.create(name, description, obligationId, groupCompaniesId, fmeUserId);

const edit = async (dataflowId, description, obligationId, name, groupCompaniesId, fmeUserId) =>
  businessDataflowRepository.edit(dataflowId, description, obligationId, name, groupCompaniesId, fmeUserId);

const getAll = async userData => businessDataflowRepository.all(userData);

export const BusinessDataflowService = {
  create,
  edit,
  getAll
};
