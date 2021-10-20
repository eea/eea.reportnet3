import { WebformRepository } from 'repositories/WebformRepository';

import { WebformUtils } from 'services/_utils/WebformUtils';

export const WebformService = {
  addPamsRecords: async (datasetId, tables, pamId, type) =>
    await WebformRepository.addPamsRecords(datasetId, WebformUtils.parsePamTables(tables, pamId, type)),

  getSinglePamData: async (datasetId, groupPaMId) => await WebformRepository.getSinglePamData(datasetId, groupPaMId),

  listAll: async () => await WebformRepository.listAll()
};
