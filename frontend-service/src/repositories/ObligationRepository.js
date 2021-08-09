import { ObligationConfig } from './config/ObligationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ObligationRepository = {
  getCountries: async () => await HTTPRequester.get({ url: getUrl(ObligationConfig.getCountries) }),

  getIssues: async () => await HTTPRequester.get({ url: getUrl(ObligationConfig.getIssues) }),

  getOrganizations: async () => await HTTPRequester.get({ url: getUrl(ObligationConfig.getOrganizations) }),

  get: async obligationId => await HTTPRequester.get({ url: getUrl(ObligationConfig.get, { obligationId }) }),

  getOpen: async (countryId = '', dateFrom = '', dateTo = '', issueId = '', organizationId = '') =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.getOpen, { countryId, dateFrom, dateTo, issueId, organizationId })
    })
};
