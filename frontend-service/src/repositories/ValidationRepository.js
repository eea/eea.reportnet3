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

  downloadQCRulesFile: async (datasetId, fileName) =>
    await HTTPRequester.download({
      url: getUrl(ValidationConfig.downloadQCRulesFile, { datasetId, fileName })
    }),

  downloadHistoricReleaseFile: async (datasetId,dataflowId,nameFile,processId) =>
    await HTTPRequester.download({
      url: getUrl(ValidationConfig.downloadHistoricReleaseFile, { datasetId, dataflowId, nameFile,processId })
    }),

  downloadShowValidationsFile: async (datasetId, fileName, code) => {
    return await HTTPRequester.download({
      url: getUrl(ValidationConfig.downloadShowValidationsFile, { datasetId, fileName, code })
    });
  },

  generateQCRulesFile: async datasetId =>
    await HTTPRequester.post({ url: getUrl(ValidationConfig.generateQCRulesFile, { datasetId }) }),

  generateHistoricDataFile: async (datasetId, dataflowId) =>
    await HTTPRequester.post({ url: getUrl(ValidationConfig.generateHistoricDataFile, { datasetId,dataflowId }) }),

  generateShowValidationsFile: async ({ datasetId, code }) =>
    await HTTPRequester.post({ url: getUrl(ValidationConfig.generateShowValidationsFile, { datasetId, code }) }),

  getAll: async (dataflowId, datasetSchemaId) =>
    await HTTPRequester.get({ url: getUrl(ValidationConfig.getAll, { dataflowId, datasetSchemaId }) }),

  getAllQCsHistoricInfo: async datasetId =>
    await HTTPRequester.get({
      url: getUrl(ValidationConfig.getAllQCsHistoricInfo, { datasetId })
    }),

  getQcHistoricInfo: async (datasetId, ruleId) =>
    await HTTPRequester.get({
      url: getUrl(ValidationConfig.getQcHistoricInfo, { datasetId, ruleId })
    }),

  runSqlRule: async (datasetId, sqlSentence, showInternalFields) =>
    await HTTPRequester.post({
      url: getUrl(ValidationConfig.runSqlRule, { datasetId, sqlSentence, showInternalFields }),
      data: { sqlRule: sqlSentence }
    }),

  runSqlRuleAsProvider: async (datasetId, sqlSentence, showInternalFields, providerCode) =>
    await HTTPRequester.post({
      url: getUrl(ValidationConfig.runSqlRuleAsProvider, { datasetId, sqlSentence, showInternalFields, providerCode }),
      data: { sqlRule: sqlSentence }
    }),

  update: async (datasetId, validationRule) =>
    await HTTPRequester.update({
      url: getUrl(validationRule.automatic ? ValidationConfig.updateAutomatic : ValidationConfig.update, { datasetId }),
      data: validationRule
    }),

  evaluateSqlSentence: async (datasetId, sqlSentence) =>
    await HTTPRequester.post({
      url: getUrl(ValidationConfig.evaluateSqlSentence, { datasetId }),
      data: { sqlRule: sqlSentence }
    }),

  viewUpdated: async datasetId => await HTTPRequester.get({ url: getUrl(ValidationConfig.viewUpdated, { datasetId }) }),

  setDefaultSeverity: async (datasetId, datasetSchema, severity) =>
    await HTTPRequester.update({
      url: getUrl(ValidationConfig.setDefaultSeverity, {
        datasetId,
        datasetSchema,
        automaticQCsDefaultLevelError: severity
      })
    })
};
