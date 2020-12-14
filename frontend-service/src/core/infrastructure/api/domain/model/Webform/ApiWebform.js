import { WebformConfig } from 'conf/domain/model/Webform';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiWebform = {
  addPamsRecords: async (datasetId, pamsRecord) => {
    return await HTTPRequester.post({ url: getUrl(WebformConfig.createPamsRecords, { datasetId }), data: pamsRecord });
  }
};
