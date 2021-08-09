import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

export const BusinessDataflowService = {
  getAll: async userData => BusinessDataflowRepository.getAll(userData),

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.update(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId)
};
