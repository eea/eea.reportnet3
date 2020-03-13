import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';

import styles from './Filters.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { filterReducer } from './_functions/Reducers/filterReducer';

import { filterUtils } from './_functions/Utils/filterUtils';

export const Filters = ({ data, dateOptions, getFiltredData, inputOptions, selectOptions }) => {
  const resources = useContext(ResourcesContext);

  const [filterState, filterDispatch] = useReducer(filterReducer, {
    data: cloneDeep(data),
    filterBy: filterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions),
    filteredData: cloneDeep(data),
    orderBy: filterUtils.getOrderInitialState(inputOptions, selectOptions, dateOptions)
  });

  useEffect(() => {
    getFiltredData(filterState.filteredData);
  }, [filterState.filteredData]);

  const changeFilterValues = (filter, value, data) => {
    filterDispatch({
      type: 'FILTER_DATA',
      payload: { data, filter, value }
    });
  };

  const onCancelButton = property => {
    filterDispatch({ type: 'CLEAR_INPUT', payload: { property } });
  };

  const onOrderData = (order, property) => {
    filterDispatch({ type: 'ORDER_DATA', payload: { order, property } });
  };

  const onClearAllFilters = () => {
    filterDispatch({
      type: 'CLEAR_ALL_FILTERS',
      payload: filterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions)
    });
  };

  const renderCalendarFilter = property => (
    <span className={`${styles.dataflowInput} p-float-label`}>
      <Calendar
        className={styles.calendarFilter}
        minDate={new Date()}
        monthNavigator={true}
        onChange={event => changeFilterValues(property, event.value, filterState.data)}
        selectionMode="multiple"
        showWeek={true}
        value={filterState.filterBy[property]}
        yearNavigator={true}
        yearRange="2020:2030"
      />
      <label htmlFor={property}>{resources.messages[property]}</label>
    </span>
  );

  const renderInputFilter = property => (
    <span className={`${styles.dataflowInput} p-float-label`}>
      <InputText
        className={styles.inputFilter}
        disabled={property.includes('ROD3')}
        id={property}
        onChange={event => changeFilterValues(property, event.target.value, filterState.data)}
        value={filterState.filterBy[property]}
      />
      {filterState.filterBy[property] && (
        <Button
          className={`p-button-secondary-transparent ${styles.clearIcon}`}
          icon="cancel"
          onClick={() => onCancelButton(property)}
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
        onChange={event => changeFilterValues(property, event.value, filterState.data)}
        optionLabel="type"
        options={filterUtils.getOptionTypes(data, property)}
        placeholder={resources.messages['select']}
        style={{ fontSize: '10pt', color: 'var(--floating-label-color)' }}
        value={filterState.filterBy[property]}
      />
    </span>
  );

  const selectTemplate = option => (
    <span className={`${styles[option.value.toLowerCase()]} ${styles.statusBox}`}>{option.type}</span>
  );

  return (
    <div className={styles.header}>
      {inputOptions.map(option => (
        <Fragment>
          {renderInputFilter(option)}
          {renderOrderFilter(option)}
        </Fragment>
      ))}
      {selectOptions.map(option => (
        <Fragment>
          {renderSelectFilter(option)}
          {renderOrderFilter(option)}
        </Fragment>
      ))}
      {dateOptions.map(option => (
        <Fragment>
          {renderCalendarFilter(option)}
          {renderOrderFilter(option)}
        </Fragment>
      ))}
      <Button
        className={`p-button-rounded p-button-secondary p-button-animated-blink`}
        icon="trash"
        onClick={() => onClearAllFilters()}
        tooltip="clear filters"
      />
    </div>
  );
};
