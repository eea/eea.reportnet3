import primeIcons from './prime.icons';
import webConfig from './web.config';

const { icons } = primeIcons;
const config = { ...webConfig };
config.MAX_FILE_SIZE = 100000000;
config.icons = icons;

export { config };
