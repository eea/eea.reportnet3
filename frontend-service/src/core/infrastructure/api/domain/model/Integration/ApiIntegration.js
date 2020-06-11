import { IntegrationConfig } from 'conf/domain/model/Integration';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiIntegration = {
  all: async integration => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.all),
      data: integration,
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });

    return response.data;
  },

  create: async integration => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(IntegrationConfig.create),
      data: integration,
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  },

  deleteById: async integrationId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(IntegrationConfig.delete, { integrationId }),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  },

  update: async integration => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(IntegrationConfig.update),
      data: integration,
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  }
};
