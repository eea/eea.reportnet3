import styles from '../../Filters.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { useSearch } from 'views/_components/Filters/_functions/Hooks/useSearch';

export const SearchFilter = ({ isLoading, onFilterData, option, recoilId }) => {
  const { searchBy, onSearch } = useSearch({ onFilterData, option, recoilId });

  const renderCleanInputButton = () => {
    if (!searchBy) {
      return null;
    }

    return (
      <Button
        className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
        icon="cancel"
        onClick={() => onSearch('')}
      />
    );
  };

  return (
    <div className={styles.block} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} />
      <div
        className={`p-float-label ${styles.label} ${styles.elementFilter} ${
          searchBy.length > 0 ? styles.elementFilterSelected : styles.elementFilter
        }`}>
        <InputText
          className={styles.searchInput}
          id="searchInput"
          onChange={event => onSearch(event.target.value)}
          value={searchBy}
        />
        {renderCleanInputButton()}

        <label className={styles.label} htmlFor={'searchInput'}>
          {option.label}
        </label>
      </div>
    </div>
  );
};