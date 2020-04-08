import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Filters.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { useOnClickOutside } from 'ui/views/_functions/Hooks/useOnClickOutside';

import { FilterUtils } from './_functions/Utils/FilterUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

export const Filters = ({
  data,
  dateOptions,
  dropDownList,
  dropdownOptions,
  filterByList,
  getFiltredData,
  inputOptions,
  selectList,
  selectOptions,
  sendData,
  sortable
}) => {
  const resources = useContext(ResourcesContext);

  const dateRef = useRef(null);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    data: [],
    filterBy: {},
    filteredData: [],
    labelAnimations: {},
    orderBy: {}
  });

  useEffect(() => {
    getInitialState();
  }, [data]);

  useEffect(() => {
    if (getFiltredData) getFiltredData(filterState.filteredData);
  }, [filterState.filteredData]);

  useOnClickOutside(dateRef, () => isEmpty(filterState.filterBy[dateOptions]) && onAnimateLabel([dateOptions], false));

  const getInitialState = () => {
    const initialData = cloneDeep(data);
    const initialFilterBy = FilterUtils.getFilterInitialState(
      data,
      inputOptions,
      selectOptions,
      dateOptions,
      dropdownOptions,
      filterByList
    );
    const initialFilteredData = cloneDeep(data);
    const initialLabelAnimations = FilterUtils.getLabelInitialState(
      inputOptions,
      selectOptions,
      dateOptions,
      dropdownOptions,
      filterState.filterBy
    );
    const initialOrderBy = SortUtils.getOrderInitialState(inputOptions, selectOptions, dateOptions, dropdownOptions);

    filterDispatch({
      type: 'INITIAL_STATE',
      payload: { initialData, initialFilterBy, initialFilteredData, initialLabelAnimations, initialOrderBy }
    });
  };

  const onAnimateLabel = (property, value) => {
    filterDispatch({
      type: 'ANIMATE_LABEL',
      payload: {
        animatedProperty: property,
        isAnimated: value
      }
    });
  };

  const onClearAllFilters = () => {
    filterDispatch({
      type: 'CLEAR_ALL',
      payload: {
        filterBy: FilterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions, dropdownOptions),
        filteredData: cloneDeep(data),
        labelAnimations: FilterUtils.getLabelInitialState(
          inputOptions,
          selectOptions,
          dateOptions,
          dropdownOptions,
          filterState.filterBy
        ),
        orderBy: SortUtils.getOrderInitialState(inputOptions, selectOptions, dateOptions, dropdownOptions)
      }
    });
  };

  const onFilterData = (filter, value) => {
    const inputKeys = FilterUtils.getFilterKeys(filterState, filter, inputOptions);
    const selectedKeys = FilterUtils.getSelectedKeys(filterState, filter, selectOptions);
    const filteredData = FilterUtils.onApplyFilters(
      filter,
      inputKeys,
      filterState,
      selectedKeys,
      value,
      dateOptions,
      selectOptions,
      dropdownOptions
    );

    filterDispatch({ type: 'FILTER_DATA', payload: { filteredData, filter, value } });
  };

  const onOrderData = (order, property) => {
    const sortedData = SortUtils.onSortData([...filterState.data], order, property);
    const filteredSortedData = SortUtils.onSortData([...filterState.filteredData], order, property);
    const orderBy = order === 0 ? -1 : order;
    const resetOrder = SortUtils.onResetOrderData(inputOptions, selectOptions, dateOptions);

    filterDispatch({
      type: 'ORDER_DATA',
      payload: { filteredSortedData, orderBy, property, resetOrder, sortedData }
    });
  };

  const renderCalendarFilter = property => (
    <span className={styles.dataflowInput} ref={dateRef}>
      {renderOrderFilter(property)}
      <span className="p-float-label">
        <Calendar
          dateFormat="yy-mm-dd"
          className={styles.calendarFilter}
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
        <label className={!filterState.labelAnimations[property] ? styles.labelDown : ''} htmlFor={property}>
          {resources.messages[property]}
        </label>
      </span>
    </span>
  );

  const renderDropdown = property => (
    <span className={`${styles.dataflowInput}`}>
      {renderOrderFilter(property)}
      <Dropdown
        className={styles.dropdownFilter}
        filter={FilterUtils.getOptionTypes(data, property, dropDownList).length > 10}
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
        options={FilterUtils.getOptionTypes(data, property, dropDownList)}
        showClear={!isEmpty(filterState.filterBy[property])}
        showFilterClear={true}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const renderInputFilter = property => (
    <span className={styles.dataflowInput}>
      {renderOrderFilter(property)}
      <span className="p-float-label">
        <InputText
          className={styles.inputFilter}
          id={property}
          onChange={event => onFilterData(property, event.target.value)}
          value={filterState.filterBy[property]}
        />
        {filterState.filterBy[property] && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onFilterData(property, '')}
          />
        )}
        <label htmlFor={property}>{resources.messages[property]}</label>
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
      />
    ) : (
      <Fragment />
    );

  const renderSelectFilter = property => (
    <span className={`${styles.dataflowInput}`}>
      {renderOrderFilter(property)}
      <MultiSelect
        checkAllHeader={resources.messages['checkAllFilter']}
        className={styles.multiselectFilter}
        headerClassName={styles.selectHeader}
        id={property}
        inputClassName={`p-float-label ${styles.label}`}
        inputId={property}
        itemTemplate={selectTemplate}
        label={resources.messages[property]}
        notCheckAllHeader={resources.messages['uncheckAllFilter']}
        onChange={event => onFilterData(property, event.value)}
        optionLabel="type"
        options={FilterUtils.getOptionTypes(data, property, selectList)}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const selectTemplate = option => {
    if (!isNil(option.value)) {
      return <span className={`${styles[option.value.toLowerCase()]} ${styles.statusBox}`}>{option.type}</span>;
    }
  };

  return (
    <div className={styles.header}>
      {inputOptions && inputOptions.map(option => renderInputFilter(option))}
      {selectOptions && selectOptions.map(option => renderSelectFilter(option))}
      {dropdownOptions && dropdownOptions.map(option => renderDropdown(option))}
      {dateOptions && dateOptions.map(option => renderCalendarFilter(option))}

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

        {(inputOptions || selectOptions || dateOptions) && (
          <Button
            className={`${
              sendData ? 'p-button-secondary' : 'p-button-secondary'
            } p-button-rounded  p-button-animated-blink`}
            icon="undo"
            onClick={() => onClearAllFilters()}
            label={resources.messages['reset']}
            style={{ marginLeft: sendData ? '1rem' : '' }}
          />
        )}
      </div>
    </div>
  );
};
