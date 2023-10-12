import { AddOrganizationsRepository } from 'repositories/AddOrganizationsRepository';

import { AddOrganizationsUtils } from './_utils/AddOrganizationsUtils';
import { ServiceUtils } from 'services/_utils/ServiceUtils';

export const AddOrganizationsService = {
  createProvider: async ({ group, label, code, groupId }) =>
    await AddOrganizationsRepository.createProvider(group, label, code, groupId),
  getOrganizations: async ({ pageNum, numberRows, sortOrder, sortField, providerCode, groupId, label }) => {
    const parsedSortField = AddOrganizationsUtils.parseSortField(sortField);

    const response = await AddOrganizationsRepository.getOrganizations({
      pageNum,
      numberRows,
      sortOrder: ServiceUtils.getSortOrder(sortOrder),
      sortField: parsedSortField,
      providerCode,
      groupId,
      label
    });

    return response.data;
  }
};
