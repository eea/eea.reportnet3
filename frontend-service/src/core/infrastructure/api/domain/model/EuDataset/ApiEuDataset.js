import { EuDatasetConfig } from 'conf/domain/model/EuDataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiEuDataset = {
  copyDataCollection: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.copyDataCollection, { dataflowId }) });
  },

  exportEuDataset: async dataflowId => {
    return await HTTPRequester.post({ url: getUrl(EuDatasetConfig.export, { dataflowId }) });
  }
};

export { apiEuDataset };
