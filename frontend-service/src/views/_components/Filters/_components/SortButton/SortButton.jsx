import { useRecoilState } from 'recoil';

import styles from '../../Filters.module.scss';

import { Button } from 'views/_components/Button';

import { sortByStore } from 'views/_components/Filters/_functions/Stores/filterStore';

export const SortButton = ({ getFilterBy, id, isLoading, isVisible, onSort, recoilId }) => {
  const [sortBy, setSortBy] = useRecoilState(sortByStore(recoilId));

  const isSortActive = id === sortBy.sortByHeader && sortBy.sortByOption !== 'idle';

  const onApplySort = async () => {
    await getFilterBy();
    onSortData(id);
  };

  const onSortData = key => {
    setSortBy(prevSortBy => {
      const sortByHeader = switchSortByOption(prevSortBy.sortByOption) === 'idle' ? '' : key;
      const sortByOption = switchSortByOption(prevSortBy.sortByHeader === key ? prevSortBy.sortByOption : 'idle');

      if (onSort) {
        onSort({ sortByHeader, sortByOption });
      }

      return { sortByHeader, sortByOption };
    });
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

  if (!isVisible) {
    return null;
  }

  return (
    <Button
      className={`p-button-secondary-transparent ${styles.sortButton} ${isSortActive ? styles.iconActive : null}`}
      disabled={isLoading}
      icon={id === sortBy.sortByHeader ? switchSortByIcon(sortBy.sortByOption) : 'sortAlt'}
      onClick={onApplySort}
    />
  );
};
