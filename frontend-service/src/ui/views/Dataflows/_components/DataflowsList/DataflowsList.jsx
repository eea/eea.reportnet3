import React, { useContext, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { DataflowsItem } from './_components/DataflowsItem';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowsList = ({ className, content, dataFetch, description, title, type }) => {
  const resources = useContext(ResourcesContext);

  const statusTypes = [
    {
      type: DataflowConf.dataflowStatus['DESIGN'],
      value: DataflowConf.dataflowStatus['DESIGN']
    },
    {
      type: DataflowConf.dataflowStatus['DRAFT'],
      value: DataflowConf.dataflowStatus['DRAFT']
    }
  ];

  const roleTypes = [
    {
      type: DataflowConf.dataflowRoles['DATA_CUSTODIAN'],
      value: DataflowConf.dataflowRoles['DATA_CUSTODIAN']
    },
    {
      type: DataflowConf.dataflowRoles['DATA_PROVIDER'],
      value: DataflowConf.dataflowRoles['DATA_PROVIDER']
    }
  ];

  const dataflowItemInitialState = {
    dataflows: cloneDeep(content),
    filter: {
      name: '',
      description: '',
      status: statusTypes,
      userRole: roleTypes,
      deadlineDate: ''
    },
    filteredDataflows: cloneDeep(content),
    order: { name: 1, description: 1, status: 1, userRole: 1, deadlineDate: 1 }
  };

  const sortData = (data, order, property) => {
    if (order === 1) {
      return data.sort((a, b) => {
        const textA = a[property].toUpperCase();
        const textB = b[property].toUpperCase();
        return textA < textB ? -1 : textA > textB ? 1 : 0;
      });
    } else {
      return data.sort((a, b) => {
        const textA = a[property].toUpperCase();
        const textB = b[property].toUpperCase();
        return textA < textB ? 1 : textA > textB ? -1 : 0;
      });
    }
  };

  const changeFilterValues = (filter, value, data) => {
    dataflowItemDispatch({
      type: 'FILTER_DATAFLOWS',
      payload: { data, filter, value }
    });
  };

  const dataflowItemReducer = (state, { type, payload }) => {
    const getFilterKeys = () =>
      Object.keys(state.filter).filter(key => key !== payload.filter && key !== 'status' && key !== 'userRole');

    const checkFilters = (filteredKeys, dataflow) => {
      for (let i = 0; i < filteredKeys.length; i++) {
        if (state.filter[filteredKeys[i]].toLowerCase() !== '') {
          if (!dataflow[filteredKeys[i]].toLowerCase().includes(state.filter[filteredKeys[i]].toLowerCase())) {
            return false;
          }
        }
      }
      return true;
    };

    switch (type) {
      case 'ORDER_DATAFLOWS':
        return {
          ...state,
          dataflows: sortData([...state.dataflows], payload.order, payload.property),
          filteredDataflows: sortData([...state.filteredDataflows], payload.order, payload.property),
          order: { ...state.order, [payload.property]: -payload.order }
        };

      case 'FILTER_DATAFLOWS':
        const filteredKeys = getFilterKeys();
        return {
          ...state,
          filter: { ...state.filter, [payload.filter]: payload.value },
          filteredDataflows: [
            ...payload.data.filter(data =>
              payload.filter === 'status' || payload.filter === 'userRole'
                ? [...payload.value.map(type => type.value.toLowerCase())].includes(
                    data[payload.filter].toLowerCase()
                  ) && checkFilters(filteredKeys, data)
                : data[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
                  [...state.filter.status.map(status => status.value.toLowerCase())].includes(
                    data.status.toLowerCase()
                  ) &&
                  checkFilters(filteredKeys, data)
            )
          ]
        };

      default:
        return state;
    }
  };

  const [dataflowItemState, dataflowItemDispatch] = useReducer(dataflowItemReducer, dataflowItemInitialState);

  const onOrderData = (order, property) => {
    dataflowItemDispatch({ type: 'ORDER_DATAFLOWS', payload: { order, property } });
  };

  const renderOrderFilter = property => (
    <Button
      className={`p-button-secondary-transparent ${styles.orderIcon}`}
      icon={dataflowItemState.order[property] === 1 ? 'alphabeticOrderUp' : 'alphabeticOrderDown'}
      onClick={() => onOrderData(dataflowItemState.order[property], property)}
      style={{ fontSize: '12pt' }}
      tooltip={resources.messages['orderAlphabetically']}
      tooltipOptions={{ position: 'bottom' }}
    />
  );

  const renderInputFilter = property => (
    <span className={`${styles.dataflowInput} p-float-label`}>
      <InputText
        className={styles.inputFilter}
        id={property}
        onChange={event => changeFilterValues(property, event.target.value, dataflowItemState.dataflows)}
        value={dataflowItemState.filter[property]}
      />
      {dataflowItemState.filter[property] && (
        <Button className={`p-button-secondary-transparent ${styles.clearIcon}`} icon="cancel" />
      )}
      <label htmlFor={property}>{resources.messages[property]}</label>
    </span>
  );

  const renderSelectFilter = (property, optionTypes) => (
    <span className={`${styles.dataflowInput}`}>
      <MultiSelect
        className={styles.multiselectFilter}
        filter={false}
        id={property}
        itemTemplate={selectTemplate}
        onChange={event => changeFilterValues(property, event.value, dataflowItemState.dataflows)}
        optionLabel="type"
        options={optionTypes}
        placeholder={resources.messages['select']}
        style={{ fontSize: '10pt', color: 'var(--floating-label-color)' }}
        value={dataflowItemState.filter[property]}
      />
    </span>
  );

  const selectTemplate = option => (
    <span className={`${styles[option.value.toLowerCase()]} ${styles.statusBox}`}>{option.type}</span>
  );

  const renderCalendarFilter = property => (
    <span className={`${styles.dataflowInput} p-float-label`}>
      <Calendar
        className={styles.inputFilter}
        minDate={new Date()}
        monthNavigator={true}
        onChange={event => changeFilterValues(property, event.value, dataflowItemState.dataflows)}
        selectionMode="multiple"
        showWeek={true}
        value={dataflowItemState.filter[property]}
        yearNavigator={true}
        yearRange="2020:2030"
      />
      <label htmlFor={property}>{resources.messages[property]}</label>
    </span>
  );

  const renderFilters = () => (
    <div className={styles.header}>
      {renderInputFilter('name')}
      {renderOrderFilter('name')}
      {renderInputFilter('description')}
      {renderOrderFilter('description')}
      {renderSelectFilter('status', statusTypes)}
      {renderOrderFilter('status')}
      {renderSelectFilter('userRole', roleTypes)}
      {renderOrderFilter('userRole')}
      {renderCalendarFilter('deadlineDate')}
    </div>
  );

  return (
    <div className={`${styles.wrap} ${className}`}>
      <h2>{title}</h2>
      <p>{description}</p>
      {renderFilters()}
      {dataflowItemState.filteredDataflows.map(dataflow => (
        <DataflowsItem dataFetch={dataFetch} itemContent={dataflow} key={dataflow.id} type={type} />
      ))}
    </div>
  );
};

export { DataflowsList };
