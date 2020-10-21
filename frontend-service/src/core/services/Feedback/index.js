import { GetAll } from './GetAll';

export const FeedbackService = {
  all: GetAll({ feedbackRepository })
};
