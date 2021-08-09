import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { UniqueConstraintsConfig } from './config/UniqueConstraintsConfig';

export const UniqueConstraintsRepository = {
  getAll: async (dataflowId, datasetSchemaId) =>
    await HTTPRequester.get({ url: getUrl(UniqueConstraintsConfig.getAll, { dataflowId, datasetSchemaId }) }),

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
    await HTTPRequester.post({
      url: getUrl(UniqueConstraintsConfig.create),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId }
    }),

  delete: async (dataflowId, uniqueConstraintId) =>
    await HTTPRequester.delete({ url: getUrl(UniqueConstraintsConfig.delete, { dataflowId, uniqueConstraintId }) }),

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) =>
    await HTTPRequester.update({
      url: getUrl(UniqueConstraintsConfig.update),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId }
    })
};
