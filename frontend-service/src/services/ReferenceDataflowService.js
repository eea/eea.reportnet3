import { DataflowRepository } from 'repositories/DataflowRepository';
import { ReferenceDataflowRepository } from 'repositories/ReferenceDataflowRepository';

import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { ReferenceDataflowUtils } from 'services/_utils/ReferenceDataflowUtils';
import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

export const ReferenceDataflowService = {
  getAll: async ({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy }) => {
    const [isAsc] = Object.values(sortBy);
    const [sortByHeader] = Object.keys(sortBy);
    const filteredFilterBy = DataflowUtils.parseRequestFilterBy(filterBy);

    const referenceDataflowsDTO = await ReferenceDataflowRepository.getAll({
      filterBy: filteredFilterBy,
      isAsc: isAsc || undefined,
      numberRows,
      pageNum,
      sortBy: sortByHeader || undefined
    });

    const referenceDataflows = referenceDataflowsDTO.data.map(referenceDataflowDTO => {
      referenceDataflowDTO.userRole = UserRoleUtils.getUserRoleByDataflow(
        referenceDataflowDTO.id,
        accessRoles,
        contextRoles
      );
      return referenceDataflowDTO;
    });

    return DataflowUtils.parseSortedDataflowListDTO(referenceDataflows);
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
