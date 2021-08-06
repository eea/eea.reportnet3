import { ObligationConfig } from './config/ObligationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ObligationRepository = {
  getCountries: async () =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.countries)
    }),

  getIssues: async () =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.issues)
    }),

  getOrganizations: async () =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.organizations)
    }),
  obligationById: async obligationId =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.obligationById, { obligationId })
    }),

  openedObligations: async (countryId = '', dateFrom = '', dateTo = '', issueId = '', organizationId = '') =>
    await HTTPRequester.get({
      url: getUrl(ObligationConfig.openedObligations, { countryId, dateFrom, dateTo, issueId, organizationId })
    })
};
