import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { AddOrganizationsConfig } from './config/AddOrganizationsConfig';

export const AddOrganizationsRepository = {
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
