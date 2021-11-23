import { SystemNotificationConfig } from './config/SystemNotificationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const SystemNotificationRepository = {
  all: async () => await HTTPRequester.get({ url: getUrl(SystemNotificationConfig.all) }),

  checkEnabled: async () => await HTTPRequester.get({ url: getUrl(SystemNotificationConfig.checkEnabled) }),

  create: async (message, level, enabled) =>
    await HTTPRequester.post({
      url: getUrl(SystemNotificationConfig.create),
      data: { message, level, enabled }
    }),

  delete: async systemNotificationId =>
    await HTTPRequester.delete({ url: getUrl(SystemNotificationConfig.delete, { systemNotificationId }) }),

  update: async (id, message, level, enabled) =>
    await HTTPRequester.update({
      url: getUrl(SystemNotificationConfig.update),
      data: { id, message, level, enabled }
    })
};
