import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';

const SORT_CATEGORY = 'pinned';

const applySort = ({ filteredData, sortBy }) => {
  const _filteredData = [...filteredData];
  const sortByKeys = Object.keys(sortBy).filter(sortByOption => sortBy[sortByOption] !== 'idle');
  const order = Object.values(sortBy).filter(orderOption => orderOption !== 'idle');

  const pinnedDataflows = _filteredData.filter(dataflow => dataflow.pinned === 'pinned');
  const dataflows = _filteredData.filter(dataflow => dataflow.pinned !== 'pinned');

  const sortPinnedDataflows = orderBy(pinnedDataflows, sortByKeys, order);
  const sortDataflows = orderBy(dataflows, sortByKeys, order);

  return [...sortPinnedDataflows, ...sortDataflows];

  // const sortedData = arrayForSort.sort((a, b) => {
  //   if (isNil(a[itemKey])) return null;

  //   if (!isNil(SORT_CATEGORY) && a[SORT_CATEGORY] !== b[SORT_CATEGORY]) {
  //     return a[SORT_CATEGORY] < b[SORT_CATEGORY] ? -2 : 2;
  //   }

  //   const optionA = a[itemKey].toUpperCase();
  //   const optionB = b[itemKey].toUpperCase();

  //   switch (sortOption) {
  //     case 'asc':
  //       return optionA > optionB ? 1 : -1;

  //     case 'desc':
  //       return optionA < optionB ? 1 : -1;

  //     case 'idle':
  //       return 0;

  //     default:
  //       return 0;
  //   }
  // });

  // return sortedData;
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
