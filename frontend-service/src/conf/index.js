import countryCode from './countries.code.json';
import dataflowStatusTypes from './dataflowStatus.json';
import exportTypeCode from './exportType.code.json';
import languageCode from './language.code.json';
import permissions from './permissions';
import primeIcons from './prime.icons';

const config = {};
const { countries } = countryCode;
const { dataflowStatus } = dataflowStatusTypes;
const { exportTypes } = exportTypeCode;
const { icons } = primeIcons;
const { languages } = languageCode;

config.countries = countries;
config.dataflowStatus = dataflowStatus;
config.exportTypes = exportTypes;
config.icons = icons;
config.languages = languages;
config.MAX_FILE_SIZE = 100000000;
config.permissions = permissions;

export { config };
