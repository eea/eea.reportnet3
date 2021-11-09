import { SystemNotificationConfig } from './config/SystemNotificationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const SystemNotificationRepository = {
  all: async (pageNum, pageSize) =>
    await HTTPRequester.get({ url: getUrl(SystemNotificationConfig.all, { pageNum, pageSize }) }),

  create: async (eventType, date, content) =>
    await HTTPRequester.post({
      url: getUrl(SystemNotificationConfig.create),
      data: { eventType, insertDate: date, content }
    }),

  deleteById: async id => await HTTPRequester.delete({ url: getUrl(SystemNotificationConfig.deleteById, { id }) })
};
