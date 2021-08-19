import isNil from 'lodash/isNil';
import reverse from 'lodash/reverse';

import { FeedbackRepository } from 'repositories/FeedbackRepository';

import { Feedback } from 'entities/Feedback';
import { FeedbackMessageAttachment } from 'entities/FeedbackMessageAttachment';

export const FeedbackService = {
  createMessage: async (dataflowId, message, providerId, type, messageAttachment) =>
    await FeedbackRepository.createMessage(dataflowId, message, providerId, type, messageAttachment),

  getAllMessages: async (dataflowId, page, dataProviderId) => {
    const response = await FeedbackRepository.getAllMessages(dataflowId, page, dataProviderId);
    response.data = reverse(
      response.data.map(
        message =>
          new Feedback({
            ...message,
            messageAttachment: !isNil(message.messageAttachmentVO)
              ? new FeedbackMessageAttachment(...message.messageAttachmentVO)
              : null
          })
      )
    );
    return response;
  },

  getMessageAttachment: async (dataflowId, messageId, dataProviderId) =>
    await FeedbackRepository.getMessageAttachment(dataflowId, messageId, dataProviderId),

  markMessagesAsRead: async (dataflowId, messages) => await FeedbackRepository.markMessagesAsRead(dataflowId, messages)
};
