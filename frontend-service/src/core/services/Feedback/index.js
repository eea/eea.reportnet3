import { GetAll } from './GetAll';
import { GetAllUnread } from './GetAllUnread';
import { feedbackRepository } from 'core/domain/model/Feedback/FeedbackRepository';

export const FeedbackService = {
  all: GetAll({ feedbackRepository }),
  allUnread: GetAllUnread({ feedbackRepository })
};
