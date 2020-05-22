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

  create: async (datasetSchemaId, fieldSchemaIds, tableSchemaId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: { datasetSchemaId, fieldSchemaIds, tableSchemaId },
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  },

  deleteById: async uniqueConstraintId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(UniqueConstraintsConfig.delete, {
        uniqueConstraintId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  update: async (datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(UniqueConstraintsConfig.update),
      data: { datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId },
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response;
  }
};
