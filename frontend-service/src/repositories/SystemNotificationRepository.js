import { SystemNotificationConfig } from './config/SystemNotificationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const SystemNotificationRepository = {
  all: async () => await HTTPRequester.get({ url: getUrl(SystemNotificationConfig.all) }),

  create: async (message, level, enabled) =>
    await HTTPRequester.post({
      url: getUrl(SystemNotificationConfig.create),
      data: { message, level, enabled }
    }),

  deleteById: async id => await HTTPRequester.delete({ url: getUrl(SystemNotificationConfig.deleteById, { id }) })
};
