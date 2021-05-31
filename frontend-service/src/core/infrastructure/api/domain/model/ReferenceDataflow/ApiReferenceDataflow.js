import { ReferenceDataflowConfig } from 'conf/domain/model/ReferenceDataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiReferenceDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.all) }),
  referenceDataflow: async () => await HTTPRequester.get({ url: getUrl(ReferenceDataflowConfig.referenceDataflow) })
};
