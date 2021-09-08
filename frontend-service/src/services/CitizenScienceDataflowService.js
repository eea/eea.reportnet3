import { config } from 'conf';

import { CitizenScienceDataflowRepository } from 'repositories/CitizenScienceDataflowRepository';

import { DataflowUtils } from 'services/_utils/DataflowUtils';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const CitizenScienceDataflowService = {
  getAll: async userData => {
    const dataflowsDTO = await CitizenScienceDataflowRepository.getAll();
    const dataflows = !userData ? dataflowsDTO.data : [];

    if (userData) {
      const userRoles = [];
      const dataflowsRoles = userData.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));
      dataflowsRoles.map((item, i) => {
        const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);

        return (userRoles[i] = {
          id: parseInt(item.replace(/\D/g, '')),
          userRole: UserRoleUtils.getUserRoleLabel(role)
        });
      });

      for (let index = 0; index < dataflowsDTO.data.length; index++) {
        const dataflow = dataflowsDTO.data[index];
        const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
        const isOpen = dataflow.status === config.dataflowStatus.OPEN;

        if (isOpen) {
          dataflow.status = dataflow.releasable ? 'OPEN' : 'CLOSED';
        }

        dataflows.push({
          ...dataflow,
          ...(isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles).find(item => item.id === dataflow.id)
        });
      }
    }

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
