import { apiNotification } from 'core/infrastructure/api/domain/model/Notification';
import { Notification } from 'core/domain/model/Notification/Notification';
import { isUndefined } from 'lodash';
import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';

const all = async () => {};
const removeById = async () => {};
const removeAll = async () => {};
const readById = async () => {};
const readAll = async () => {};
const parse = ({ type, content, message, config, routes }) => {
  const notificationDTO = {};
  config.forEach(notificationGeneralTypeConfig => {
    const notificationTypeConfig = notificationGeneralTypeConfig.types.find(configType => configType.key === type);
    if (notificationTypeConfig) {
      const { key, fixed, lifTime, navigateTo, downloadLinkSchema } = notificationGeneralTypeConfig;
      notificationDTO.message = message;
      notificationDTO.type = key;
      notificationDTO.fixed = notificationTypeConfig.fixed || fixed;
      notificationDTO.lifTime = notificationTypeConfig.lifTime || lifTime;
      if (!isUndefined(navigateTo)) {
        const urlParameters = {};
        navigateTo.parameters.forEach(parameter => {
          urlParameters[parameter] = content[parameter];
        });
        notificationDTO.redirectionUrl = getUrl(routes[(notificationTypeConfig.section, urlParameters)]);
        notificationDTO.message = TextUtils.parseText(notificationDTO.message, {
          navigateTo: notificationDTO.redirectionUrl
        });
      }
      if (!isUndefined(downloadLinkSchema)) {
        notificationDTO.downloadLink = content.downloadLink;
        notificationDTO.message = TextUtils.parseText(notificationDTO.message, {
          downloadLink: notificationDTO.downloadLink
        });
      }
      console.log('content', content);
      notificationDTO.message = TextUtils.parseText(notificationDTO.message, content);
    }
  });
  return new Notification(
    notificationDTO.id,
    notificationDTO.message,
    notificationDTO.redirectionUrl,
    notificationDTO.downloadLink,
    notificationDTO.type,
    notificationDTO.fixed,
    notificationDTO.lifeTime,
    notificationDTO.readed
  );
};

export const ApiNotificationRepository = { all, parse, removeAll, removeById, readAll, readById };
