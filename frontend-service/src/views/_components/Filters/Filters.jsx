import { useContext, useState, useEffect, useRef } from 'react';
import { noWait, useRecoilCallback } from 'recoil';

import isNil from 'lodash/isNil';

import styles from './Filters.module.scss';

import { Button } from 'views/_components/Button';
import { CheckboxFilter } from './_components/CheckboxFilter';
import { DateFilter } from './_components/DateFilter';
import { DropdownFilter } from './_components/DropdownFilter';
import { InputFilter } from './_components/InputFilter';
import { MultiSelectFilter } from './_components/MultiSelectFilter';
import { SearchFilter } from './_components/SearchFilter';
import { StrictModeToggle } from './_components/StrictModeToggle';

import {
  dataStore,
  filterByCustomFilterStore,
  filterByStore,
  filteredDataStore,
  isFilteredStore,
  isStrictModeStore,
  searchByStore,
  sortByStore
} from './_functions/Stores/filterStore';

import {
  filterByAllKeys,
  filterByKeyCheckboxStore,
  filterByKeyInputStore,
  filterByKeyMultiSelectStore,
  filterByKeySearchStore
} from './_functions/Stores/filterKeysStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { FiltersUtils } from './_functions/Utils/FiltersUtils';

const components = {
  CHECKBOX: CheckboxFilter,
  DATE: DateFilter,
  DROPDOWN: DropdownFilter,
  INPUT: InputFilter,
  MULTI_SELECT: MultiSelectFilter,
  SEARCH: SearchFilter
};

export const Filters = ({
  activeIndex,
  className,
  isJobsStatuses = false,
  isLoading,
  isProvider,
  isStrictModeVisible,
  onFilter,
  onReset = () => {},
  onSort,
  options = [],
  panelClassName,
  providerUsername,
  recoilId
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [viewDate, setViewData] = useState(undefined);

  const hasCustomSort = !isNil(onFilter) || !isNil(onSort);

  const hasFiltersBeenRendered = useRef(false);

  useEffect(() => {
    if (isJobsStatuses) {
      if (hasFiltersBeenRendered.current) {
        setViewData(new Date());
        onResetFilters();
        onReset({ sortByHeader: '', sortByOption: 'idle' });
      }
      hasFiltersBeenRendered.current = true;
    }
  }, [activeIndex]);

  const clearDateInputs = () => {
    [...document.getElementsByClassName('date-filter-input')].forEach(input => (input.value = ''));
  };

  const getFilterBy = useRecoilCallback(
    ({ snapshot, set }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));
        const response = await Promise.all(
          filterByKeys.map(key => snapshot.getPromise(filterByStore(`${key}_${recoilId}`)))
        );

        const responseFilters = Object.assign({}, ...response);
        const responseFiltersLength = Object.keys(responseFilters).length;

        const filterBy =
          isProvider && !(isJobsStatuses && activeIndex === 1 && responseFiltersLength === 0)
            ? Object.assign({}, ...response, { creatorUsername: [providerUsername] })
            : Object.assign({}, ...response);

        set(filterByCustomFilterStore(recoilId), filterBy);
      },
    [recoilId, activeIndex]
  );

  const onApplyFilters = async () => {
    await getFilterBy();
    await onFilter();
  };

  const onFilterData = useRecoilCallback(
    ({ snapshot, set }) =>
      async newData => {
        const data = await snapshot.getPromise(dataStore(recoilId));
        const allKeys = await snapshot.getPromise(filterByAllKeys(recoilId));

        const keys = [
          filterByKeyCheckboxStore(recoilId),
          filterByKeyInputStore(recoilId),
          filterByKeyMultiSelectStore(recoilId),
          filterByKeySearchStore(recoilId)
        ];

        const [checkboxKeys, inputKeys, multiSelectKeys, searchKeys] = await Promise.all(
          keys.map(key => snapshot.getPromise(key))
        );

        let searchValue = await snapshot.getPromise(searchByStore(recoilId));
        let isStrictMode = await snapshot.getPromise(isStrictModeStore);

        const response = await Promise.all(
          allKeys.map(key => snapshot.getPromise(noWait(filterByStore(`${key}_${recoilId}`))))
        );
        const filterBy = Object.assign({}, ...response.map(res => res.contents));

        if (newData.type !== 'SEARCH' || newData.type !== 'STRICT_MODE') {
          filterBy[newData.key] = newData.value;
        }

        if (newData.type === 'STRICT_MODE') {
          isStrictMode = newData.isStrictMode;
        }

        if (newData.type === 'SEARCH') {
          searchValue = newData.searchValue;
        }

        const filteredData = data.filter(
          item =>
            FiltersUtils.applyInputs({ filterBy, filteredKeys: inputKeys.keys, item }) &&
            FiltersUtils.applyCheckBox({ filterBy, filteredKeys: checkboxKeys.keys, item }) &&
            FiltersUtils.applyMultiSelects({ filterBy, filteredKeys: multiSelectKeys.keys, isStrictMode, item }) &&
            FiltersUtils.applySearch({ filteredKeys: searchKeys.keys, item, value: searchValue })
        );

        set(isFilteredStore(recoilId), FiltersUtils.getIsFiltered(filterBy));
        set(filteredDataStore(recoilId), filteredData);
      },
    [recoilId]
  );

  const onResetFilters = useRecoilCallback(
    ({ snapshot, reset }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));

        reset(searchByStore(recoilId));
        reset(sortByStore(recoilId));
        reset(filteredDataStore(recoilId));
        reset(isFilteredStore(recoilId));
        reset(filterByCustomFilterStore(recoilId));
        clearDateInputs();
        await Promise.all(filterByKeys.map(key => reset(filterByStore(`${key}_${recoilId}`))));
      },
    [recoilId]
  );

  const renderFilter = (option, type) => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderFilter(nestedOption, option.type));
    }

    const FilterComponent = components[type];

    return (
      <FilterComponent
        getFilterBy={getFilterBy}
        hasCustomSort={hasCustomSort}
        isLoading={isLoading}
        key={option.key}
        onCustomFilter={onFilter}
        onFilterData={onFilterData}
        onSort={onSort}
        option={option}
        panelClassName={panelClassName}
        recoilId={recoilId}
        viewDate={viewDate}
      />
    );
  };

  const renderStrictModeToggle = () => {
    if (!isStrictModeVisible) {
      return null;
    }

    return <StrictModeToggle onFilter={onFilterData} onToggle={onFilterData} />;
  };

  const renderCustomFiltersButton = () => {
    if (!hasCustomSort) {
      return null;
    }

    return (
      <div className={`${styles.filterButton}`}>
        <Button
          className={`p-button-primary p-button-rounded p-button-animated-blink`}
          disabled={isLoading}
          icon="filter"
          label={resourcesContext.messages['filter']}
          onClick={onApplyFilters}
        />
      </div>
    );
  };

  return (
    <div className={`${className ? styles[className] : styles.default}`}>
      {options.map(option => renderFilter(option, option.type))}
      {renderStrictModeToggle()}

      <div className={styles.buttonWrapper}>
        {renderCustomFiltersButton()}
        <div className={`${styles.resetButton}`}>
          <Button
            className="p-button-secondary p-button-rounded p-button-animated-blink"
            disabled={isLoading}
            icon="undo"
            label={resourcesContext.messages['reset']}
            onClick={async () => {
              setViewData(new Date());
              await onResetFilters();
              await onReset({ sortByHeader: '', sortByOption: 'idle' });
            }}
          />
        </div>
      </div>
    </div>
  );
};
