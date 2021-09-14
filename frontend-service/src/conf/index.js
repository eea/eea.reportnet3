import avatarImages from './avatarImages.json';
import countryByGroup from './countriesByGroup.json';
import dataflowStatus from './dataflowStatus.json';
import dataflowType from './dataflowType.json';
import exportTypeCode from './exportType.code.json';
import footer from './footer.config.json';
import importTypeCode from './importType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications.json';
import permissions from './permissions.json';
import primeIcons from './prime.icons.json';
import publicFrontpage from './publicFrontpage.json';
import storage from './storage.config.json';
import datasetStatus from './datasetStatus.json';
import theme from './theme.config.json';
import validations from './validation.config.json';
import webforms from './webforms.config.json';

const config = {};
config.MB_SIZE = 1024 * 1024;
config.COMPANY_CODE_KEYWORD = '{%R3_COMPANY_CODE%}';
config.COUNTRY_CODE_KEYWORD = '{%R3_COUNTRY_CODE%}';
config.ORGANIZATION_CODE_KEYWORD = '{%R3_ORGANIZATION_CODE%}';
config.MAX_FILE_EXTENSION_LENGTH = 10;
config.MAX_FILE_SIZE = 100000000;
config.MAX_INTEGRATION_NAME_LENGTH = 50;
config.INPUT_MAX_LENGTH = 255;
config.DESCRIPTION_MAX_LENGTH = 10000;

config.MAX_ATTACHMENT_SIZE = 20 * config.MB_SIZE;

config.IMPORT_FILE_DELIMITER = ',';

config.avatars = avatarImages;
config.countriesByGroup = countryByGroup;
config.dataflowStatus = dataflowStatus;
config.dataflowType = dataflowType;
config.exportTypes = exportTypeCode;
config.footer = footer;
config.icons = primeIcons;
config.importTypes = importTypeCode;
config.languages = languageCode;
config.notifications = notifications;
config.permissions = permissions;
config.publicFrontpage = publicFrontpage;
config.storage = storage;
config.datasetStatus = datasetStatus;
config.theme = theme;
config.validations = validations;
config.webforms = webforms;

export { config };
