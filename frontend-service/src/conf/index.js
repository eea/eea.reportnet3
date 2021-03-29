import avatarImages from './avatarImages.json';
import countryByGroup from './countriesByGroup';
import dataflowPermissions from './dataflowPermissions';
import exportTypeCode from './exportType.code.json';
import footer from './footer.config.json';
import languageCode from './language.code.json';
import notifications from './notifications';
import permissions from './permissions';
import primeIcons from './prime.icons';
import publicFrontpage from './publicFrontpage.json';
import storage from './storage.config.json';
import theme from './theme.config.json';
import validations from './validation.config.json';
import webforms from './webforms.config.json';

const config = {};
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { images } = avatarImages;
const { languages } = languageCode;

config.MAX_FILE_EXTENSION_LENGTH = 10;
config.MAX_FILE_SIZE = 10;
config.MAX_INTEGRATION_NAME_LENGTH = 50;

config.MAX_ATTACHMENT_SIZE = 20 * 1000 * 1024;

config.avatars = images;
config.countriesByGroup = countryByGroup;
config.dataflowPermissions = dataflowPermissions;
config.exportTypes = exportTypes;
config.footer = footer;
config.icons = icons;
config.languages = languages;
config.notifications = notifications;
config.permissions = permissions;
config.publicFrontpage = publicFrontpage;
config.validations = validations;
config.storage = storage;
config.theme = theme;
config.webforms = webforms;

export { config };
