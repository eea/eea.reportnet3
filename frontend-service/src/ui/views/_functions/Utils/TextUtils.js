import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';

const areEquals = (a, b) =>
  isNil(a) || isNil(b)
    ? false
    : typeof a === 'string' && typeof b === 'string'
    ? a.localeCompare(b, undefined, { sensitivity: 'accent' }) === 0
    : a === b;

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

const ellipsis = (rawText, limit) => {
  if (rawText.length > limit - 3) {
    return `${rawText.substr(0, limit - 3)}...`;
  }
  return rawText;
};

const removeCommaSeparatedWhiteSpaces = str => str.replace(/^[,\s]+|[,\s]+$/g, ' ').replace(/\s*,\s*/g, ',');

const removeSemicolonSeparatedWhiteSpaces = str => str.replace(/;\s+/g, ';');

const splitByChar = (str, char = ',') => {
  return char === ','
    ? removeCommaSeparatedWhiteSpaces(str).split(char)
    : removeSemicolonSeparatedWhiteSpaces(str).split(char);
};

export const TextUtils = {
  areEquals,
  parseText,
  ellipsis,
  removeCommaSeparatedWhiteSpaces,
  removeSemicolonSeparatedWhiteSpaces,
  splitByChar
};
