import { camelCase, isNull, isUndefined, kebabCase } from 'lodash';

import { config as generalConfig } from 'conf';

import { Notification } from 'core/domain/model/Notification/Notification';

import { getUrl, TextUtils } from 'core/infrastructure/CoreUtils';

const all = async () => {};
const removeById = async () => {};
const removeAll = async () => {};
const readById = async () => {};
const readAll = async () => {};
const parse = ({ type, content = {}, message, config, routes }) => {
  const notificationDTO = {};
  config.forEach(notificationGeneralTypeConfig => {
    const notificationTypeConfig = notificationGeneralTypeConfig.types.find(configType => configType.key === type);
    if (notificationTypeConfig) {
      const { key, fixed, lifeTime } = notificationGeneralTypeConfig;
      const { fixed: typeFixed, lifeTime: typeLifeTime, navigateTo } = notificationTypeConfig;
      notificationDTO.message = message;
      notificationDTO.type = key;
      notificationDTO.fixed = !isUndefined(typeFixed) ? typeFixed : fixed;
      notificationDTO.lifeTime = typeLifeTime || lifeTime;
      notificationDTO.key = type;
      const contentKeys = Object.keys(content);

      if (!isUndefined(navigateTo) && !isNull(navigateTo)) {
        const urlParameters = {};
        navigateTo.parameters.forEach(parameter => {
          urlParameters[parameter] = content[parameter];
        });
        notificationDTO.redirectionUrl = getUrl(routes[navigateTo.section], urlParameters, true);
        notificationDTO.message = TextUtils.parseText(notificationDTO.message, {
          navigateTo: notificationDTO.redirectionUrl
        });
      }
      contentKeys.forEach(key => {
        if (isUndefined(navigateTo)) {
          const shortKey = camelCase(`short-${kebabCase(key)}`);
          content[shortKey] = TextUtils.ellipsis(content[key], generalConfig.notifications.STRING_LENGTH_MAX);
        } else {
          if (!navigateTo.parameters.includes(key)) {
            const shortKey = camelCase(`short-${kebabCase(key)}`);
            content[shortKey] = TextUtils.ellipsis(content[key], generalConfig.notifications.STRING_LENGTH_MAX);
          }
        }
      });
      notificationDTO.message = TextUtils.parseText(notificationDTO.message, content);
    }
  });
  return new Notification(
    notificationDTO.id,
    notificationDTO.key,
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
