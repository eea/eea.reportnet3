import avatarImages from './avatarImages.json';
import countryByGroup from './countriesByGroup.json';
import dataflowStatus from './dataflowStatus.json';
import dataflowType from './dataflowType.json';
import datasetRunningStatus from './datasetRunningStatus.json';
import datasetStatus from './datasetStatus.json';
import datasetType from './datasetType.json';
import exportTypeCode from './exportType.code.json';
import fieldType from './fieldType.json';
import footer from './footer.config.json';
import jobRunningStatus from './jobRunningStatus.json';
import jobType from './jobType.json';
import importTypeCode from './importType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications.json';
import permissions from './permissions.json';
import primeIcons from './prime.icons.json';
import providerGroup from './providerGroup.json';
import providerGroupDev from './providerGroupDev.json';
import providerGroupTest from './providerGroupTest.json';
import storage from './storage.config.json';
import systemNotifications from './systemNotifications.json';
import theme from './theme.config.json';
import validations from './validation.config.json';

export const config = {
  MB_SIZE: 1024 * 1024,
  MAX_FILE_EXTENSION_LENGTH: 10,
  MAX_FILE_SIZE: 100000000,
  MAX_INTEGRATION_NAME_LENGTH: 50,
  INPUT_MAX_LENGTH: 255,
  DATAFLOWS_PER_PAGE: 100,
  DESCRIPTION_MAX_LENGTH: 10000,
  SYSTEM_NOTIFICATION_MAX_LENGTH: 300,
  GEOGRAPHICAL_LAT_COORD: { min: -90, max: 90 },
  GEOGRAPHICAL_LONG_COORD: { min: -180, max: 180 },
  GEOGRAPHICAL_LAT_COORD_3035: { min: 32.88, max: 84.17 },
  GEOGRAPHICAL_LONG_COORD_3035: { min: -16.1, max: 40.18 },
  SQL_SENTENCE_LOW_COST: 20,
  SQL_SENTENCE_HIGH_COST: 50,
  MAX_ATTACHMENT_SIZE: 20 * 1024 * 1024,
  MAX_ATTACHMENT_SIZE_FEEDBACK: 100 * 1024 * 1024,
  IMPORT_FILE_DELIMITER: ',',
  IMPORT_ERROR_LIFETIME: 120000,

  avatars: avatarImages,
  countriesByGroup: countryByGroup,
  dataflowStatus: dataflowStatus,
  dataflowType: dataflowType,
  datasetRunningStatus: datasetRunningStatus,
  datasetType: datasetType,
  exportTypes: exportTypeCode,
  fieldType: fieldType,
  footer: footer,
  icons: primeIcons,
  importTypes: importTypeCode,
  jobRunningStatus: jobRunningStatus,
  jobType: jobType,
  languages: languageCode,
  notifications: notifications,
  systemNotifications: systemNotifications,
  permissions: permissions,
  providerGroup: providerGroup,
  providerGroupDev: providerGroupDev,
  providerGroupTest: providerGroupTest,
  storage: storage,
  datasetStatus: datasetStatus,
  theme: theme,
  validations: validations
};
