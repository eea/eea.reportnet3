import isNil from 'lodash/isNil';

import styles from './DropdownFilter.module.scss';

import { Dropdown } from 'views/_components/Dropdown';
import { LevelError } from 'views/_components/LevelError';
import { SortButton } from 'views/_components/Filters/_components/SortButton';

import { filterByKeyDropdownStore } from 'views/_components/Filters/_functions/Stores/filterKeysStore';

import { useFilters } from 'views/_components/Filters/_functions/Hooks/useFilters';

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

  const renderTemplate = (template, type) => {
    if (template === 'LevelError') {
      return <LevelError type={type} />;
    }

    return <span className={styles.statusBox}>{type?.toString()}</span>;
  };

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
        panelClassName={panelClassName}
        showClear={true}
        showFilterClear={true}
        value={filterBy[option.key]}
      />
    </div>
  );
};
