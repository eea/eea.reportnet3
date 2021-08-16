import { FeedbackConfig } from './config/FeedbackConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const FeedbackRepository = {
  createMessage: async (dataflowId, message, dataProviderId, type, messageAttachment) => {
    return await HTTPRequester.post({
      url: getUrl(FeedbackConfig.createMessage, { dataflowId }),
      data: { content: message, providerId: dataProviderId, type, messageAttachment }
    });
  },

  getAllMessages: async (dataflowId, page, dataProviderId) => {
    return await HTTPRequester.get({
      url: getUrl(FeedbackConfig.getAllMessages, { dataflowId, page, providerId: dataProviderId })
    });
  },

  getMessageAttachment: async (dataflowId, messageAttachmentId, dataProviderId) => {
    return await HTTPRequester.download({
      url: getUrl(FeedbackConfig.getMessageAttachment, { dataflowId }),
      data: { messageAttachmentId, providerId: dataProviderId }
    });
  },

  markMessagesAsRead: async (dataflowId, messages) => {
    return await HTTPRequester.update({
      url: getUrl(FeedbackConfig.markMessagesAsRead, { dataflowId }),
      data: messages
    });
  }
};
