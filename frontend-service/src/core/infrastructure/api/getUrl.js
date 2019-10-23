import isObject from 'lodash/isObject';
import isUndefined from 'lodash/isUndefined';

export const getUrl = (url, urlParams = {}, isRoute = false) => {
  let cUrl = url;
  if (isObject(urlParams)) {
    const keys = Object.keys(urlParams);

    keys.forEach(key => {
      if (isUndefined(urlParams[key])) {
        let min = cUrl.indexOf(`{:${key}}`) - `${key}`.length - 2;
        let max = cUrl.indexOf(`{:${key}}`) + `{:${key}}`.length;
        if (cUrl.charAt(min) === '?') {
          min++;
          max++;
        }
        cUrl = cUrl.substr(0, min) + cUrl.substr(max);
      } else {
        //Routes are defined without {}, so we will check isRoute for the url replacement.
        if (!isRoute) {
          cUrl = cUrl.replace(`{:${key}}`, urlParams[key]);
        } else {
          cUrl = cUrl.replace(`:${key}`, urlParams[key]);
        }
      }
    });
  }
  return cUrl;
};
