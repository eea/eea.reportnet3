import { WebformConfig } from 'conf/domain/model/Webform';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const apiWebform = {
  addPamsRecords: async (datasetId, pamsRecord) => {
    return await HTTPRequester.post({ url: getUrl(WebformConfig.createPamsRecords, { datasetId }), data: pamsRecord });
  }
};
