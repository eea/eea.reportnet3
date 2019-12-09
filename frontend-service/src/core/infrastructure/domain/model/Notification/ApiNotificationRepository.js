import { apiNotification } from 'core/infrastructure/api/domain/model/Notification';
import { Notification } from 'core/domain/model/Notification/Notification';

const all = async () => {};
const removeById = async () => {};
const removeAll = async () => {};
const readedById = async () => {};
const readedAll = async () => {};
const parse = notificationDTO => {
  const notification = new Notification(
    notificationDTO.id,
    notificationDTO.message,
    notificationDTO.redirectionUrl,
    notificationDTO.downloadLink,
    notificationDTO.type,
    notificationDTO.fixed,
    notificationDTO.lifeTime,
    notificationDTO.readed
  );
  return notification;
};

export const ApiNotificationRepository = { all, parse, removeAll, removeById, readedAll, readedById };
