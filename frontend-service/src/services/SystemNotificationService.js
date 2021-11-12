import { SystemNotification } from 'entities/SystemNotification';

import { SystemNotificationRepository } from 'repositories/SystemNotificationRepository';

export const SystemNotificationService = {
  all: async () => {
    const systemNotificationsDTO = await SystemNotificationRepository.all();

    console.log({ systemNotificationsDTO });
    return systemNotificationsDTO?.data?.map(systemNotificationDTO => {
      const { id, message, enabled, level } = systemNotificationDTO;
      return new SystemNotification({ id, message, enabled, level: level || 'INFO', lifeTime: 5000 });
    });
  },

  create: async ({ message, level, enabled }) => await SystemNotificationRepository.create(message, level, enabled),

  delete: async id => await SystemNotificationRepository.delete(id),

  update: async ({ id, message, level, enabled }) =>
    await SystemNotificationRepository.update(id, message, level, enabled)
};
