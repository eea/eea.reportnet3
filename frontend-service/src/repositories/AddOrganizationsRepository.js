import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { AddOrganizationsConfig } from './config/AddOrganizationsConfig';

export const AddOrganizationsRepository = {
  createProvider: async (group, label, code, groupId) =>
    await HTTPRequester.post({
      url: getUrl(AddOrganizationsConfig.createProvider),
      data: { group, label, code, groupId }
    }),
  getOrganizations: async ({ pageNum, numberRows, sortOrder, sortField = '', providerCode, groupId, label }) =>
    await HTTPRequester.get({
      url: getUrl(AddOrganizationsConfig.getOrganizations, {
        pageNum,
        numberRows,
        sortOrder,
        sortField,
        providerCode,
        groupId,
        label
      })
    })
};
