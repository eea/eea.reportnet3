import reverse from 'lodash/reverse';

import { FeedbackRepository } from 'repositories/FeedbackRepository';

import { Feedback } from 'entities/Feedback';

export const FeedbackService = {
  createMessage: async (dataflowId, message, providerId) =>
    await FeedbackRepository.createMessage(dataflowId, message, providerId),

  getAllMessages: async (dataflowId, page, dataProviderId) => {
    const response = await FeedbackRepository.getAllMessages(dataflowId, page, dataProviderId);
    response.data = reverse(response.data.map(message => new Feedback({ ...message })));
    return response;
  },

  markMessagesAsRead: async (dataflowId, messages) => await FeedbackRepository.markMessagesAsRead(dataflowId, messages)
};
