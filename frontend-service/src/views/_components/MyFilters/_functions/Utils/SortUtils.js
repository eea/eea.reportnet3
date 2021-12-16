import groupBy from 'lodash/groupBy';
import orderBy from 'lodash/orderBy';

const SORT_CATEGORY = 'pinned';

const applySort = ({ filteredData, order, prevSortState, sortByKey }) => {
  const copyFilteredData = [...filteredData];
  const groupedCategories = groupBy(copyFilteredData, SORT_CATEGORY);

  if (order === 'idle') return prevSortState;

  return Object.keys(groupedCategories).flatMap(key => orderBy(groupedCategories[key], [sortByKey], [order]));
};

const switchSortByIcon = sortByKey => {
  switch (sortByKey) {
    case 'idle':
      return 'sortAlt';

    case 'asc':
      return 'alphabeticOrderDown';

    case 'desc':
      return 'alphabeticOrderUp';

    default:
      return 'sortAlt';
  }
};

const switchSortByOption = sortByKey => {
  switch (sortByKey) {
    case 'idle':
      return 'asc';

    case 'asc':
      return 'desc';

    case 'desc':
      return 'idle';

    default:
      return 'asc';
  }
};

export const SortUtils = { applySort, switchSortByIcon, switchSortByOption };
