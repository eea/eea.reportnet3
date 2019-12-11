import React, { useContext, useEffect, useState, useReducer } from 'react';

import { isEmpty } from 'lodash';

import styles from './DataProvidersList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  const [dataProviders, setDataProviders] = useState([]);
  const [representatives, setRepresentatives] = useState([]);
  const [representativesTypesList, setRepresentativesTypesList] = useState([]);
  const [selectedDataProvidersType, setSelectedDataProvidersType] = useState(null);

  useEffect(() => {
    loadDataProvidersList();
  }, []);

  useEffect(() => {
    setRepresentatives([
      { nameLabel: 'Spain', name: 'Es' },
      { nameLabel: 'Germany', name: 'De' }
    ]);
    //Need get function on api for that list
    // Http requester......
    setRepresentativesTypesList([
      { nameLabel: 'Countries', name: 'countries' },
      { nameLabel: 'Companies', name: 'companies' }
    ]);
  }, []);

  const onSelectProvidersType = e => {
    setSelectedDataProvidersType(e.value);
  };

  const loadDataProvidersList = async () => {
    setDataProviders(await DataProviderService.all(dataflowId));
  };

  const onDataProviderAdd = async (email, name) => {
    await DataProviderService.add(dataflowId, email, name);
  };

  const onDataProviderDelete = async dataProviderId => {
    await DataProviderService.deleteById(dataflowId, dataProviderId);
  };

  const initialState = { name: '', email: '', dataProviderId: '' };

  const nameReducer = (state, action) => {
    let newState;
    switch (action.type) {
      case 'ADD_DATAPROVIDER':
        newState = { ...state, name: action.payload.name, email: action.payload.email };

        onDataProviderAdd(newState.email, newState.name);

        return newState;

      case 'DELETE_DATAPROVIDER':
        newState = { ...state, name: '', dataProviderId: action.payload };

        onDataProviderDelete(newState.dataProviderId);

        return newState;

      default:
        return state;
    }
  };
  const [dataProviderState, dataProviderDispatcher] = useReducer(nameReducer, initialState);

  useEffect(() => {
    loadDataProvidersList();
  }, [dataProviderState]);

  const getDataProvidersListOfSelectedType = async type => {
    return await DataProviderService.allRepresentativesOf(type);
  };

  const emailInputColumnTemplate = rowData => {
    return (
      <input
        value={rowData.email}
        placeholder={'Data Providers email...'}
        onChange={e => {
          /* dataProviderDispatcher({ type: `UPDATE_TO_${e.value.name}`.toUpperCase(), payload: rowData.id }); */
        }}
      />
    );
  };

  const nameDropdownColumnTemplate = rowData => {
    if (selectedDataProvidersType !== null) {
      getDataProvidersListOfSelectedType(selectedDataProvidersType).then(result => result);
    }

    const getActualName = () => {
      if (rowData) {
        switch (rowData.name) {
          case 'Es':
            return { nameLabel: 'Spain', name: 'Es' };

          case 'It':
            return { nameLabel: 'Italy', name: 'It' };

          case 'Fr':
            return { nameLabel: 'France', name: 'Fr' };

          case 'UK':
            return { nameLabel: 'Britain', name: 'UK' };

          default:
            return { nameLabel: 'Choose one...', name: '' };
        }
      }
    };

    return (
      <>
        {/* <Dropdown
          // appendTo={document.body}
          optionLabel="nameLabel"
          options={representatives}
          placeholder={'select'}
          onChange={e => {
            // dataProviderDispatcher({ type: `UPDATE_TO_${e.value.name}`.toUpperCase(), payload: rowData.id });
          }}
        /> */}
        <select
          className="p-dropdown p-component"
          required
          onChange={e => {
            console.log('provider', e.target.value);
            // dataProviderDispatcher({ type: `UPDATE_TO_${e.value.name}`.toUpperCase(), payload: rowData.id });
          }}>
          {representatives.map(provider => (
            <option
              key={provider.name}
              className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
              value={provider.name}>
              {provider.nameLabel}
            </option>
          ))}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = rowData => {
    return (
      <Button
        tooltip={resources.messages.deleteDataProvider}
        tooltipOptions={{ position: 'right' }}
        icon="trash"
        disabled={false}
        className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
        onClick={e => {
          dataProviderDispatcher({ type: 'DELETE_DATAPROVIDER', payload: rowData.id });
        }}
      />
    );
  };

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>Data providers</div>

        <div>
          <label htmlFor="selectedDataProvidersType">Representative of </label>

          <Dropdown
            name="selectedDataProvidersType"
            optionLabel="nameLabel"
            placeholder="Select..."
            value={selectedDataProvidersType}
            options={representativesTypesList}
            onChange={onSelectProvidersType}
          />
        </div>
      </div>

      <DataTable value={dataProviders} paginator={false} scrollable={true} scrollHeight="60vh">
        <Column body={emailInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
    </>
  );
};

export { DataProvidersList };
