import { isDate } from 'lodash';

const parseDateValues = values => {
  if (!values) return [];

  return values.map(value => {
    if (!value) return null;

    return isDate(value) ? value.getTime() : new Date(value);
  });
};

export const MyFiltersUtils = { parseDateValues };
