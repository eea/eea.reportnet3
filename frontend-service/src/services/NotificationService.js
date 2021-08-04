import camelCase from 'lodash/camelCase';
import capitalize from 'lodash/capitalize';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import kebabCase from 'lodash/kebabCase';

import { config as generalConfig } from 'conf';

import { Notification } from 'entities/Notification';

import { TextUtils } from 'repositories/_utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

const all = async () => {};
const removeById = async () => {};
const removeAll = async () => {};
const readById = async () => {};
const readAll = async () => {};
const parse = ({ config, content = {}, message, onClick, routes, type }) => {
  if (type === 'UPDATED_DATASET_STATUS') {
    getUpdatedDatasetStatusNotificationContent(content);
  }

  const notificationDTO = {};
  config.forEach(notificationGeneralTypeConfig => {
    const notificationTypeConfig = notificationGeneralTypeConfig.types.find(configType => configType.key === type);
    if (notificationTypeConfig) {
      const { key, fixed, lifeTime } = notificationGeneralTypeConfig;
      const { fixed: typeFixed, lifeTime: typeLifeTime, navigateTo } = notificationTypeConfig;
      notificationDTO.content = content;
      notificationDTO.fixed = !isUndefined(typeFixed) ? typeFixed : fixed;
      notificationDTO.key = type;
      notificationDTO.lifeTime = typeLifeTime || lifeTime;
      notificationDTO.message = message;
      notificationDTO.onClick = onClick;
      notificationDTO.type = key;
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

        if (!isNil(navigateTo.hasQueryString) && navigateTo.hasQueryString) {
          notificationDTO.redirectionUrl = `${notificationDTO.redirectionUrl}${window.location.search}`;
        }
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
const parseHidden = ({ type, content = {}, config }) => {
  const notificationDTO = {};
  const notificationTypeConfig = config.filter(notificationGeneralTypeConfig => notificationGeneralTypeConfig === type);

  if (notificationTypeConfig) {
    notificationDTO.key = type;
    notificationDTO.content = content;
  }

  return new Notification(notificationDTO);
};

const getUpdatedDatasetStatusNotificationContent = content =>
  (content.datasetStatus = capitalize(content.datasetStatus.split('_').join(' ')));

const getSectionValidationRedirectionUrl = sectionDTO => {
  if (!isNil(sectionDTO)) {
    if (sectionDTO === 'REPORTING') {
      return 'DATASET';
    }

    if (sectionDTO === 'DESIGN') {
      return 'DATASET_SCHEMA';
    }

    return 'EU_DATASET';
  }
};

export const NotificationService = { all, parse, parseHidden, removeAll, removeById, readAll, readById };
