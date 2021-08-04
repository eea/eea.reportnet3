import { EuDatasetConfig } from './config/EuDatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

const euDatasetRepository = {
  copyDataCollection: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.copyDataCollection, { dataflowId }) });
  },

  exportEuDataset: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.export, { dataflowId }) });
  }
};

export { euDatasetRepository };
