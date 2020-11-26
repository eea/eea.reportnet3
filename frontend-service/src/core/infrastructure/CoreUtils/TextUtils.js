import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';
import isUndefined from 'lodash/isUndefined';

const ellipsis = (rawText = '', limit) => {
  if (!isNil(rawText) && !isUndefined(limit) && rawText.length > limit - 3) {
    return `${rawText.substr(0, limit - 3)}...`;
  }
  return rawText;
};

const parseText = (rawText = '', param = {}) => {
  let text = rawText;
  if (isObject(param)) {
    Object.keys(param).forEach(key => {
      text = text.replace(
        new RegExp(`{:${key}}`.replace(/[.*+\-?^${}()|[\]\\]/g, '\\$&'), 'g'),
        !isNil(param[key]) ? param[key] : ''
      );
    });
  }
  return text;
};

const reduceString = (text, prefix, suffix) => {
  let index = text.indexOf(prefix);
  if (index >= 0) {
    text = text.substring(index + prefix.length);
  } else {
    return '';
  }
  if (suffix) {
    index = text.indexOf(suffix);
    if (index < 0) {
      return '';
    } else {
      text = text.substring(0, index);
    }
  }
  return text;
};

export const TextUtils = {
  ellipsis,
  parseText,
  reduceString
};
