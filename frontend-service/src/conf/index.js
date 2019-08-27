import primeIcons from './prime.icons';
import webConfig from './web.config';
import languageCode from './language.code.json';
import dataSet from './dataSet.json';

const { icons } = primeIcons;
const config = { ...webConfig };
const { languages } = languageCode;
config.MAX_FILE_SIZE = 100000000;
config.icons = icons;
config.languages = languages;
config.dataSet = dataSet;

export { config };
