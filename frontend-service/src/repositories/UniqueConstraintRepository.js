import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';
import { UniqueConstraintConfig } from './config/UniqueConstraintConfig';

export const UniqueConstraintRepository = {
  getAll: async (dataflowId, datasetSchemaId) =>
    await HTTPRequester.get({ url: getUrl(UniqueConstraintConfig.getAll, { dataflowId, datasetSchemaId }) }),

  create: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId) =>
    await HTTPRequester.post({
      url: getUrl(UniqueConstraintConfig.create),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId }
    }),

  delete: async (dataflowId, uniqueConstraintId) =>
    await HTTPRequester.delete({ url: getUrl(UniqueConstraintConfig.delete, { dataflowId, uniqueConstraintId }) }),

  update: async (dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId) =>
    await HTTPRequester.update({
      url: getUrl(UniqueConstraintConfig.update),
      data: { dataflowId, datasetSchemaId, fieldSchemaIds, tableSchemaId, uniqueId }
    })
};
