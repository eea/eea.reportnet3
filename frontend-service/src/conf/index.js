import avatarImages from './avatarImages.json';
import countryCode from './countries.code.json';
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

const config = {};
const { countries } = countryCode;
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { images } = avatarImages;
const { languages } = languageCode;

config.MAX_FILE_EXTENSION_LENGTH = 10;
config.MAX_FILE_SIZE = 100000000;
config.MAX_INTEGRATION_NAME_LENGTH = 50;

config.avatars = images;
config.countries = countries;
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

export { config };
