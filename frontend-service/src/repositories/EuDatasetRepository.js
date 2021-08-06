import { EuDatasetConfig } from './config/EuDatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const EuDatasetRepository = {
  copyFromDataCollection: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.copyFromDataCollection, { dataflowId }) });
  },

  export: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.export, { dataflowId }) });
  }
};
