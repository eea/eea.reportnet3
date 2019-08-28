import primeIcons from './prime.icons';
import webConfig from './web.config';
import languageCode from './language.code.json';
import exportTypeCode from './exportType.code.json';

const { icons } = primeIcons;
const config = { ...webConfig };
const { languages } = languageCode;
const { exportTypes } = exportTypeCode;
config.MAX_FILE_SIZE = 100000000;
config.icons = icons;
config.languages = languages;
config.exportTypes = exportTypes;

export { config };
