import { FeedbackConfig } from 'conf/domain/model/Feedback';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiFeedback = {
  all: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadAll)
    });

    return response.data;
  }
};
