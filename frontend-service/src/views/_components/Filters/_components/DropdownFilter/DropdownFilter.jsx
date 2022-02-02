import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import styles from './DropdownFilter.module.scss';

import { Dropdown } from 'views/_components/Dropdown';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

export const DropdownFilter = ({ isLoading, onFilterData, onSort, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onFilter = async value => {
    setFilterBy({ [option.key]: value });
    await onFilterData({ key: option.key, value, type: option.type });
  };

  return (
    <div className={`${styles.block}`} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} onSort={onSort} />
      <Dropdown
        ariaLabel={option.key}
        className={`${styles.dropdownFilter} ${
          filterBy[option.key]?.length > 0 ? styles.elementFilterSelected : styles.elementFilter
        }`}
        filter={option.dropdownOptions.length > 10}
        filterPlaceholder={option.label}
        id={`${option.key}_dropdown`}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={option.key}
        label={option.label}
        onChange={event => onFilter(event.target.value)}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="label"
        options={option.dropdownOptions}
        showClear={filterBy[option.key]}
        showFilterClear={true}
        value={filterBy[option.key]}
      />
    </div>
  );
};
