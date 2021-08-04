import { Create } from './Create';
import { GetAll } from './GetAll';
import { UpdateRead } from './UpdateRead';

import { feedbackRepository } from 'entities/Feedback/FeedbackRepository';

export const FeedbackService = {
  create: Create({ feedbackRepository }),
  loadMessages: GetAll({ feedbackRepository }),
  markAsRead: UpdateRead({ feedbackRepository })
};
