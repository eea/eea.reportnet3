import { WebformRepository } from 'repositories/WebformRepository';

import { WebformUtils } from 'services/_utils/WebformUtils';

export const WebformService = {
  addPamsRecords: async (datasetId, tables, pamId, type) =>
    await WebformRepository.addPamsRecords(datasetId, WebformUtils.parsePamTables(tables, pamId, type)),

  create: async webformConfiguration => await WebformRepository.create(webformConfiguration),

  delete: async id => await WebformRepository.delete(id),

  download: async id => await WebformRepository.download(id),

  getAll: async () => WebformUtils.parseWebformListDTO(await WebformRepository.getAll()),

  getSinglePamData: async (datasetId, groupPaMId) => await WebformRepository.getSinglePamData(datasetId, groupPaMId),

  getWebformConfig: async webformId => await WebformRepository.getWebformConfig(webformId),

  update: async webformConfiguration => await WebformRepository.update(webformConfiguration)
};
