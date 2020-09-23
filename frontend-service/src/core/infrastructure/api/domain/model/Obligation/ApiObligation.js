import { ObligationConfig } from 'conf/domain/model/Obligation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiObligation = {
  getCountries: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.countries)
    });

    return response.data;
  },

  getIssues: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.issues)
    });

    return response.data;
  },

  getOrganizations: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.organizations)
    });

    return response.data;
  },

  obligationById: async obligationId => {
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.obligationById, { obligationId })
    });

    return response.data;
  },

  openedObligations: async (countryId = '', dateFrom = '', dateTo = '', issueId = '', organizationId = '') => {
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.openedObligations, { countryId, dateFrom, dateTo, issueId, organizationId })
    });

    return response.data;
  }
};
