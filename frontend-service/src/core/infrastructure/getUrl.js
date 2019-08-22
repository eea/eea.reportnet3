import isObject from 'lodash/isObject';

export const getUrl = (url, urlParams = {}) => {
  let cUrl = url;
  if (isObject(urlParams)) {
    const keys = Object.keys(urlParams);
    keys.forEach(key => {
      cUrl = cUrl.replace(`{:${key}}`, urlParams[key]);
    });
  }
  return cUrl;
};
