import isNil from 'lodash/isNil';
import reverse from 'lodash/reverse';

import { FeedbackRepository } from 'repositories/FeedbackRepository';

import { Feedback } from 'entities/Feedback';
import { FeedbackMessageAttachment } from 'entities/FeedbackMessageAttachment';

export const FeedbackService = {
  createMessage: async (dataflowId, message, providerId, type, messageAttachment) =>
    await FeedbackRepository.createMessage(dataflowId, message, providerId, type, messageAttachment),

  deleteMessage: async (dataflowId, messageId, providerId) =>
    await FeedbackRepository.deleteMessage(dataflowId, messageId, providerId),

  getAllMessages: async (dataflowId, page, dataProviderId) => {
    const response = await FeedbackRepository.getAllMessages(dataflowId, page, dataProviderId);
    response.data.listMessage = reverse(
      response.data.listMessage.map(
        message =>
          new Feedback({
            ...message,
            messageAttachment: !isNil(message.messageAttachment)
              ? new FeedbackMessageAttachment(message.messageAttachment)
              : null
          })
      )
    );
    return response.data;
  },

  getMessageAttachment: async (dataflowId, messageId, dataProviderId) => {
    const response = await FeedbackRepository.getMessageAttachment(dataflowId, messageId, dataProviderId);
    return response.data;
  },

  markMessagesAsRead: async (dataflowId, messages) => await FeedbackRepository.markMessagesAsRead(dataflowId, messages)
};
