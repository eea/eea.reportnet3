import { EuDatasetConfig } from 'conf/domain/model/EuDataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const apiEuDataset = {
  copyDataCollection: async dataflowId => {
    const response = await HTTPRequester.post({
      url: getUrl(EuDatasetConfig.copyDataCollection, { dataflowId })
    });
    return response;
  },

  exportEuDataset: async dataflowId => {
    const response = await HTTPRequester.post({
      url: getUrl(EuDatasetConfig.export, { dataflowId })
    });
    return response;
  }
};
export { apiEuDataset };
