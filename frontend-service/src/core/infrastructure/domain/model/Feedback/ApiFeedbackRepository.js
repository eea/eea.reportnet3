// import { config } from 'conf';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

import { Feedback } from 'core/domain/model/Feedback/Feedback';

const create = async (dataflowId, message, providerId) => {
  const created = await apiFeedback.create(dataflowId, message, providerId);
  return created;
};

const loadMessages = async (dataProviderId, page) => {
  const response = await apiFeedback.loadMessages(dataProviderId, page);
  const messagesDTO = response.map(message => new Feedback({ ...message }));
  console.log({ messagesDTO });
  return messagesDTO;
};

const loadMessagesByFlag = async () => {
  return await apiFeedback.loadMessagesByFlag();
};

const markAsRead = async (dataflowId, messageIds, read) => {
  const messages = messageIds.map(messageId => {
    return { id: messageId, read };
  });
  const updated = await apiFeedback.markAsRead(dataflowId, messages);
  return updated;
};

export const ApiFeedbackRepository = {
  create,
  loadMessages,
  loadMessagesByFlag,
  markAsRead
};
