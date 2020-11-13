import { ApiFeedbackRepository } from 'core/infrastructure/domain/model/Feedback/ApiFeedbackRepository';

export const FeedbackRepository = {
  create: () => Promise.reject('[FeedbackRepository#create] must be implemented'),
  loadMessages: () => Promise.reject('[FeedbackRepository#loadMessages] must be implemented'),
  markAsRead: () => Promise.reject('[FeedbackRepository#markAsRead] must be implemented')
};

export const feedbackRepository = Object.assign({}, FeedbackRepository, ApiFeedbackRepository);
