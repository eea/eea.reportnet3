import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';

const parseText = (rawText = '', param = {}) => {
  if (isNil(rawText)) {
    return '';
  }

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

const ellipsis = (rawText = '', limit) => {
  if (isNil(rawText)) {
    return '';
  }

  if (rawText.length > limit - 3) {
    return `${rawText.substring(0, limit - 3)}...`;
  }
  return rawText;
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

const areEquals = (a, b) =>
  isNil(a) || isNil(b)
    ? false
    : typeof a === 'string' && typeof b === 'string'
    ? a.localeCompare(b, undefined, { sensitivity: 'accent' }) === 0
    : a === b;

const removeCommaSeparatedWhiteSpaces = str => str.replace(/^[,\s]+|[,\s]+$/g, ' ').replace(/\s*,\s*/g, ',');

const removeSemicolonSeparatedWhiteSpaces = str => str.replace(/;\s+/g, ';');

const splitByChar = (str, char = ',') => {
  return char === ','
    ? removeCommaSeparatedWhiteSpaces(str).split(char)
    : removeSemicolonSeparatedWhiteSpaces(str).split(char);
};

export const TextUtils = {
  areEquals,
  ellipsis,
  parseText,
  reduceString,
  removeCommaSeparatedWhiteSpaces,
  removeSemicolonSeparatedWhiteSpaces,
  splitByChar
};
