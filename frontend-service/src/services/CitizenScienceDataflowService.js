import { CitizenScienceDataflowRepository } from 'repositories/CitizenScienceDataflowRepository';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const CitizenScienceDataflowService = {
  getAll: async ({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy }) => {
    const [isAsc] = Object.values(sortBy);
    const [sortByHeader] = Object.keys(sortBy);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const dataflowsDTO = await CitizenScienceDataflowRepository.getAll({
      filterBy: filteredFilterBy,
      isAsc: isAsc || undefined,
      numberRows,
      pageNum,
      sortBy: sortByHeader || undefined
    });
    const dataflows = dataflowsDTO.data.map(dataflowDTO => {
      dataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(dataflowDTO.id, accessRoles, contextRoles);
      return dataflowDTO;
    });

    return DataflowUtils.parseSortedDataflowListDTO(dataflows);
  },

  create: async (name, description, obligationId) =>
    await CitizenScienceDataflowRepository.create(name, description, obligationId),

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
