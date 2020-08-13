import camelCase from 'lodash/camelCase';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import kebabCase from 'lodash/kebabCase';

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
      notificationDTO.content = content;
      const contentKeys = Object.keys(content);

      if (!isUndefined(navigateTo) && !isNull(navigateTo)) {
        const urlParameters = {};
        navigateTo.parameters.forEach(parameter => {
          urlParameters[parameter] = content[parameter];
        });
        const section =
          type.toString() !== 'VALIDATION_FINISHED_EVENT'
            ? routes[navigateTo.section]
            : routes[getSectionValidationRedirectionUrl(content.type)];
        notificationDTO.redirectionUrl = getUrl(section, urlParameters, true);
        notificationDTO.message = TextUtils.parseText(notificationDTO.message, {
          navigateTo: notificationDTO.redirectionUrl
        });
      }
      contentKeys.forEach(key => {
        if (isUndefined(navigateTo) || !navigateTo.parameters.includes(key)) {
          const shortKey = camelCase(`short-${kebabCase(key)}`);
          content[shortKey] = TextUtils.ellipsis(content[key], generalConfig.notifications.STRING_LENGTH_MAX);
        }
      });
      notificationDTO.message = TextUtils.parseText(notificationDTO.message, content);
      notificationDTO.date = new Date();
    }
  });
  return new Notification(notificationDTO);
};

const getSectionValidationRedirectionUrl = sectionDTO => {
  if (!isNil(sectionDTO)) {
    if (sectionDTO === 'REPORTING') {
      return 'DATASET';
    } else if (sectionDTO === 'DESIGN') {
      return 'DATASET_SCHEMA';
    } else {
      return 'EU_DATASET';
    }
  }
};

export const ApiNotificationRepository = { all, parse, removeAll, removeById, readAll, readById };
