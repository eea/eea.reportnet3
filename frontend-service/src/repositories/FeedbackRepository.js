import { FeedbackConfig } from './config/FeedbackConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const feedbackRepository = {
  create: async (dataflowId, message, providerId) => {
    return await HTTPRequester.post({
      url: getUrl(FeedbackConfig.create, { dataflowId }),
      data: { content: message, providerId }
    });
  },

  loadMessages: async (dataflowId, page, dataProviderId) => {
    return await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadMessages, { dataflowId, page, providerId: dataProviderId })
    });
  },

  markAsRead: async (dataflowId, messages) => {
    return await HTTPRequester.update({ url: getUrl(FeedbackConfig.markAsRead, { dataflowId }), data: messages });
  }
};
