import { useContext } from 'react';
import { noWait, useRecoilCallback, useRecoilValue } from 'recoil';

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
  filterByStore,
  filteredDataStore,
  isFilteredStore,
  isStrictModeStore,
  searchByStore
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
  className,
  isLoading,
  isStrictModeVisible,
  onFilter,
  onReset = () => {},
  onSort,
  options = [],
  recoilId
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const isFiltered = useRecoilValue(isFilteredStore(recoilId));

  const hasCustomSort = !isNil(onFilter) || !isNil(onSort);

  const onFilterFilteredData = useRecoilCallback(
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

        const searchValue = await snapshot.getPromise(searchByStore(recoilId));
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

        const filteredData = data.filter(
          item =>
            FiltersUtils.applyInputs({ filterBy, filteredKeys: inputKeys.keys, item }) &&
            FiltersUtils.applyCheckBox({ filterBy, filteredKeys: checkboxKeys.keys, item }) &&
            FiltersUtils.applyMultiSelects({ filterBy, filteredKeys: multiSelectKeys.keys, isStrictMode, item }) &&
            FiltersUtils.applySearch({ filteredKeys: searchKeys.keys, item, value: newData.searchValue || searchValue })
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

        reset(filteredDataStore(recoilId));
        reset(isFilteredStore(recoilId));
        await Promise.all(filterByKeys.map(key => reset(filterByStore(`${key}_${recoilId}`))));
      },
    [recoilId]
  );

  const renderFilters = () => options.map(option => renderFilter(option, option.type));

  const renderFilter = (option, type) => {
    if (option.nestedOptions) {
      return option.nestedOptions.map(nestedOption => renderFilter(nestedOption, option.type));
    }

    const FilterComponent = components[type];

    return (
      <FilterComponent
        isLoading={isLoading}
        key={option.key}
        onFilterData={onFilterFilteredData}
        onSort={onSort}
        option={option}
        recoilId={recoilId}
      />
    );
  };

  const renderStrictModeToggle = () => {
    if (!isStrictModeVisible) {
      return null;
    }

    return <StrictModeToggle onFilter={onFilterFilteredData} onToggle={onFilterFilteredData} />;
  };

  const renderCustomFiltersButton = () => {
    if (!hasCustomSort) {
      return null;
    }

    return (
      <div className={`${styles.filterButton}`}>
        <Button
          className="p-button-primary p-button-rounded p-button-animated-blink"
          disabled={isLoading}
          icon="filter"
          label={resourcesContext.messages['filter']}
          onClick={onFilter}
        />
      </div>
    );
  };

  return (
    <div className={className ? styles[className] : styles.default}>
      {renderFilters()}
      {renderStrictModeToggle()}
      {renderCustomFiltersButton()}

      <div className={`${styles.resetButton}`}>
        <Button
          className={`p-button-secondary p-button-rounded ${isFiltered ? 'p-button-animated-blink' : ''}`}
          disabled={isLoading}
          icon="undo"
          label={resourcesContext.messages['reset']}
          onClick={async () => {
            onReset();
            await onResetFilters();
          }}
        />
      </div>
    </div>
  );
};
