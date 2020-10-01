import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

const getWebformTabs = (allTables = []) => {
  const initialValues = {};

  const value = allTables[0];

  compact(allTables).forEach(table => {
    initialValues[table] = false;
    initialValues[value] = true;
  });

  return initialValues;
};

const mergeArrays = (array1 = [], array2 = [], array1Key = '', array2Key = '') => {
  const result = [];
  for (let i = 0; i < array1.length; i++) {
    result.push({ ...array1[i], ...array2.find(element => element[array2Key] === array1[i][array1Key]) });
  }
  return result;
};

export const Article15Utils = { getWebformTabs, mergeArrays };
