import { config } from 'conf';

import { BusinessDataflowRepository } from 'repositories/BusinessDataflowRepository';

import { BusinessDataflowUtils } from 'services/_utils/BusinessDataflowUtils';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const BusinessDataflowService = {
  getAll: async (accessRole, contextRoles) => {
    const businessDataflowsDTO = await BusinessDataflowRepository.getAll();

    const businessDataflows = !accessRole ? businessDataflowsDTO.data : [];

    const userRoles = [];
    if (contextRoles) {
      const dataflowsRoles = contextRoles.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));
      dataflowsRoles.map((item, i) => {
        const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);

        return (userRoles[i] = {
          id: parseInt(item.replace(/\D/g, '')),
          userRole: UserRoleUtils.getUserRoleLabel(role)
        });
      });
    }

    for (let index = 0; index < businessDataflowsDTO.data.length; index++) {
      const businessDataflow = businessDataflowsDTO.data[index];

      const isOpen = businessDataflow.status === config.dataflowStatus.OPEN;

      if (isOpen) {
        businessDataflow.releasable ? (businessDataflow.status = 'OPEN') : (businessDataflow.status = 'CLOSED');
      }

      if (contextRoles.length === 0) {
        businessDataflow.userRole =
          accessRole.some(role => role === config.permissions.roles.ADMIN.key) && config.permissions.roles.ADMIN.key; // TODO WITH TWO ROLES
        businessDataflows.push({
          ...businessDataflow
        });
      } else {
        const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
        businessDataflows.push({
          ...businessDataflow,
          ...(isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles).find(
            item => item.id === businessDataflow.id
          )
        });
      }
    }
    return BusinessDataflowUtils.parseSortedBusinessDataflowListDTO(businessDataflows);
  },

  create: async (name, description, obligationId, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.create(name, description, obligationId, dataProviderGroupId, fmeUserId),

  update: async (dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId) =>
    BusinessDataflowRepository.update(dataflowId, description, obligationId, name, dataProviderGroupId, fmeUserId),

  delete: async dataflowId => BusinessDataflowRepository.delete(dataflowId)
};
