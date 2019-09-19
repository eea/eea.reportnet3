import primeIcons from './prime.icons';
import webConfig from './web.config';
import countryCode from './countries.code.json';
import languageCode from './language.code.json';
import exportTypeCode from './exportType.code.json';
import permissions from './permissions';

const { icons } = primeIcons;
const config = { ...webConfig };
const { countries } = countryCode;
const { languages } = languageCode;
const { exportTypes } = exportTypeCode;
config.MAX_FILE_SIZE = 100000000;
config.icons = icons;
config.countries = countries;
config.languages = languages;
config.exportTypes = exportTypes;
config.permissions = permissions;

export { config };
