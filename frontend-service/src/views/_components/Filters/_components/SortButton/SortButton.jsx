import { useRecoilValue } from 'recoil';

import styles from '../../Filters.module.scss';

import { Button } from 'views/_components/Button';

import { sortByStore } from '../../_functions/Stores/filterStore';

export const SortButton = ({ id, isLoading, isVisible, onSortData, recoilId }) => {
  const sortBy = useRecoilValue(sortByStore(recoilId));

  const isSortActive = id === sortBy.sortByHeader && sortBy.sortByOption !== 'idle';

  const switchSortByIcon = () => {};

  if (!isVisible) {
    return <div className={styles.sortButtonSize} />;
  }

  return (
    <Button
      className={`p-button-secondary-transparent ${styles.sortButton} ${isSortActive ? styles.iconActive : null}`}
      disabled={isLoading}
      icon={id === sortBy.sortByHeader ? switchSortByIcon(sortBy.sortByOption) : 'sortAlt'}
      onClick={() => onSortData(id)}
    />
  );
};
