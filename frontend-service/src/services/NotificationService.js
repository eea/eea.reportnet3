import camelCase from 'lodash/camelCase';
import capitalize from 'lodash/capitalize';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import kebabCase from 'lodash/kebabCase';
import merge from 'lodash/merge';

import { config as generalConfig } from 'conf';

import { Notification } from 'entities/Notification';

import { NotificationRepository } from 'repositories/NotificationRepository';

import { NotificationUtils } from 'services/_utils/NotificationUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const NotificationService = {
  all: async ({ pageNum, pageSize }) => {
    const notificationsDTO = await NotificationRepository.all(pageNum, pageSize);
    const notifications = {};

    notifications.userNotifications = notificationsDTO?.data?.userNotifications?.map(notificationDTO => {
      const { content, insertDate, eventType } = notificationDTO;
      return new Notification({ content, date: insertDate, type: eventType });
    });

    notifications.totalRecords = notificationsDTO.data.totalRecords;
    return notifications;
  },

  create: async (type, date, content) => await NotificationRepository.create(type, date, content),

  parse: ({ config, content = {}, date, message, onClick, routes, type }) => {
    content = merge(content, content.customContent);
    delete content.customContent;

    if (type === 'UPDATED_DATASET_STATUS') {
      content.datasetStatus = capitalize(content.datasetStatus.split('_').join(' '));
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
            type.toString() !== 'VALIDATION_FINISHED_EVENT' && type.toString() !== 'VALIDATE_DATA_INIT'
              ? routes[navigateTo.section]
              : routes[
                  NotificationUtils.getSectionValidationRedirectionUrl(
                    !isNil(content.type) ? content.type : !isNil(content.typeStatus) ? content.typeStatus : 'DESIGN'
                  )
                ];

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
        notificationDTO.date = new Date(date);
      }
    });
    return new Notification(notificationDTO);
  },

  parseHidden: ({ type, content = {}, config }) => {
    const notificationDTO = {};
    const notificationTypeConfig = config.filter(
      notificationGeneralTypeConfig => notificationGeneralTypeConfig === type
    );

    if (notificationTypeConfig) {
      notificationDTO.key = type;
      notificationDTO.content = content;
    }

    return new Notification(notificationDTO);
  }
};
