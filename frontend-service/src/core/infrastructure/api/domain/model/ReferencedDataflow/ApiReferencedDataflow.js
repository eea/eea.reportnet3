import { DataflowConfig } from 'conf/domain/model/Dataflow';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiReferenceDataflow = {
  all: async () => await HTTPRequester.get({ url: getUrl(DataflowConfig.reference) }),

  create: async (name, description, type) => {
    return await HTTPRequester.post({
      url: getUrl(DataflowConfig.createDataflow),
      // data: { description, name, releasable: true, type }
      data: { description, name, type }
    });
  }
};
