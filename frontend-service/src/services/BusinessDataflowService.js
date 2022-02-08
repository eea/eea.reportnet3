import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { BusinessDataflowUtils } from 'services/_utils/BusinessDataflowUtils';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const BusinessDataflowService = {
  getAll: async ({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy }) => {
    const { isAsc, sortByHeader } = DataflowUtils.parseRequestSortBy(sortBy);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const businessDataflowsDTO = await BusinessDataflowRepository.getAll({
      filterBy: filteredFilterBy,
      isAsc,
      numberRows,
      pageNum,
      sortBy: sortByHeader
    });

    const businessDataflows = businessDataflowsDTO.data.dataflows.map(businessDataflowDTO => {
      businessDataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(
        businessDataflowDTO.id,
        accessRoles,
        contextRoles
      );
      return businessDataflowDTO;
    });

    return {
      ...businessDataflowsDTO.data,
      dataflows: BusinessDataflowUtils.parseBusinessDataflows(businessDataflows)
    };
  },

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.update(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId)
};
