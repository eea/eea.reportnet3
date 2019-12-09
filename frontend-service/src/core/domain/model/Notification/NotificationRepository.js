import { ApiNotificationRepository } from 'core/infrastructure/domain/model/Notification/ApiNotificationRepository';

export const NotificationRepository = {
  all: () => Promise.reject('[NotificationRepository#all] must be implemented'),
  parse: () => '[NotificationRepository#parse] must be implemented',
  removeAll: () => Promise.reject('[NotificationRepository#removeAll] must be implemented'),
  removeById: () => Promise.reject('[NotificationRepository#removeById] must be implemented'),
  readedAll: () => Promise.reject('[NotificationRepository#readedAll] must be implemented'),
  readedById: () => Promise.reject('[NotificationRepository#readedById] must be implemented')
};

export const notificationRepository = Object.assign({}, NotificationRepository, ApiNotificationRepository);
