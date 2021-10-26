import { NotificationConfig } from './config/NotificationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const NotificationRepository = {
  all: async () => await HTTPRequester.get({ url: getUrl(NotificationConfig.all) }),
  create: async (eventType, date, content) =>
    await HTTPRequester.post({
      url: getUrl(NotificationConfig.create),
      data: { eventType, insertDate: date, content }
    })
};
