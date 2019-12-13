import isObject from 'lodash/isObject';
import isUndefined from 'lodash/isUndefined';

const parseText = (rawText, param = {}) => {
  let text = rawText;
  if (isObject(param)) {
    Object.keys(param).forEach(key => {
      if (param[key]) {
        text = text.replace(`{:${key}}`, param[key]);
      } else {
        text = text.replace(`{:${key}}`, '');
      }
    });
  }
  return text;
};

export const TextUtils = {
  parseText
};
