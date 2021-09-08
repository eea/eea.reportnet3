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
    response.data.listMessageVO = reverse(
      response.data.listMessageVO.map(
        message =>
          new Feedback({
            ...message,
            messageAttachment: !isNil(message.messageAttachmentVO)
              ? new FeedbackMessageAttachment(message.messageAttachmentVO)
              : null
          })
      )
    );
    console.log(response.data);
    return response.data;
  },

  getMessageAttachment: async (dataflowId, messageId, dataProviderId) => {
    const response = await FeedbackRepository.getMessageAttachment(dataflowId, messageId, dataProviderId);
    return response.data;
  },

  markMessagesAsRead: async (dataflowId, messages) => await FeedbackRepository.markMessagesAsRead(dataflowId, messages)
};
