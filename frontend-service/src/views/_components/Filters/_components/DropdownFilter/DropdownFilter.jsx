import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';

import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import styles from './DropdownFilter.module.scss';

import { Dropdown } from 'views/_components/Dropdown';
import { LevelError } from 'views/_components/LevelError';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { filterByAllKeys } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

export const DropdownFilter = ({ hasCustomSort, isLoading, onFilterData, onSort, option, recoilId }) => {
  const setFilterByAllKeys = useSetRecoilState(filterByAllKeys(recoilId));

  const [filterBy, setFilterBy] = useRecoilState(filterByStore(`${option.key}_${recoilId}`));

  useEffect(() => {
    setFilterByAllKeys(prevState => uniq([...prevState, option.key]));
  }, [recoilId]);

  const onFilter = async value => {
    setFilterBy({ [option.key]: value });

    if (!hasCustomSort) {
      await onFilterData({ key: option.key, value, type: option.type });
    }
  };

  const renderTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }

    return <span className={styles.statusBox}>{type?.toString()}</span>;
  };

  return (
    <div className={`${styles.block} ${option.label === 'Status' && styles.roleFilter}`} key={option.key}>
      <SortButton
        id={option.key}
        isLoading={isLoading}
        isVisible={option.isSortable}
        onSort={onSort}
        recoilId={recoilId}
      />
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
        itemTemplate={item => renderTemplate(option.template, item.label)}
        label={option.label}
        onChange={event => onFilter(event.target.value)}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="label"
        options={option.dropdownOptions}
        showClear={true}
        showFilterClear={true}
        value={filterBy[option.key]}
      />
    </div>
  );
};
