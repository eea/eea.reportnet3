import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import uniq from 'lodash/uniq';

import styles from './DropdownFilter.module.scss';

import { Dropdown } from 'views/_components/Dropdown';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';
import { isNil } from 'lodash';

export const DropdownFilter = ({ isLoading, onSort, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  return (
    <div className={`${styles.block}`} key={option.key}>
      <SortButton id={option.key} isLoading={isLoading} isVisible={option.isSortable} onSort={onSort} />
      <Dropdown
        ariaLabel={option.key}
        className={`p-float-label ${styles.dropdownFilter} ${
          !isNil(filterBy[option.key]) ? styles.elementFilterSelected : styles.elementFilter
        }`}
        filter={option.dropdownOptions.length > 10}
        filterPlaceholder={option.label}
        id={`${option.key}_dropdown`}
        inputClassName={styles.label}
        inputId={option.key}
        label={option.label}
        onChange={event => setFilterBy({ [option.key]: event.target.value })}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="label"
        options={option.dropdownOptions}
        showClear={isNil(filterBy[option.key])}
        showFilterClear={true}
        value={filterBy[option.key]}
      />
    </div>
  );
};
