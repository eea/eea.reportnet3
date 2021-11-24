import avatarImages from './avatarImages.json';
import countryByGroup from './countriesByGroup.json';
import dataflowStatus from './dataflowStatus.json';
import dataflowType from './dataflowType.json';
import datasetType from './datasetType.json';
import exportTypeCode from './exportType.code.json';
import fieldType from './fieldType.json';
import footer from './footer.config.json';
import importTypeCode from './importType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications.json';
import systemNotifications from './systemNotifications.json';
import permissions from './permissions.json';
import primeIcons from './prime.icons.json';
import storage from './storage.config.json';
import datasetStatus from './datasetStatus.json';
import theme from './theme.config.json';
import validations from './validation.config.json';
import webforms from './webforms.config.json';

const config = {};
config.MB_SIZE = 1024 * 1024;
config.MAX_FILE_EXTENSION_LENGTH = 10;
config.MAX_FILE_SIZE = 100000000;
config.MAX_INTEGRATION_NAME_LENGTH = 50;
config.INPUT_MAX_LENGTH = 255;
config.DESCRIPTION_MAX_LENGTH = 10000;
config.SYSTEM_NOTIFICATION_MAX_LENGTH = 300;
config.GEOGRAPHICAL_LAT_COORD = { min: -90, max: 90 };
config.GEOGRAPHICAL_LONG_COORD = { min: -180, max: 180 };
config.METRICAL_X_COORD = { min: 1896628.62, max: 9512027.5 };
config.METRICAL_Y_COORD = { min: 1507846.05, max: 4851204.83 };

config.MAX_ATTACHMENT_SIZE = 20 * config.MB_SIZE;

config.IMPORT_FILE_DELIMITER = ',';

config.avatars = avatarImages;
config.countriesByGroup = countryByGroup;
config.dataflowStatus = dataflowStatus;
config.dataflowType = dataflowType;
config.datasetType = datasetType;
config.exportTypes = exportTypeCode;
config.fieldType = fieldType;
config.footer = footer;
config.icons = primeIcons;
config.importTypes = importTypeCode;
config.languages = languageCode;
config.notifications = notifications;
config.systemNotifications = systemNotifications;
config.permissions = permissions;
config.storage = storage;
config.datasetStatus = datasetStatus;
config.theme = theme;
config.validations = validations;
config.webforms = webforms;

export { config };
