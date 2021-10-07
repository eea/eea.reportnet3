import { CitizenScienceDataflowRepository } from 'repositories/CitizenScienceDataflowRepository';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const CitizenScienceDataflowService = {
  getAll: async (accessRoles, contextRoles) => {
    const dataflowsDTO = await CitizenScienceDataflowRepository.getAll();

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
