import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const parseConfigurationDTO = userConfigurationDTO => {
  const userConfiguration = {};

  const userDefaultConfiguration = {
    amPm24h: true,
    basemapLayer: 'Topographic',
    dateFormat: 'YYYY-MM-DD',
    listView: true,
    notificationSound: false,
    pinnedDataflows: [],
    pushNotifications: true,
    rowsPerPage: 10,
    showLogoutConfirmation: true,
    userImage: [],
    visualTheme: 'light'
  };
  if (isNil(userConfigurationDTO) || isEmpty(userConfigurationDTO)) {
    userConfiguration.basemapLayer = userDefaultConfiguration.basemapLayer;
    userConfiguration.dateFormat = userDefaultConfiguration.dateFormat;
    userConfiguration.notificationSound = userDefaultConfiguration.notificationSound;
    userConfiguration.pushNotifications = userDefaultConfiguration.pushNotifications;
    userConfiguration.showLogoutConfirmation = userDefaultConfiguration.showLogoutConfirmation;
    userConfiguration.rowsPerPage = userDefaultConfiguration.rowsPerPage;
    userConfiguration.visualTheme = userDefaultConfiguration.visualTheme;
    userConfiguration.userImage = userDefaultConfiguration.userImage;
    userConfiguration.amPm24h = userDefaultConfiguration.amPm24h;
    userConfiguration.listView = userDefaultConfiguration.listView;
    userConfiguration.pinnedDataflows = userDefaultConfiguration.pinnedDataflows;
  } else {
    userConfiguration.basemapLayer = !isNil(userConfigurationDTO.basemapLayer)
      ? userConfigurationDTO.basemapLayer[0]
      : userDefaultConfiguration.basemapLayer;

    userConfiguration.pinnedDataflows = !isNil(userConfigurationDTO.pinnedDataflows)
      ? userConfigurationDTO.pinnedDataflows
      : userDefaultConfiguration.pinnedDataflows;

    userConfiguration.dateFormat = !isNil(userConfigurationDTO.dateFormat[0])
      ? userConfigurationDTO.dateFormat[0]
      : userDefaultConfiguration.dateFormat;

    userConfiguration.notificationSound = isNil(userConfigurationDTO.notificationSound)
      ? userDefaultConfiguration.notificationSound
      : userConfigurationDTO.notificationSound[0] === 'false'
      ? (userConfiguration.notificationSound = false)
      : (userConfiguration.notificationSound = true);

    userConfiguration.pushNotifications = isNil(userConfigurationDTO.pushNotifications)
      ? userDefaultConfiguration.pushNotifications
      : userConfigurationDTO.pushNotifications[0] === 'false'
      ? (userConfiguration.pushNotifications = false)
      : (userConfiguration.pushNotifications = true);

    userConfiguration.showLogoutConfirmation = isNil(userConfigurationDTO.showLogoutConfirmation)
      ? userDefaultConfiguration.showLogoutConfirmation
      : userConfigurationDTO.showLogoutConfirmation[0] === 'false'
      ? (userConfiguration.showLogoutConfirmation = false)
      : (userConfiguration.showLogoutConfirmation = true);

    userConfiguration.rowsPerPage = !isNil(userConfigurationDTO.rowsPerPage)
      ? parseInt(userConfigurationDTO.rowsPerPage[0])
      : userDefaultConfiguration.rowsPerPage;

    userConfiguration.visualTheme = !isNil(userConfigurationDTO.visualTheme)
      ? userConfigurationDTO.visualTheme[0]
      : userDefaultConfiguration.visualTheme;

    userConfiguration.userImage = !isNil(userConfigurationDTO.userImage)
      ? userConfigurationDTO.userImage
      : userDefaultConfiguration.userImage;

    userConfiguration.amPm24h = isNil(userConfigurationDTO.amPm24h)
      ? userDefaultConfiguration.amPm24h
      : userConfigurationDTO.amPm24h[0] === 'false'
      ? (userConfiguration.amPm24h = false)
      : (userConfiguration.amPm24h = true);

    userConfiguration.listView = isNil(userConfigurationDTO.listView)
      ? userDefaultConfiguration.listView
      : userConfigurationDTO.listView[0] === 'false'
      ? (userConfiguration.listView = false)
      : (userConfiguration.listView = true);
  }
  return userConfiguration;
};

export const UserUtils = {
  parseConfigurationDTO
};
