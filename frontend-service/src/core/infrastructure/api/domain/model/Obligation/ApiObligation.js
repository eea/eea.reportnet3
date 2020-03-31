import { ObligationConfig } from 'conf/domain/model/Obligation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiObligation = {
  getClients: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.clients),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });

    return response;
  },

  getCountries: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.countries),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });

    return response;
  },

  getIssues: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.issues),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });

    return response;
  },

  getObligationByID: async obligationId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.obligationById, {
        obligationId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  openedObligations: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.openedObligations),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  }
};
