// import { config } from 'conf';

import { apiFeedback } from 'core/infrastructure/api/domain/model/Feedback';

// import { Feedback } from 'core/domain/model/Feedback/Feedback';

const all = async () => {
  return await apiFeedback.all();
};

const allUnread = async () => {
  return await apiFeedback.allUnread();
};

export const ApiFeedbackRepository = {
  all,
  allUnread
};
