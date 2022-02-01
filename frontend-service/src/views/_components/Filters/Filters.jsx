import { useContext } from 'react';
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
  filterByStore,
  filteredDataStore,
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

export const Filters = ({ className, isLoading, isStrictModeVisible, onFilter, onSort, options = [], recoilId }) => {
  const resourcesContext = useContext(ResourcesContext);

  const hasCustomSort = !isNil(onFilter) || !isNil(onSort);

  const onFilterFilteredData = useRecoilCallback(
    ({ snapshot, set }) =>
      async newData => {
        const data = await snapshot.getPromise(dataStore(recoilId));
        const allKeys = await snapshot.getPromise(filterByAllKeys(recoilId));
        const multiSelectKeys = await snapshot.getPromise(filterByKeyMultiSelectStore(recoilId));
        const inputKeys = await snapshot.getPromise(filterByKeyInputStore(recoilId));
        const searchKeys = await snapshot.getPromise(filterByKeySearchStore(recoilId));
        const checkboxKeys = await snapshot.getPromise(filterByKeyCheckboxStore(recoilId));

        const searchValue = await snapshot.getPromise(searchByStore(recoilId));
        const isStrictMode = await snapshot.getPromise(isStrictModeStore);

        const response = await Promise.all(
          allKeys.map(key => snapshot.getPromise(noWait(filterByStore(`${key}_${recoilId}`))))
        );
        const filterBy = Object.assign({}, ...response.map(res => res.contents));

        if (newData.type !== 'SEARCH' || newData.type !== 'STRICT_MODE') {
          filterBy[newData.key] = newData.value;
        }

        const filteredData = data.filter(item => {
          return (
            FiltersUtils.applyInputs({ filterBy, filteredKeys: inputKeys.keys, item }) &&
            FiltersUtils.applyCheckBox({ filterBy, filteredKeys: checkboxKeys.keys, item }) &&
            FiltersUtils.applyMultiSelects({ filterBy, filteredKeys: multiSelectKeys.keys, isStrictMode, item }) &&
            FiltersUtils.applySearch({ filteredKeys: searchKeys.keys, item, value: newData.searchValue || searchValue })
          );
        });

        set(filteredDataStore(recoilId), filteredData);
      },
    [recoilId]
  );

  const onResetFilters = useRecoilCallback(
    ({ snapshot, reset }) =>
      async () => {
        const filterByKeys = await snapshot.getPromise(filterByAllKeys(recoilId));

        reset(filteredDataStore(recoilId));
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

  return (
    <div className={className ? styles[className] : styles.default}>
      {renderFilters()}
      {isStrictModeVisible ? <StrictModeToggle onToggle={onFilterFilteredData} /> : null}

      {hasCustomSort && (
        <div className={`${styles.filterButton}`}>
          <Button
            className="p-button-primary p-button-rounded p-button-animated-blink"
            disabled={isLoading}
            icon="filter"
            label={resourcesContext.messages['filter']}
            onClick={onFilter}
          />
        </div>
      )}

      <div className={`${styles.resetButton}`}>
        <Button
          className="p-button-secondary p-button-rounded p-button-animated-blink"
          disabled={isLoading}
          icon="undo"
          label={resourcesContext.messages['reset']}
          onClick={onResetFilters}
        />
      </div>
    </div>
  );
};
