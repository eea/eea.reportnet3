import { SystemNotification } from 'entities/SystemNotification';

import { SystemNotificationRepository } from 'repositories/SystemNotificationRepository';

export const SystemNotificationService = {
  all: async () => {
    // const systemNotificationsDTO = await SystemNotificationRepository.all();
    let systemNotificationsDTO = {};
    systemNotificationsDTO.data = [
      { id: 1, message: 'System temporaly shutdown', level: 'ERROR', enabled: true },
      { id: 2, message: 'System temporaly shutdown 2', level: 'INFO', enabled: true },
      { id: 3, message: 'System temporaly shutdown 3', level: 'ERROR', enabled: true }
    ];

    return systemNotificationsDTO?.data?.map(systemNotificationDTO => {
      const { id, message, enabled, level } = systemNotificationDTO;
      return new SystemNotification({ id, message, enabled, level, lifeTime: 5000 });
    });
  },

  create: async ({ message, level, enabled }) => await SystemNotificationRepository.create(message, level, enabled),

  deleteById: async id => await SystemNotificationRepository.deleteById(id)

  // removeAll: async () => {},

  // readById: async () => {},

  // readAll: async () => {},

  // parse: ({ message, onClick, routes, type }) => {
  //   content = merge(content, content.customContent);
  //   delete content.customContent;

  //   if (type === 'UPDATED_DATASET_STATUS') {
  //     content.datasetStatus = capitalize(content.datasetStatus.split('_').join(' '));
  //   }

  //   const notificationDTO = {};
  //   config.forEach(notificationGeneralTypeConfig => {
  //     const notificationTypeConfig = notificationGeneralTypeConfig.types.find(configType => configType.key === type);
  //     if (notificationTypeConfig) {
  //       const { key, fixed, lifeTime } = notificationGeneralTypeConfig;
  //       const { fixed: typeFixed, lifeTime: typeLifeTime, navigateTo } = notificationTypeConfig;
  //       notificationDTO.content = content;
  //       notificationDTO.fixed = !isUndefined(typeFixed) ? typeFixed : fixed;
  //       notificationDTO.key = type;
  //       notificationDTO.lifeTime = typeLifeTime || lifeTime;
  //       notificationDTO.message = message;
  //       notificationDTO.onClick = onClick;
  //       notificationDTO.type = key;
  //       const contentKeys = Object.keys(content);

  //       if (!isUndefined(navigateTo) && !isNull(navigateTo)) {
  //         const urlParameters = {};

  //         navigateTo.parameters.forEach(parameter => {
  //           urlParameters[parameter] = content[parameter];
  //         });

  //         const section =
  //           type.toString() !== 'VALIDATION_FINISHED_EVENT' && type.toString() !== 'VALIDATE_DATA_INIT'
  //             ? routes[navigateTo.section]
  //             : routes[
  //                 NotificationUtils.getSectionValidationRedirectionUrl(
  //                   !isNil(content.type) ? content.type : !isNil(content.typeStatus) ? content.typeStatus : 'DESIGN'
  //                 )
  //               ];

  //         notificationDTO.redirectionUrl = getUrl(section, urlParameters, true);

  //         if (!isNil(navigateTo.hasQueryString) && navigateTo.hasQueryString) {
  //           notificationDTO.redirectionUrl = `${notificationDTO.redirectionUrl}${window.location.search}`;
  //         }

  //         notificationDTO.message = TextUtils.parseText(notificationDTO.message, {
  //           navigateTo: notificationDTO.redirectionUrl
  //         });
  //       }

  //       contentKeys.forEach(key => {
  //         if (isUndefined(navigateTo) || !navigateTo.parameters.includes(key)) {
  //           const shortKey = camelCase(`short-${kebabCase(key)}`);
  //           content[shortKey] = TextUtils.ellipsis(content[key], generalConfig.notifications.STRING_LENGTH_MAX);
  //         }
  //       });

  //       notificationDTO.message = TextUtils.parseText(notificationDTO.message, content);
  //       notificationDTO.date = new Date(date);
  //     }
  //   });
  //   return new Notification(notificationDTO);
  // },
};
