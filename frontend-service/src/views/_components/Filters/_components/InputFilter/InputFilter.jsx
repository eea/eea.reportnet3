import isEmpty from 'lodash/isEmpty';

import styles from './InputFilter.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { SortButton } from '../SortButton';

import { filterByKeyInputStore } from '../../_functions/Stores/filterKeysStore';

import { useFilters } from '../../_functions/Hooks/useFilters';

export const InputFilter = ({ isLoading, onFilterData, onSort, option, recoilId }) => {
  const { filterBy, onFilter } = useFilters({ keyStore: filterByKeyInputStore, onFilterData, option, recoilId });

  return (
    <div className={styles.block} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} onSort={onSort} />
      <div
        className={`p-float-label ${styles.label} ${
          filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
        }`}>
        <InputText
          className={styles.inputFilter}
          id={`${option.key}_input`}
          key={option.key}
          onChange={event => onFilter(event.target.value)}
          value={filterBy[option.key] || ''}
        />
        <label className={styles.label} htmlFor={`${option.key}_input`}>
          {option.label || ''}
        </label>
        {!isEmpty(filterBy[option.key]) && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onFilter('')}
          />
        )}
      </div>
    </div>
  );
};
