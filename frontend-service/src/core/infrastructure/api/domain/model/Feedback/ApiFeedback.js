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
  loadAllMessages: async (dataflowId, page) => {
    console.log({ dataflowId, page });
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadAllMessages, { dataflowId, page })
    });

    return response.data;
  },
  loadMessagesByFlag: async (dataflowId, page, read) => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.loadMessagesByFlag, { dataflowId, page, read })
    });

    return response.data;
  },
  markAsRead: async (dataflowId, messageIds, read) => {
    const response = await HTTPRequester.get({
      url: getUrl(FeedbackConfig.markAsRead, { dataflowId, messageIds, read })
    });

    return response.data;
  }
};
