import { FeedbackConfig } from 'conf/domain/model/Feedback';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiFeedback = {
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

  loadMessagesByFlag: async (dataflowId, page, read, dataProviderId) => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadMessagesByFlag, { dataflowId, page, read, providerId: dataProviderId })
    });
    return response.data;
  },

  markAsRead: async (dataflowId, messages) => {
    return await HTTPRequester.update({ url: getUrl(FeedbackConfig.markAsRead, { dataflowId }), data: messages });
  }
};
