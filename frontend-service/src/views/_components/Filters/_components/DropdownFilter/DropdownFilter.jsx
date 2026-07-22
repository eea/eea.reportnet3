import { useEffect } from 'react';
import isNil from 'lodash/isNil';

import styles from './DropdownFilter.module.scss';

import { Dropdown } from 'primereact/dropdown';
import { LevelError } from 'views/_components/LevelError';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByKeyDropdownStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';
//react 18.3 changes needed for recoil to work correctly
export const DropdownFilter = ({
  getFilterBy,
  hasCustomSort,
  isLoading,
  onFilterData,
  onSort,
  option,
  panelClassName,
  recoilId
}) => {
  const { filterBy, onFilter } = useFilters({
    hasCustomSort,
    keyStore: filterByKeyDropdownStore,
    onFilterData,
    option,
    recoilId
  });

  useEffect(() => {
    if (!filterBy[option.key] && option.key === 'is_deleted') {
      onFilter(option.dropdownOptions[1]);
    }
  }, [filterBy]);

  const renderTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }
    return <span className={styles.statusBox}>{type?.toString()}</span>;
  };

  const currentRecoilState = filterBy[option.key];

  const selectedDropdownValue = currentRecoilState && typeof currentRecoilState === 'object' && 'value' in currentRecoilState
    ? currentRecoilState.value
    : currentRecoilState;


  const resolvedValue = option.key === 'is_deleted' && (selectedDropdownValue === undefined || selectedDropdownValue === null)
    ? false
    : selectedDropdownValue;

  return (
    <div
      className={`${styles.block} ${styles[option.className]} ${
        !option.isSortable && !isNil(option.isSortable) ? styles.noSortFilterWrapper : ''
      }`}
      key={option.key}>
      <SortButton
        getFilterBy={getFilterBy}
        id={option.key}
        isLoading={isLoading}
        isVisible={option.isSortable}
        onSort={onSort}
        recoilId={recoilId}
      />

      <span className="p-float-label" style={{ width: '100%' }}>
        <Dropdown
          ariaLabel={option.key}
          className={`${styles.dropdownFilter} ${
            !isNil(filterBy[option.key]) ? styles.elementFilterSelected : styles.elementFilter
          }`}
          filter={option.dropdownOptions.length > 10}
          filterPlaceholder={option.label}
          id={`${option.key}_dropdown`}
          inputId={option.key}
          itemTemplate={item => renderTemplate(option.template, item.label)}

          onChange={event => {
            onFilter({
              key: option.key,
              value: event.value
            });
          }}

          optionLabel="label"
          options={option.dropdownOptions}
          panelClassName={panelClassName}
          showClear={true}

          value={resolvedValue}
          style={{ width: '100%' }}
        />
        <label htmlFor={option.key}>{option.label}</label>
      </span>
    </div>
  );
};