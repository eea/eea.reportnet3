import React, { useContext, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { DataflowsItem } from './_components/DataflowsItem';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'primereact/multiselect';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowsList = ({ className, content, dataFetch, description, title, type }) => {
  const resources = useContext(ResourcesContext);

  const dataflowItemInitialState = {
    dataflows: cloneDeep(content),
    filter: {
      name: '',
      description: '',
      status: [
        {
          type: DataflowConf.dataflowStatus['DESIGN'],
          value: DataflowConf.dataflowStatus['DESIGN']
        },
        {
          type: DataflowConf.dataflowStatus['DRAFT'],
          value: DataflowConf.dataflowStatus['DRAFT']
        }
      ],
      role: [
        {
          type: DataflowConf.dataflowRoles['DATA_CUSTODIAN'],
          value: DataflowConf.dataflowRoles['DATA_CUSTODIAN']
        },
        {
          type: DataflowConf.dataflowRoles['DATA_PROVIDER'],
          value: DataflowConf.dataflowRoles['DATA_PROVIDER']
        }
      ]
    },
    isKeyFiltered: false,
    filteredDataflows: cloneDeep(content),
    order: { name: 1, description: 1, status: 1, role: 1 }
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
    const getFilterKeys = () => Object.keys(state.filter).filter(key => key !== payload.filter && key !== 'status');

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
        return {
          ...state,
          isKeyFiltered: true,
          filter: { ...state.filter, [payload.filter]: payload.value },
          filteredDataflows: [
            ...payload.data.filter(data =>
              payload.filter === 'status'
                ? [...payload.value.map(status => status.value.toLowerCase())].includes(
                    data.status.toLowerCase() && checkFilters(getFilterKeys, data)
                  )
                : data[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
                  [...state.filter.status.map(status => status.value.toLowerCase())].includes(
                    data.status.toLowerCase()
                  ) &&
                  checkFilters(getFilterKeys, data)
            )
          ]
        };

      default:
        return state;
    }
  };

  const [dataflowItemState, dataflowItemDispatch] = useReducer(dataflowItemReducer, dataflowItemInitialState);

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

  const onOrderData = (order, property) => {
    dataflowItemDispatch({ type: 'ORDER_DATAFLOWS', payload: { order, property } });
  };

  const renderFilterOrder = property => (
    <Button
      className={`p-button-secondary-transparent ${styles.orderIcon}`}
      icon={dataflowItemState.order[property] === 1 ? 'alphabeticOrderUp' : 'alphabeticOrderDown'}
      onClick={() => onOrderData(dataflowItemState.order[property], property)}
      style={{ fontSize: '12pt' }}
      tooltip={resources.messages['orderAlphabetically']}
      tooltipOptions={{ position: 'bottom' }}
    />
  );

  const statusTemplate = option => (
    <span className={`${styles[option.value.toLowerCase()]} ${styles.statusBox}`}>{option.type}</span>
  );

  const renderFilters = () => (
    <div className={styles.header}>
      <span className={`${styles.dataflowInput} p-float-label`}>
        <InputText
          className={styles.inputFilter}
          id={'filterNameInput'}
          onChange={e => changeFilterValues('name', e.target.value, dataflowItemState.dataflows)}
          value={dataflowItemState.filter.name}
        />
        <label htmlFor={'filterNameInput'}>{resources.messages['codelistName']}</label>
      </span>
      {renderFilterOrder('name')}
      <span className={`${styles.dataflowInput} p-float-label`}>
        <InputText
          className={styles.inputFilter}
          id={'filterDescriptionInput'}
          // onChange={e => changeFilterValues('name', e.target.value, dataflowItemState.dataflows)}
          value={dataflowItemState.filter.description}
        />
        <label htmlFor={'filterDescriptionInput'}>{resources.messages['codelistDescription']}</label>
      </span>
      {renderFilterOrder('description')}
      <span className={`${styles.dataflowInput}`}>
        <MultiSelect
          className={styles.multiselectFilter}
          filter={false}
          itemTemplate={statusTemplate}
          onChange={event => changeFilterValues('status', event.value, dataflowItemState.dataflows)}
          optionLabel="type"
          options={statusTypes}
          placeholder={resources.messages['ok']}
          style={{ fontSize: '10pt', color: 'var(--floating-label-color)' }}
          value={dataflowItemState.filter.status}
        />
      </span>
      {renderFilterOrder('status')}
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
