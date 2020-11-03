import { GetAll } from './GetAll';
import { Create } from './Create';
import { GetAllByFlag } from './GetAllByFlag';
import { UpdateRead } from './UpdateRead';
import { feedbackRepository } from 'core/domain/model/Feedback/FeedbackRepository';

export const FeedbackService = {
  create: Create({ feedbackRepository }),
  loadMessages: GetAll({ feedbackRepository }),
  loadMessagesByFlag: GetAllByFlag({ feedbackRepository }),
  markAsRead: UpdateRead({ feedbackRepository })
};
