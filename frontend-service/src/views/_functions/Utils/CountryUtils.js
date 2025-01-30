import isNil from 'lodash/isNil';

import { config } from 'conf';

const getCountryName = countryCode => {
  if (!isNil(config.countriesByGroup)) {
    const allCountries = config.countriesByGroup['eeaCountries']
      .concat(config.countriesByGroup['cooperatingCountries'])
      .concat(config.countriesByGroup['otherCountries']);
    const countryName = allCountries.find(country => countryCode === country.code).name;

    return countryName;
  }
};

const getCountryCode = countryName => {
  if (!isNil(config.countriesByGroup)) {
    const allCountries = config.countriesByGroup['eeaCountries']
      .concat(config.countriesByGroup['cooperatingCountries'])
      .concat(config.countriesByGroup['otherCountries']);
    const countryCode = allCountries.find(country => countryName === country.name).code;
    return countryCode;
  }
};

export const CountryUtils = {
  getCountryName,
  getCountryCode
};
