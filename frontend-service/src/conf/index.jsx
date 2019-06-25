import primeIcons from "./prime.icons";
import webConfig from "./web.config";

const { icons } = primeIcons;
const config = { ...webConfig };
config.icons = icons;

export default config;
