import { WebformConfig } from './config/WebformConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const WebformRepository = {
  addPamsRecords: async (datasetId, pamsRecord) =>
    await HTTPRequester.post({ url: getUrl(WebformConfig.createPamsRecords, { datasetId }), data: pamsRecord }),

  getSinglePamData: async (datasetId, groupPaMId) =>
    await HTTPRequester.get({ url: getUrl(WebformConfig.getSinglePamData, { datasetId, groupPaMId }) }),

  listAll: async () => {
    const { data } = await HTTPRequester.get({ url: getUrl(WebformConfig.listAll) });
    // const data = [
    //   { label: 'No webform', value: null },
    //   { label: 'Governance Regulation Art. 19', value: 'MMR-ART15' },
    //   { label: 'Governance Regulation Art. 13', value: 'MMR-ART13' },
    //   { label: 'National Systems', value: 'NATIONAL-SYSTEMS' }
    // ];
    return data;
  },

  getWebformConfig: async webformId => {
    const { data } = await HTTPRequester.get({ url: getUrl(WebformConfig.getWebformConfig, { webformId }) });
    return data;
  }
};
