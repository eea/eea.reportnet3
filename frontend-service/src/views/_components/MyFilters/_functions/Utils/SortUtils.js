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

const switchSortByOption = prevSortByOption => {
  switch (prevSortByOption) {
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

export const SortUtils = { switchSortByIcon, switchSortByOption };
