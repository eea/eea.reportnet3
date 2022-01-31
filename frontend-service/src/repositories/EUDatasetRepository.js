import { EUDatasetConfig } from './config/EUDatasetConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const EUDatasetRepository = {
  copyFromDataCollection: async dataflowId =>
    await HTTPRequester.post({ url: getUrl(EUDatasetConfig.copyFromDataCollection, { dataflowId }) }),

  export: async dataflowId => await HTTPRequester.post({ url: getUrl(EUDatasetConfig.export, { dataflowId }) })
};
