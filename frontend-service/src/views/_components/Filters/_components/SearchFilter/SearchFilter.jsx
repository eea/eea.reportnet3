import styles from '../../Filters.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { SortButton } from '../SortButton';

import { useSearch } from '../../_functions/Hooks/useSearch';

export const SearchFilter = ({ onFilterData, option, recoilId }) => {
  const { searchBy, onSearch } = useSearch({ onFilterData, option, recoilId });

  return (
    <div className={styles.block} key={option.key}>
      <SortButton id={option.key} isVisible={option.isSortable} />
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
        {searchBy && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onSearch('')}
          />
        )}

        <label className={styles.label} htmlFor={'searchInput'}>
          {option.label}
        </label>
      </div>
    </div>
  );
};
