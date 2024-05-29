import { CitizenScienceDataflowRepository } from 'repositories/CitizenScienceDataflowRepository';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const CitizenScienceDataflowService = {
  getAll: async ({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy }) => {
    const { isAsc, sortByHeader } = DataflowUtils.parseRequestSortBy(sortBy);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const dataflowsDTO = await CitizenScienceDataflowRepository.getAll({
      filterBy: filteredFilterBy,
      isAsc,
      numberRows,
      pageNum,
      sortBy: sortByHeader
    });

    const dataflows = dataflowsDTO.data.dataflows.map(dataflowDTO => {
      dataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(dataflowDTO.id, accessRoles, contextRoles);
      return dataflowDTO;
    });

    return { ...dataflowsDTO.data, dataflows: DataflowUtils.parseDataflowListDTO(dataflows) };
  },

  create: async (name, description, obligationId, dataProviderGroupId) =>
    await CitizenScienceDataflowRepository.create(name, description, obligationId, dataProviderGroupId),

  update: async (dataflowId, name, description, obligationId, isReleasable, showPublicInfo) =>
    await CitizenScienceDataflowRepository.update(
      dataflowId,
      name,
      description,
      obligationId,
      isReleasable,
      showPublicInfo
    )
};
