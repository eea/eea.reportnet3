import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';

export const apiUniqueConstraints = {
  all: async (dataflowId, datasetSchemaId) => {
    return await HTTPRequester.get({ url: getUrl(UniqueConstraintsConfig.all, { dataflowId, datasetSchemaId }) });
  },

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) => {
    return await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId }
    });
  },

  deleteById: async (dataflowId, uniqueConstraintId) => {
    return await HTTPRequester.delete({
      url: getUrl(UniqueConstraintsConfig.delete, { dataflowId, uniqueConstraintId })
    });
  },

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) => {
    return await HTTPRequester.update({
      url: getUrl(UniqueConstraintsConfig.update),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId }
    });
  }
};
