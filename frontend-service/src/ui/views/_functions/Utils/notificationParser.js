import { isUndefined } from 'lodash';

import { getUrl } from 'core/infrastructure/CoreUtils';

const notificationParser = ({ type, content, message }, config, routes) => {
  const notificationDTO = {};
  config.notifications.forEach(notificationGeneralTypeConfig => {
    const notificationConfig = notificationGeneralTypeConfig.types.find(configType => configType.key === type);
    if (notificationConfig) {
      const { key, fixed, lifTime, navigateTo, downloadLinkSchema } = notificationGeneralTypeConfig;
      notificationDTO.message = message;
      notificationDTO.type = key;
      notificationDTO.fixed = fixed;
      notificationDTO.lifTime = lifTime;
      if (!isUndefined(navigateTo)) {
        const urlParameters = {};
        navigateTo.parameters.forEach(parameter => {
          urlParameters[parameter] = content[parameter];
        });
        notificationDTO.redirectionUrl = getUrl(routes[(notificationConfig.section, urlParameters)]);
      }
      if (!isUndefined(downloadLinkSchema)) {
        notificationDTO.downloadLink = content.downloadLink;
      }
    }
  });

  return notificationDTO;
};
export { notificationParser };
