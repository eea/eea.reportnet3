import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uuid from 'uuid';

import styles from './Filters.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { useOnClickOutside } from 'ui/views/_functions/Hooks/useOnClickOutside';

import { ApplyFilterUtils } from './_functions/Utils/ApplyFilterUtils';
import { ErrorUtils } from 'ui/views/_functions/Utils';
import { FiltersUtils } from './_functions/Utils/FiltersUtils';
import { SortUtils } from './_functions/Utils/SortUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const Filters = ({
  checkboxOptions,
  className,
  data = [],
  dateOptions,
  dropDownList,
  dropdownOptions,
  filterByList,
  getFilteredData,
  getFilteredSearched = () => {},
  inputOptions,
  matchMode,
  searchAll,
  searchBy = [],
  selectList,
  selectOptions,
  sendData,
  sortable,
  sortCategory,
  validations,
  validationsAllTypesFilters
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dateRef = useRef(null);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    clearedFilters: false,
    checkboxes: [],
    data: data,
    filterBy: {},
    previousState: {},
    filtered: false,
    filteredData: data,
    filteredSearched: false,
    labelAnimations: {},
    matchMode: true,
    orderBy: {},
    property: '',
    searchBy: '',
    searched: false
  });

  useEffect(() => {
    getInitialState();
  }, [data]);

  useEffect(() => {
    if (filterState.filtered) {
      onReApplyFilters();
    }
  }, [filterState.data]);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filterState.filteredData);
  }, [filterState.filteredData]);

  useEffect(() => {
    onReApplyFilters();
  }, [filterState.matchMode]);

  useEffect(() => {
    getFilteredSearchedStateValue();
  }, [filterState.filtered, filterState.searched]);

  useEffect(() => {
    getFilteredState();
  }, [filterState.filterBy]);

  useEffect(() => {
    getFilteredSearched(filterState.filteredSearched);
  }, [filterState.filteredSearched]);

  useEffect(() => {
    getChangedCheckboxes(filterState.property);
  }, [JSON.stringify(filterState.checkboxes), filterState.property]);

  useEffect(() => {
    if (sendData && filterState.clearedFilters) {
      sendData(filterState.filterBy);
      filterDispatch({ type: 'SET_CLEARED_FILTERS', payload: false });
    }
  }, [filterState.clearedFilters]);
  useOnClickOutside(dateRef, () => isEmpty(filterState.filterBy[dateOptions]) && onAnimateLabel([dateOptions], false));

  const getCheckboxFilterState = property => {
    const [checkBox] = filterState.checkboxes.filter(checkbox => checkbox.property === property);
    return isNil(checkBox) ? false : checkBox.isChecked;
  };

  const getFilteredSearchedStateValue = () => {
    const filteredSearchedValue = filterState.filtered || filterState.searched ? true : false;
    filterDispatch({ type: 'FILTERED_SEARCHED_STATE', payload: { filteredSearchedValue } });
  };

  const getFilteredState = () => {
    let filteredStateValue = false;
    if (filterState.checkboxes.length > 0) {
      const filtersValue = [];
      Object.values(filterState.filterBy).forEach(value => {
        !isEmpty(value) && filtersValue.push(!isEmpty(value));
        if (value.includes(true) && value.includes(false)) {
          filtersValue.pop();
        }
        filteredStateValue = filtersValue.includes(true);
      });
    } else {
      filteredStateValue = Object.values(filterState.filterBy)
        .map(value => isEmpty(value))
        .includes(false);
    }

    filterDispatch({ type: 'FILTERED', payload: { filteredStateValue } });
  };

  const getInitialState = () => {
    const initialData = cloneDeep(data);

    const initialFilterBy = FiltersUtils.getFilterInitialState(
      data,
      inputOptions,
      selectOptions,
      dateOptions,
      dropdownOptions,
      checkboxOptions,
      filterByList
    );

    const initialFilteredData = ApplyFilterUtils.onApplySearch(data, searchBy, filterState.searchBy, filterState);

    const initialLabelAnimations = FiltersUtils.getLabelInitialState(
      inputOptions,
      selectOptions,
      dateOptions,
      dropdownOptions,
      checkboxOptions,
      filterState.filterBy
    );

    const initialOrderBy = SortUtils.getOrderInitialState(
      inputOptions,
      selectOptions,
      dateOptions,
      dropdownOptions,
      checkboxOptions
    );
    const initialCheckboxes = FiltersUtils.getCheckboxFilterInitialState(checkboxOptions);

    filterDispatch({
      type: 'INITIAL_STATE',
      payload: {
        initialData,
        initialFilterBy,
        initialFilteredData,
        initialLabelAnimations,
        initialOrderBy,
        initialCheckboxes
      }
    });
  };

  const onAnimateLabel = (property, value) => {
    filterDispatch({ type: 'ANIMATE_LABEL', payload: { animatedProperty: property, isAnimated: value } });
  };

  const getChangedCheckboxes = property => {
    filterState.checkboxes.forEach(checkbox => {
      if (checkbox.property === property) {
        checkbox.isChecked
          ? onFilterData(checkbox.property, [checkbox.isChecked])
          : onFilterData(checkbox.property, [checkbox.isChecked, !checkbox.isChecked]);
      }
    });
  };

  const onChangeCheckboxFilter = property => {
    filterDispatch({ type: 'ON_CHECKBOX_FILTER', payload: { property } });
  };

  const onClearAllFilters = () => {
    filterDispatch({
      type: 'CLEAR_ALL',
      payload: {
        filterBy: FiltersUtils.getFilterInitialState(
          data,
          inputOptions,
          selectOptions,
          dateOptions,
          dropdownOptions,
          checkboxOptions
        ),
        filteredData: cloneDeep(data),
        labelAnimations: ApplyFilterUtils.onClearLabelState(
          inputOptions,
          selectOptions,
          dateOptions,
          dropdownOptions,
          checkboxOptions
        ),
        orderBy: SortUtils.getOrderInitialState(
          inputOptions,
          selectOptions,
          dateOptions,
          dropdownOptions,
          checkboxOptions
        ),
        searchBy: '',
        checkboxes: FiltersUtils.getCheckboxFilterInitialState(checkboxOptions),
        filtered: false,
        filteredSearched: false,
        clearedFilters: true
      }
    });
  };

  const onFilterData = (filter, value) => {
    const inputKeys = FiltersUtils.getFilterKeys(filterState, filter, inputOptions);
    const searchedKeys = !isEmpty(searchBy) ? searchBy : ApplyFilterUtils.getSearchKeys(filterState.data);
    const selectedKeys = FiltersUtils.getSelectedKeys(filterState, filter, selectOptions);
    const checkedKeys = FiltersUtils.getSelectedKeys(filterState, filter, checkboxOptions);

    console.log('filter, value', filter, value);

    const filteredData = ApplyFilterUtils.onApplyFilters({
      dateOptions,
      dropdownOptions,
      filter,
      filteredKeys: inputKeys,
      searchedKeys,
      selectedKeys,
      selectOptions,
      checkedKeys,
      checkboxOptions,
      state: filterState,
      data: data,
      value
    });

    filterDispatch({ type: 'FILTER_DATA', payload: { filteredData, filter, value } });
  };

  const onOrderData = (order, property) => {
    const sortedData = SortUtils.onSortData([...filterState.data], order, property, sortCategory);
    const filteredSortedData = SortUtils.onSortData([...filterState.filteredData], order, property, sortCategory);
    const orderBy = order === 0 ? -1 : order;
    const resetOrder = SortUtils.onResetOrderData(inputOptions, selectOptions, dateOptions, checkboxOptions);

    filterDispatch({ type: 'ORDER_DATA', payload: { filteredSortedData, orderBy, property, resetOrder, sortedData } });
  };

  const onSearchData = value => {
    const inputKeys = FiltersUtils.getFilterKeys(filterState, '', inputOptions);
    const selectedKeys = FiltersUtils.getSelectedKeys(filterState, '', selectOptions);
    const checkedKeys = FiltersUtils.getSelectedKeys(filterState, '', checkboxOptions);
    const searchedValues = ApplyFilterUtils.onApplySearch(
      filterState.data,
      searchBy,
      value,
      filterState,
      inputKeys,
      selectedKeys,
      checkedKeys
    );
    const searched = isEmpty(value) ? false : true;

    filterDispatch({ type: 'ON_SEARCH_DATA', payload: { searchedValues, value, searched } });
  };

  const onToggleMatchMode = () => filterDispatch({ type: 'TOGGLE_MATCH_MODE', payload: !filterState.matchMode });

  const onReApplyFilters = () => {
    let filterBy = { ...filterState.filterBy };

    const filterKeys = Object.keys(filterBy);

    if (!isEmpty(filterBy)) {
      filterBy = filterState.previousState.filterBy;
      const possibleOptions = new Map();
      filterKeys.forEach(key => possibleOptions.set(key, new Set()));

      const initialFilteredData = ApplyFilterUtils.onApplySearch(data, searchBy, filterState.searchBy, filterState);

      console.log('initialFilteredData', initialFilteredData);

      initialFilteredData.forEach(item =>
        selectOptions.forEach(filterKey => {
          let currentValue = possibleOptions.get(filterKey);

          currentValue.add(item[filterKey]);
        })
      );

      const filterByAsKeyValueArray = Object.entries(filterBy);

      const removeInexistentFilters = () => {
        return filterByAsKeyValueArray.map(keyValue => {
          selectOptions.forEach(key => {
            // key [0], value [1]
            if (key === keyValue[0]) {
              keyValue[1] = keyValue[1].filter(value => {
                const option = possibleOptions.get(key);

                if (key === 'pinned' || key === 'table' || key === 'field') {
                  return option.has(value.toLowerCase());
                }
                return option.has(value);
              });
            }
          });

          return keyValue;
        });
      };

      const parsedResult = removeInexistentFilters();

      filterBy = Object.fromEntries(parsedResult);

      filterDispatch({ type: 'UPDATE_FILTER_BY', payload: { filterBy } });
    }

    for (let index = 0; index < filterKeys.length; index++) {
      const filter = filterKeys[index];
      const value = filterBy[filter];

      if (!isEmpty(value)) {
        onFilterData(filter, filterBy[filter]);
      }
    }
  };

  const renderCalendarFilter = (property, i) => {
    const inputId = uuid.v4();
    return (
      <span key={i} className={styles.dataflowInput} ref={dateRef}>
        {renderOrderFilter(property)}
        <span className={`p-float-label ${!sendData ? styles.label : ''}`}>
          <Calendar
            className={styles.calendarFilter}
            dateFormat={userContext.userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
            inputClassName={styles.inputFilter}
            inputId={inputId}
            monthNavigator={true}
            onChange={event => onFilterData(property, event.value)}
            onFocus={() => onAnimateLabel(property, true)}
            readOnlyInput={true}
            selectionMode="range"
            showWeek={true}
            style={{ zoom: '0.95' }}
            value={filterState.filterBy[property]}
            yearNavigator={true}
            yearRange="2015:2030"
          />
          {!isEmpty(filterState.filterBy[property]) && (
            <Button
              className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
              icon="cancel"
              onClick={() => {
                onFilterData(property, []);
                onAnimateLabel(property, false);
                document.getElementById(inputId).value = '';
              }}
            />
          )}
          <label
            className={!filterState.labelAnimations[property] ? styles.labelDown : styles.label}
            htmlFor={property}>
            {resources.messages[property]}
          </label>
        </span>
      </span>
    );
  };

  const renderCheckbox = () => (
    <Fragment>
      <span className={styles.checkboxWrap} data-tip data-for="checkboxTooltip">
        {resources.messages['strictModeCheckboxFilter']}
        <Button
          className={`${styles.strictModeInfoButton} p-button-rounded p-button-secondary-transparent`}
          icon="infoCircle"
          tooltip={resources.messages['strictModeTooltip']}
          tooltipOptions={{ position: 'top' }}
        />
        <span className={styles.checkbox}>
          <Checkbox
            id={`matchMode_checkbox`}
            inputId={`matchMode_checkbox`}
            isChecked={filterState.matchMode}
            onChange={() => onToggleMatchMode()}
            role="checkbox"
          />
          <label htmlFor={`matchMode_checkbox`} className="srOnly">
            {resources.messages['strictModeCheckboxFilter']}
          </label>
        </span>
      </span>
    </Fragment>
  );

  const renderCheckboxFilter = (property, i) => {
    return (
      <span key={i} className={styles.checkboxWrap}>
        <div>
          <span className={styles.switchTextInput}>{resources.messages[property]}</span>
          <span className={styles.checkbox}>
            <Checkbox
              id={property}
              inputId={property}
              isChecked={getCheckboxFilterState(property)}
              label={property}
              onChange={() => onChangeCheckboxFilter(property)}
              style={{ marginRight: '50px' }}
            />
            <label htmlFor={property} className="srOnly">
              {resources.messages[property]}
            </label>
          </span>
        </div>
      </span>
    );
  };

  const renderDropdown = (property, i) => (
    <span key={i} className={`${styles.dataflowInput}`}>
      {renderOrderFilter(property)}
      <Dropdown
        ariaLabel={property}
        className={styles.dropdownFilter}
        filter={FiltersUtils.getOptionTypes(data, property, dropDownList).length > 10}
        filterPlaceholder={resources.messages[property]}
        id={property}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={property}
        label={resources.messages[property]}
        onChange={event => onFilterData(property, event.value)}
        onMouseDown={event => {
          event.preventDefault();
          event.stopPropagation();
        }}
        optionLabel="type"
        options={FiltersUtils.getOptionTypes(data, property, dropDownList)}
        showClear={!isEmpty(filterState.filterBy[property])}
        showFilterClear={true}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const renderInputFilter = (property, i) => (
    <span key={i} className={styles.dataflowInput}>
      {renderOrderFilter(property)}
      <span className={`p-float-label ${styles.label}`}>
        <InputText
          className={styles.inputFilter}
          id={property}
          onChange={event => onFilterData(property, event.target.value)}
          value={filterState.filterBy[property] ? filterState.filterBy[property] : ''}
        />
        {filterState.filterBy[property] && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onFilterData(property, '')}
          />
        )}
        <label className={styles.label} htmlFor={property}>
          {resources.messages[property]}
        </label>
      </span>
    </span>
  );

  const renderOrderFilter = property =>
    sortable ? (
      <Button
        className={`p-button-secondary-transparent ${styles.icon}`}
        layout="simple"
        icon={SortUtils.getOrderIcon(filterState.orderBy[property])}
        id={`${property}_sort`}
        onClick={() => onOrderData(filterState.orderBy[property], property)}
        style={{ fontSize: '12pt' }}
        tooltip={resources.messages['sort']}
        tooltipOptions={{ position: 'bottom' }}
        value={`${property}_sortOrder`}
      />
    ) : (
      <Fragment />
    );

  const renderSelectFilter = (property, i) => (
    <span key={i} className={`${styles.dataflowInput}`}>
      {renderOrderFilter(property)}
      <MultiSelect
        ariaLabelledBy={property}
        checkAllHeader={resources.messages['checkAllFilter']}
        className={styles.multiselectFilter}
        headerClassName={styles.selectHeader}
        id={property}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={property}
        isFilter={true}
        itemTemplate={selectTemplate}
        label={resources.messages[property]}
        notCheckAllHeader={resources.messages['uncheckAllFilter']}
        onChange={event => onFilterData(property, event.value)}
        optionLabel="type"
        options={
          validations
            ? FiltersUtils.getValidationsOptionTypes(validationsAllTypesFilters, property)
            : FiltersUtils.getOptionTypes(data, property, selectList, ErrorUtils.orderLevelErrors)
        }
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const renderSearchAll = () => (
    <span className={`p-float-label ${styles.dataflowInput}`}>
      <InputText
        className={styles.searchInput}
        id={'searchInput'}
        onChange={event => onSearchData(event.target.value)}
        value={filterState.searchBy}
      />
      {filterState.searchBy && (
        <Button
          className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onSearchData('')}
        />
      )}

      <label
        className={styles.label}
        htmlFor={'searchInput'}
        dangerouslySetInnerHTML={{
          __html: TextUtils.parseText(resources.messages['searchAllLabel'], {
            searchData: !isEmpty(searchBy) ? `(${searchBy.join(', ')})` : ''
          })
        }}></label>
    </span>
  );

  const selectTemplate = option => {
    if (!isNil(option.type)) {
      return (
        <span className={`${styles[option.type.toString().toLowerCase()]} ${styles.statusBox}`}>
          {option.type.toString().toUpperCase()}
        </span>
      );
    }
  };

  return (
    <div className={className ? styles[className] : styles.header}>
      {searchAll && renderSearchAll()}
      {inputOptions && inputOptions.map((option, i) => renderInputFilter(option, i))}
      {selectOptions && selectOptions.map((option, i) => renderSelectFilter(option, i))}
      {dropdownOptions && dropdownOptions.map((option, i) => renderDropdown(option, i))}
      {dateOptions && dateOptions.map((option, i) => renderCalendarFilter(option, i))}
      {matchMode && renderCheckbox()}
      {checkboxOptions && checkboxOptions.map((option, i) => renderCheckboxFilter(option, i))}
      <div className={styles.buttonWrapper} style={{ width: sendData ? 'inherit' : '' }}>
        {sendData ? (
          <Button
            className={`p-button-animated-blink ${styles.sendButton}`}
            icon="filter"
            label={resources.messages['applyFilters']}
            onClick={() => sendData(filterState.filterBy)}
          />
        ) : (
          <Fragment />
        )}

        {(inputOptions || selectOptions || dateOptions || checkboxOptions) && (
          <Button
            className={`${
              sendData ? 'p-button-secondary' : 'p-button-secondary'
            } p-button-rounded  p-button-animated-blink`}
            icon="undo"
            label={resources.messages['reset']}
            onClick={() => onClearAllFilters()}
            style={{ marginLeft: sendData ? '1rem' : '' }}
          />
        )}
      </div>
    </div>
  );
};
