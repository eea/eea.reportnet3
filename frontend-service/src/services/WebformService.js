import { WebformRepository } from 'repositories/WebformRepository';

import { WebformUtils } from 'services/_utils/WebformUtils';

export const WebformService = {
  addPamsRecords: async (datasetId, tables, pamId, type) =>
    await WebformRepository.addPamsRecords(datasetId, WebformUtils.parsePamTables(tables, pamId, type)),

  create: async webform => await WebformRepository.create(webform),

  delete: async id => await WebformRepository.delete(id),

  download: async id => await WebformRepository.download(id),

  getAll: async () => await WebformRepository.getAll(),

  getSinglePamData: async (datasetId, groupPaMId) => await WebformRepository.getSinglePamData(datasetId, groupPaMId),

  getWebformConfig: async webformId => await WebformRepository.getWebformConfig(webformId),

  update: async (webform, id) => await WebformRepository.update(webform, id)
};
