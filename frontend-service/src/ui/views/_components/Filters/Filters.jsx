import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

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
  inputOptions,
  matchMode,
  searchAll,
  searchBy = [],
  selectList,
  selectOptions,
  sendData,
  sortable
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const dateRef = useRef(null);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    checkboxes: [],
    data: data,
    filterBy: {},
    filteredData: data,
    labelAnimations: {},
    matchMode: true,
    orderBy: {},
    searchBy: ''
  });

  useEffect(() => {
    getInitialState();
  }, [data]);

  useEffect(() => {
    if (getFilteredData) getFilteredData(filterState.filteredData);
  }, [filterState.filteredData]);

  useEffect(() => {
    onReApplyFilters();
  }, [filterState.matchMode]);

  useOnClickOutside(dateRef, () => isEmpty(filterState.filterBy[dateOptions]) && onAnimateLabel([dateOptions], false));

  const getCheckboxFilterState = property => {
    const [checkBox] = filterState.checkboxes.filter(checkbox => checkbox.property === property);
    return isNil(checkBox) ? false : checkBox.isChecked;
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

  const onChangeCheckboxFilter = property => {
    filterDispatch({ type: 'ON_CHECKBOX_FILTER', payload: { property } });
    filterState.checkboxes.forEach(checkbox => {
      checkbox.property === property && onFilterData(checkbox.property, [checkbox.isChecked]);
    });
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
        checkboxes: FiltersUtils.getCheckboxFilterInitialState(checkboxOptions)
      }
    });
  };

  const onFilterData = (filter, value) => {
    const inputKeys = FiltersUtils.getFilterKeys(filterState, filter, inputOptions);
    const searchedKeys = !isEmpty(searchBy) ? searchBy : ApplyFilterUtils.getSearchKeys(filterState.data);
    const selectedKeys = FiltersUtils.getSelectedKeys(filterState, filter, selectOptions);
    const checkboxKeys = FiltersUtils.getSelectedKeys(filterState, filter, checkboxOptions);
    const filteredData = ApplyFilterUtils.onApplyFilters({
      dateOptions,
      dropdownOptions,
      filter,
      filteredKeys: inputKeys,
      searchedKeys,
      selectedKeys,
      selectOptions,
      checkboxKeys,
      checkboxOptions,
      state: filterState,
      value
    });

    filterDispatch({ type: 'FILTER_DATA', payload: { filteredData, filter, value } });
  };

  const onOrderData = (order, property) => {
    const sortedData = SortUtils.onSortData([...filterState.data], order, property);
    const filteredSortedData = SortUtils.onSortData([...filterState.filteredData], order, property);
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

    filterDispatch({ type: 'ON_SEARCH_DATA', payload: { searchedValues, value } });
  };

  const onToggleMatchMode = () => filterDispatch({ type: 'TOGGLE_MATCH_MODE', payload: !filterState.matchMode });

  const onReApplyFilters = () => {
    const keys = Object.keys(filterState.filterBy);
    for (let index = 0; index < keys.length; index++) {
      const filter = keys[index];
      const value = filterState.filterBy[filter];

      if (!isEmpty(value)) onFilterData(filter, filterState.filterBy[filter]);
    }
  };

  const renderCalendarFilter = (property, i) => (
    <span key={i} className={styles.dataflowInput} ref={dateRef}>
      {renderOrderFilter(property)}
      <span className={`p-float-label ${!sendData ? styles.label : ''}`}>
        <Calendar
          className={styles.calendarFilter}
          dateFormat={userContext.userProps.dateFormat.toLowerCase().replace('yyyy', 'yy')}
          inputClassName={styles.inputFilter}
          inputId={property}
          monthNavigator={true}
          onChange={event => onFilterData(property, event.value)}
          onFocus={() => onAnimateLabel(property, true)}
          readOnlyInput={true}
          selectionMode="range"
          showWeek={true}
          value={filterState.filterBy[property]}
          yearNavigator={true}
          yearRange="2015:2030"
          style={{ zoom: '0.95' }}
        />
        {!isEmpty(filterState.filterBy[property]) && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => {
              onFilterData(property, []);
              onAnimateLabel(property, false);
            }}
          />
        )}
        <label className={!filterState.labelAnimations[property] ? styles.labelDown : styles.label} htmlFor={property}>
          {resources.messages[property]}
        </label>
      </span>
    </span>
  );

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
              // className={styles.checkRequired}
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
        options={FiltersUtils.getOptionTypes(data, property, selectList, ErrorUtils.orderLevelErrors)}
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
