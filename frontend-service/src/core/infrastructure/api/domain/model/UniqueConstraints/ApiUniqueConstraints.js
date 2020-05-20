import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiUniqueConstraints = {
  all: async datasetSchemaId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(UniqueConstraintsConfig.all, { datasetSchemaId }),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response.data;
  },

  create: async (description, fieldSchemaId, name) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: {
        description,
        fieldSchemaId,
        name
      },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  // parameters in url
  deleteById: async (datasetSchemaId, fieldId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(UniqueConstraintsConfig.delete, {
        datasetSchemaId,
        fieldId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  update: async (description, fieldId, fieldSchemaId, name) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: {
        description,
        fieldId,
        fieldSchemaId,
        name
      },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  }
};
