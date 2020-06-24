import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';

export const apiUniqueConstraints = {
  all: async datasetSchemaId => {
    const response = await HTTPRequester.get({
      url: getUrl(UniqueConstraintsConfig.all, { datasetSchemaId })
    });
    return response.data;
  },

  create: async (datasetSchemaId, fieldSchemaIds, tableSchemaId) => {
    const response = await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: { datasetSchemaId, fieldSchemaIds, tableSchemaId }
    });
    return response;
  },

  deleteById: async uniqueConstraintId => {
    const response = await HTTPRequester.delete({
      url: getUrl(UniqueConstraintsConfig.delete, {
        uniqueConstraintId
      })
    });
    return response;
  },

  update: async (datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UniqueConstraintsConfig.update),
      data: { datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId }
    });
    return response;
  }
};
