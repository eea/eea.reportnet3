import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { BusinessDataflowUtils } from 'services/_utils/BusinessDataflowUtils';

import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const BusinessDataflowService = {
  getAll: async (accessRoles, contextRoles) => {
    const businessDataflowsDTO = await BusinessDataflowRepository.getAll();
    const businessDataflows = businessDataflowsDTO.data.map(businessDataflowDTO => {
      businessDataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(
        businessDataflowDTO.id,
        accessRoles,
        contextRoles
      );
      return businessDataflowDTO;
    });

    return BusinessDataflowUtils.parseSortedBusinessDataflowListDTO(businessDataflows);
  },

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.update(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId)
};
