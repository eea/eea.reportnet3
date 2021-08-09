import { ValidationConfig } from './config/ValidationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const ValidationRepository = {
  create: async (datasetSchemaId, validationRule) =>
    await HTTPRequester.update({
      url: getUrl(ValidationConfig.create, { datasetId: datasetSchemaId }),
      data: validationRule
    }),

  delete: async (datasetSchemaId, ruleId) =>
    await HTTPRequester.delete({ url: getUrl(ValidationConfig.delete, { datasetSchemaId, ruleId }) }),

  downloadShowValidationsFile: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(ValidationConfig.downloadShowValidationsFile, { datasetId, fileName })
    }),

  generateShowValidationsFile: async datasetId =>
    await HTTPRequester.post({ url: getUrl(ValidationConfig.generateShowValidationsFile, { datasetId }) }),

  getAll: async datasetSchemaId =>
    await HTTPRequester.get({ url: getUrl(ValidationConfig.getAll, { datasetSchemaId }) }),

  update: async (datasetId, validation) => {
    let url = getUrl(ValidationConfig.update, { datasetId });
    if (validation.automatic) {
      url = getUrl(ValidationConfig.updateAutomatic, { datasetId });
    }
    return await HTTPRequester.update({
      url: url,
      data: validation
    });
  }
};
