import { DataflowRepository } from 'repositories/DataflowRepository';
import { ReferenceDataflowRepository } from 'repositories/ReferenceDataflowRepository';

import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { ReferenceDataflowUtils } from 'services/_utils/ReferenceDataflowUtils';

import { config } from 'conf';

import { CoreUtils } from 'repositories/_utils/CoreUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const ReferenceDataflowService = {
  getAll: async (userData = []) => {
    const dataflowsDTO = await ReferenceDataflowRepository.getAll();
    const userRoles = [];
    const dataflows = [];

    const dataflowsRoles = userData.filter(role => role.includes(config.permissions.prefixes.DATAFLOW));

    dataflowsRoles.map((item, index) => {
      const role = TextUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);
      return (userRoles[index] = {
        id: parseInt(item.replace(/\D/g, '')),
        userRole: UserRoleUtils.getUserRoleLabel(role)
      });
    });

    dataflowsDTO.data.forEach(dataflow => {
      const isDuplicated = CoreUtils.isDuplicatedInObject(userRoles, 'id');
      const role = isDuplicated ? UserRoleUtils.getUserRoles(userRoles) : userRoles;

      dataflows.push({ ...dataflow, ...role.find(item => item.id === dataflow.id) });
    });

    return DataflowUtils.parseSortedDataflowListDTO(dataflows);
  },

  create: async (name, description, type) => ReferenceDataflowRepository.create(name, description, type),

  update: async (dataflowId, description, name, type) =>
    ReferenceDataflowRepository.update(dataflowId, description, name, type),

  getReferencingDataflows: async referenceDataflowId =>
    await ReferenceDataflowRepository.getReferencingDataflows(referenceDataflowId),

  get: async referenceDataflowId => {
    const referenceDataflowDTO = await DataflowRepository.get(referenceDataflowId);
    const dataflow = ReferenceDataflowUtils.parseReferenceDataflowDTO(referenceDataflowDTO.data);
    dataflow.datasets.sort(DatasetUtils.sortDatasetTypeByName);
    dataflow.designDatasets.sort(DatasetUtils.sortDatasetTypeByName);
    return dataflow;
  }
};
