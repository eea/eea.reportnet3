import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';

export const apiUniqueConstraints = {
  all: async (dataflowId, datasetSchemaId) => {
    const response = await HTTPRequester.get({
      url: getUrl(UniqueConstraintsConfig.all, { dataflowId, datasetSchemaId })
    });
    return response.data;
  },

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) => {
    const response = await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId }
    });
    return response;
  },

  deleteById: async (dataflowId, uniqueConstraintId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(UniqueConstraintsConfig.delete, { dataflowId, uniqueConstraintId })
    });
    return response;
  },

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
    const response = await HTTPRequester.update({
      url: getUrl(UniqueConstraintsConfig.update),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId }
    });
    return response;
  }
};
