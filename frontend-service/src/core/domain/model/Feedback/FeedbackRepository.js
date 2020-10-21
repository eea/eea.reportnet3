import { ApiFeedbackRepository } from 'core/infrastructure/domain/model/Feedback/ApiFeedbackRepository';

export const FeedbackRepository = {
  all: () => Promise.reject('[FeedbackRepository#all] must be implemented')
};

export const feedbackRepository = Object.assign({}, FeedbackRepository, ApiFeedbackRepository);
