import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Filters.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

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
      type: 'CLEAR_ALL',
      payload: {
        filterBy: FilterUtils.getFilterInitialState(data, inputOptions, selectOptions, dateOptions),
        filteredData: cloneDeep(data),
        orderBy: SortUtils.getOrderInitialState(inputOptions, selectOptions, dateOptions)
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
    const orderBy = order === 0 ? -1 : order;

    filterDispatch({ type: 'ORDER_DATA', payload: { filteredSortedData, orderBy, property, sortedData } });
  };

  const changeLabelClass = () => {
    document.getElementById('dateLabel').className = styles.dateLabelUp;
  };

  const renderCalendarFilter = property => {
    const minDate = FilterUtils.getYesterdayDate();
    return (
      <span className={`p-float-label ${styles.dataflowInput} `}>
        <Calendar
          className={styles.calendarFilter}
          disabledDates={[minDate]}
          inputClassName={styles.inputFilter}
          inputId={property}
          minDate={minDate}
          monthNavigator={true}
          onChange={event => onFilterData(property, event.value)}
          onFocus={() => changeLabelClass()}
          readOnlyInput={true}
          selectionMode="range"
          showWeek={true}
          value={filterState.filterBy[property]}
          yearNavigator={true}
          yearRange="2020:2030"
        />
        {!isEmpty(filterState.filterBy[property]) && (
          <Button
            className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
            icon="cancel"
            onClick={() => onFilterData(property, [])}
          />
        )}
        <label id="dateLabel" className={styles.dateLabel} htmlFor={property}>
          {resources.messages[property]}
        </label>
      </span>
    );
  };

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
          className={`p-button-secondary-transparent ${styles.icon} ${styles.cancelIcon}`}
          icon="cancel"
          onClick={() => onFilterData(property, '')}
        />
      )}
      <label htmlFor={property}>{resources.messages[property]}</label>
    </span>
  );

  const renderOrderFilter = property => (
    <Button
      className={`p-button-secondary-transparent ${styles.icon}`}
      disabled={property.includes('ROD3')}
      icon={SortUtils.getOrderIcon(filterState.orderBy[property])}
      id={`${property}_sort`}
      onClick={() => onOrderData(filterState.orderBy[property], property)}
      style={{ fontSize: '12pt' }}
      tooltip={resources.messages['sort']}
      tooltipOptions={{ position: 'bottom' }}
    />
  );

  const renderSelectFilter = property => (
    <span className={`${styles.dataflowInput}`}>
      <MultiSelect
        className={styles.multiselectFilter}
        id={property}
        itemTemplate={selectTemplate}
        onChange={event => onFilterData(property, event.value)}
        optionLabel="type"
        options={FilterUtils.getOptionTypes(data, property)}
        placeholder={resources.messages[property]}
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
      {inputOptions &&
        inputOptions.map(option => (
          <Fragment>
            {renderOrderFilter(option)}
            {renderInputFilter(option)}
          </Fragment>
        ))}
      {selectOptions &&
        selectOptions.map(option => (
          <Fragment>
            {renderOrderFilter(option)}
            {renderSelectFilter(option)}
          </Fragment>
        ))}
      {dateOptions &&
        dateOptions.map(option => (
          <Fragment>
            {renderOrderFilter(option)}
            {renderCalendarFilter(option)}
          </Fragment>
        ))}
      {(inputOptions || selectOptions || dateOptions) && (
        <Button
          className={`p-button-rounded p-button-secondary p-button-animated-blink ${styles.cancelFilters}`}
          icon="cancel"
          onClick={() => onClearAllFilters()}
          tooltip={resources.messages['clearFilters']}
          tooltipOptions={{ position: 'left' }}
        />
      )}
    </div>
  );
};
