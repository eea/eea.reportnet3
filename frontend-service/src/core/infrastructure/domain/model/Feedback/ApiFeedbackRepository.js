import reverse from 'lodash/reverse';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

import { Feedback } from 'core/domain/model/Feedback/Feedback';

const create = async (dataflowId, message, providerId) => await apiFeedback.create(dataflowId, message, providerId);

const loadMessages = async (dataflowId, page, dataProviderId) => {
  const response = await apiFeedback.loadMessages(dataflowId, page, dataProviderId);
  response.data = reverse(response.data.map(message => new Feedback({ ...message })));
  return response;
};

const loadMessagesByFlag = async (dataflowId, page, read, dataProviderId) => {
  const response = await apiFeedback.loadMessagesByFlag(dataflowId, page, read, dataProviderId);
  const messagesDTO = response.map(message => new Feedback({ ...message }));
  return reverse(messagesDTO);
};

const markAsRead = async (dataflowId, messages) => await apiFeedback.markAsRead(dataflowId, messages);

export const ApiFeedbackRepository = { create, loadMessages, loadMessagesByFlag, markAsRead };
