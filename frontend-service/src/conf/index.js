import avatarImages from './avatarImages.json';
import countryCode from './countries.code.json';
import exportTypeCode from './exportType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications';
import permissions from './permissions';
import primeIcons from './prime.icons';
import validations from './validation.config.json';
import publicFrontpage from './publicFrontpage.json';
import footer from './footer.config.json';

const config = {};
const { images } = avatarImages;
const { countries } = countryCode;
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { languages } = languageCode;

config.avatars = images;
config.countries = countries;
config.exportTypes = exportTypes;
config.footer = footer;
config.icons = icons;
config.languages = languages;
config.MAX_FILE_EXTENSION_LENGTH = 10;
config.MAX_FILE_SIZE = 100000000;
config.notifications = notifications;
config.permissions = permissions;
config.publicFrontpage = publicFrontpage;
config.validations = validations;

export { config };
