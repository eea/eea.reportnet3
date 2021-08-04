import reverse from 'lodash/reverse';

import { feedbackRepository } from 'repositories/FeedbackRepository';

import { Feedback } from 'entities/Feedback';

const create = async (dataflowId, message, providerId) =>
  await feedbackRepository.create(dataflowId, message, providerId);

const loadMessages = async (dataflowId, page, dataProviderId) => {
  const response = await feedbackRepository.loadMessages(dataflowId, page, dataProviderId);
  response.data = reverse(response.data.map(message => new Feedback({ ...message })));
  return response;
};

const markAsRead = async (dataflowId, messages) => await feedbackRepository.markAsRead(dataflowId, messages);

export const FeedbackService = { create, loadMessages, markAsRead };
