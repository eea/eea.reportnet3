import primeIcons from "./prime.icons";
import webConfig from "./web.config";
import baseConfig from "./site.config";

const { icons } = primeIcons;
const config = { ...webConfig, ...baseConfig };
config.icons = icons;

export default config;
