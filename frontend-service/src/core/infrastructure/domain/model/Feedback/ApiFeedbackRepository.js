// import { config } from 'conf';
import reverse from 'lodash/reverse';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

import { Feedback } from 'core/domain/model/Feedback/Feedback';

const create = async (dataflowId, message, providerId) => {
  const messageCreated = await apiFeedback.create(dataflowId, message, providerId);
  return messageCreated;
};

const loadMessages = async (dataflowId, page, dataProviderId) => {
  const response = await apiFeedback.loadMessages(dataflowId, page, dataProviderId);
  const messagesDTO = response.map(message => new Feedback({ ...message }));
  console.log({ messagesDTO });
  return reverse(messagesDTO);
};

const loadMessagesByFlag = async (dataflowId, page, read, dataProviderId) => {
  const response = await apiFeedback.loadMessagesByFlag(dataflowId, page, read, dataProviderId);
  const messagesDTO = response.map(message => new Feedback({ ...message }));
  console.log({ messagesDTO });
  return reverse(messagesDTO);
};

const markAsRead = async (dataflowId, messages) => {
  const updated = await apiFeedback.markAsRead(dataflowId, messages);
  return updated;
};

export const ApiFeedbackRepository = {
  create,
  loadMessages,
  loadMessagesByFlag,
  markAsRead
};
