import { ApiNotificationRepository } from 'core/infrastructure/domain/model/Notification/ApiNotificationRepository';

export const NotificationRepository = {
  all: () => Promise.reject('[NotificationRepository#all] must be implemented'),
  parse: () => '[NotificationRepository#parse] must be implemented',
  removeAll: () => Promise.reject('[NotificationRepository#removeAll] must be implemented'),
  removeById: () => Promise.reject('[NotificationRepository#removeById] must be implemented'),
  readAll: () => Promise.reject('[NotificationRepository#readAll] must be implemented'),
  ReadById: () => Promise.reject('[NotificationRepository#ReadById] must be implemented')
};

export const notificationRepository = Object.assign({}, NotificationRepository, ApiNotificationRepository);
