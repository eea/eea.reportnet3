import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';

import styles from './Filters.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { FilterUtils } from './_functions/Utils/FilterUtils';
import { SortUtils } from './_functions/Utils/SortUtils';

export const Filters = ({ data, dateOptions, getFiltredData, inputOptions, selectOptions }) => {
  const resources = useContext(ResourcesContext);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    data: cloneDeep(data),
    dateOptions: dateOptions,
    filterBy: FilterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions),
    filteredData: cloneDeep(data),
    inputOptions: inputOptions,
    orderBy: SortUtils.getOrderInitialState(inputOptions, selectOptions, dateOptions),
    selectOptions: selectOptions
  });

  useEffect(() => {
    if (getFiltredData) {
      getFiltredData(filterState.filteredData);
    }
  }, [filterState.filteredData]);

  const onClearAllFilters = () => {
    filterDispatch({
      type: 'CLEAR_ALL_FILTERS',
      payload: {
        filterBy: FilterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions),
        filteredData: cloneDeep(data)
      }
    });
  };

  const onFilterData = (filter, value) => {
    const filteredKeys = FilterUtils.getFilterKeys(filterState, filter);
    const selectedKeys = FilterUtils.getSelectedKeys(filterState, filter);
    const filteredData = FilterUtils.onApplyFilters(filter, filteredKeys, filterState, selectedKeys, value);

    filterDispatch({ type: 'FILTER_DATA', payload: { filteredData, filter, value } });
  };

  const onOrderData = (order, property) => {
    const sortedData = SortUtils.onSortData([...filterState.data], order, property);
    const filteredSortedData = SortUtils.onSortData([...filterState.filteredData], order, property);

    filterDispatch({ type: 'ORDER_DATA', payload: { filteredSortedData, order, property, sortedData } });
  };

  const renderCalendarFilter = property => (
    <span className={`p-float-label ${styles.dataflowInputDate} `}>
      <Calendar
        className={styles.calendarFilter}
        inputClassName={styles.calendarFilterBorder}
        inputId={property}
        minDate={new Date()}
        monthNavigator={true}
        onChange={event => onFilterData(property, event.value)}
        placeholder={property}
        selectionMode="range"
        showWeek={true}
        value={filterState.filterBy[property]}
        yearNavigator={true}
        yearRange="2020:2030"
      />
      <label className={styles.datePlaceholder} htmlFor={property}>
        {resources.messages[property]}
      </label>
    </span>
  );

  const renderInputFilter = property => (
    <span className={`${styles.dataflowInput}  p-float-label`}>
      <InputText
        className={styles.inputFilter}
        disabled={property.includes('ROD3')}
        id={property}
        onChange={event => onFilterData(property, event.target.value)}
        value={filterState.filterBy[property]}
      />
      {filterState.filterBy[property] && (
        <Button
          className={`p-button-secondary-transparent ${styles.orderIcon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onFilterData(property, '')}
        />
      )}
      <label htmlFor={property}>{resources.messages[property]}</label>
    </span>
  );

  const renderOrderFilter = property => (
    <Button
      className={`p-button-secondary-transparent ${styles.orderIcon}`}
      disabled={property.includes('ROD3')}
      icon={filterState.orderBy[property] === 1 ? 'alphabeticOrderUp' : 'alphabeticOrderDown'}
      id={`${property}_sort`}
      onClick={() => onOrderData(filterState.orderBy[property], property)}
      style={{ fontSize: '12pt' }}
      tooltip={resources.messages['orderAlphabetically']}
      tooltipOptions={{ position: 'bottom' }}
    />
  );

  const renderSelectFilter = property => (
    <span className={`${styles.dataflowInput}`}>
      <MultiSelect
        className={styles.multiselectFilter}
        filter={false}
        id={property}
        itemTemplate={selectTemplate}
        onChange={event => onFilterData(property, event.value, filterState.data)}
        optionLabel="type"
        options={FilterUtils.getOptionTypes(data, property)}
        placeholder={resources.messages['select']}
        //style={{ fontSize: '9pt' }}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const selectTemplate = option => (
    <span className={`${styles[option.value.toLowerCase()]} ${styles.statusBox}`}>{option.type}</span>
  );

  return (
    <div className={styles.header}>
      {inputOptions &&
        inputOptions.map(option => (
          <Fragment>
            {renderInputFilter(option)}
            {renderOrderFilter(option)}
          </Fragment>
        ))}
      {selectOptions &&
        selectOptions.map(option => (
          <Fragment>
            {renderSelectFilter(option)}
            {renderOrderFilter(option)}
          </Fragment>
        ))}
      {dateOptions &&
        dateOptions.map(option => (
          <Fragment>
            {renderCalendarFilter(option)}
            {renderOrderFilter(option)}
          </Fragment>
        ))}
      {(inputOptions || selectOptions || dateOptions) && (
        <Button
          className={`p-button-rounded p-button-secondary p-button-animated-blink ${styles.drashInput}`}
          icon="trash"
          onClick={() => onClearAllFilters()}
          tooltip={resources.messages['clearFilters']}
        />
      )}
    </div>
  );
};
