import avatarImages from './avatarImages.json';
import countryCode from './countries.code.json';
import exportTypeCode from './exportType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications';
import permissions from './permissions';
import primeIcons from './prime.icons';
import publicFrontpage from './publicFrontpage.json';
import storage from './storage.config.json';
import validations from './validation.config.json';

const config = {};
const { countries } = countryCode;
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { images } = avatarImages;
const { languages } = languageCode;

config.avatars = images;
config.countries = countries;
config.exportTypes = exportTypes;
config.icons = icons;
config.languages = languages;
config.MAX_FILE_SIZE = 100000000;
config.notifications = notifications;
config.permissions = permissions;
config.publicFrontpage = publicFrontpage;
config.storage = storage;
config.validations = validations;

export { config };
