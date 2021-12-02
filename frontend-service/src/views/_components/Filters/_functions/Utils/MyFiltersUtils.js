import dayjs from 'dayjs';
import isDate from 'lodash/isDate';

const deepIncludes = ({ entries, value }) => entries.toLowerCase().includes(value.toLowerCase());

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();

const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const parseDateValues = values => {
  if (!values) return [];

  return values.map(value => {
    if (!value) return null;

    return isDate(value) ? value.getTime() : new Date(value);
  });
};

export const MyFiltersUtils = { deepIncludes, getEndOfDay, getStartOfDay, parseDateValues };
