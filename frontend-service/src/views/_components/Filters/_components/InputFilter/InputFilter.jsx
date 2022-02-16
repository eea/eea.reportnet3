import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import sharedStyles from 'views/_components/Filters/Filters.module.scss';
import styles from './InputFilter.module.scss';

import { Button } from 'views/_components/Button';
import { InputText } from 'views/_components/InputText';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByKeyInputStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';

export const InputFilter = ({ hasCustomSort, isLoading, onCustomFilter, onFilterData, onSort, option, recoilId }) => {
  const { filterBy, onFilter } = useFilters({
    hasCustomSort,
    keyStore: filterByKeyInputStore,
    onFilterData,
    option,
    recoilId
  });

  const onKeyPress = event => {
    if (event.key === 'Enter' && !isNil(onCustomFilter)) {
      onFilter(event.target.value);
      onCustomFilter();
    }
  };

  return (
    <div className={styles.block} key={option.key}>
      <SortButton
        id={option.key}
        isLoading={isLoading}
        isVisible={option.isSortable}
        onSort={onSort}
        recoilId={recoilId}
      />
      <div
        className={`p-float-label ${
          filterBy[option.key]?.length > 0 ? sharedStyles.elementFilterSelected : sharedStyles.elementFilter
        }`}>
        <InputText
          className={styles.inputFilter}
          id={`${option.key}_input`}
          key={option.key}
          keyfilter={option.keyfilter}
          onChange={event => onFilter(event.target.value)}
          onKeyPress={onKeyPress}
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
