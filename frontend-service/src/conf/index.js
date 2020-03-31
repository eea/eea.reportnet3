import countryCode from './countries.code.json';
import exportTypeCode from './exportType.code.json';
import languageCode from './language.code.json';
import notifications from './notifications';
import permissions from './permissions';
import primeIcons from './prime.icons';
import validations from './validation.config.json';

const config = {};
const { countries } = countryCode;
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { languages } = languageCode;

config.countries = countries;
config.exportTypes = exportTypes;
config.icons = icons;
config.languages = languages;
config.MAX_FILE_SIZE = 100000000;
config.notifications = notifications;
config.permissions = permissions;
config.validations = validations;

export { config };
