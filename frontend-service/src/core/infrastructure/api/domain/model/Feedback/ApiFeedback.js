import { FeedbackConfig } from 'conf/domain/model/Feedback';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiFeedback = {
  create: async (dataflowId, message, providerId) => {
    const response = await HTTPRequester.post({
      url: getUrl(FeedbackConfig.create, { dataflowId }),
      data: { content: message, providerId }
    });
    return response.status >= 200 && response.status <= 299;
  },
  loadMessages: async (dataflowId, page) => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadMessages, { dataflowId, page })
    });

    return response.data;
  },
  loadMessagesByFlag: async (dataflowId, page, read) => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadMessagesByFlag, { dataflowId, page, read })
    });

    return response.data;
  },
  markAsRead: async (dataflowId, messages) => {
    const response = await HTTPRequester.update({
      url: getUrl(FeedbackConfig.markAsRead, { dataflowId }),
      data: messages
    });

    return response.status >= 200 && response.status <= 299;
  }
};
