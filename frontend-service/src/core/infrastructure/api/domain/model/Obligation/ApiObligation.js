import { ObligationConfig } from 'conf/domain/model/Obligation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiObligation = {
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
