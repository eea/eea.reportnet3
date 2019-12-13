import React, { useContext, useEffect, useState, useReducer } from 'react';

import { includes, isUndefined, isEmpty } from 'lodash';

import styles from './DataProvidersList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const reducer = (state, { type, payload }) => {
  const emptyField = { dataProviderId: '', email: '', name: '' };
  let newState = {};
  switch (type) {
    case 'INITIAL_LOAD':
      if (!includes(state.dataProviders, emptyField)) {
        payload.dataProviders.push(emptyField);
      }

      newState = {
        ...state,
        dataProviders: payload.dataProviders,
        selectedRepresentativeType: payload.representativesOf
      };
      return newState;

    case 'SELECT_REPRESENTATIVE_TYPE':
      console.log('SELECT_REPRESENTATIVE_TYPE', payload);
      return {
        ...state,
        selectedRepresentativeType: payload
      };

    case 'ON_LOGIN_CHANGE':
      console.log('ON_LOGIN_CHANGE', payload);
      return {
        ...state,
        input: payload
      };

    /* case 'ADD_DATAPROVIDER':
      newState = { ...state, name: payload.name, email: payload.email };

      onDataProviderAdd(newState.email, newState.name);

      return newState;

    case 'DELETE_DATAPROVIDER':
      newState = { ...state, name: '', dataProviderId: payload };

      onDataProviderDelete(newState.dataProviderId);

      return newState;
*/
    default:
      return state;
  }
};

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  const [posibleDataProvidersList, setPosibleDataProvidersList] = useState([]);
  const [representativesTypesList, setRepresentativesTypesList] = useState([]);
  const [selectedDataProvider, setSelectedDataProvider] = useState(null);

  const initialState = {
    dataProviders: [],
    selectedRepresentativeType: null
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  /*   const onPageLoad = async () => {
    const loadedData = await DataProviderService.all(dataflowId);

    return loadedData;
  }; */
  const onPageLoad = cb => {
    const loadedData = {
      representativesOf: 'countries',
      dataProviders: [
        { dataProviderId: '1111', email: 'spain@es.es', name: 'Es' },
        { dataProviderId: '2222', email: 'germany@de.de', name: 'De' },
        { dataProviderId: '3333', email: 'greatbr@uk.uk', name: 'UK' },
        { dataProviderId: '4444', email: 'france@fr.fr', name: 'Fr' },
        { dataProviderId: '5555', email: 'italy@it.it', name: 'It' }
      ]
    };

    return cb(null, loadedData);
  };

  useEffect(() => {
    console.log('INIT_USEEFFECT');

    onPageLoad((error, success) => {
      if (!error) {
        formDispatcher({
          type: 'INITIAL_LOAD',
          payload: success
        });
      }
    });
  }, []);

  useEffect(() => {
    //Need get function on api for that list
    // Http requester......
    setRepresentativesTypesList([
      { nameLabel: 'Countries', name: 'countries' },
      { nameLabel: 'Companies', name: 'companies' }
    ]);
  }, []);

  useEffect(() => {
    setPosibleDataProvidersList([
      { nameLabel: 'Select...', name: '' },
      { nameLabel: 'Spain', name: 'Es' },
      { nameLabel: 'Germany', name: 'De' },
      { nameLabel: 'Uk', name: 'UK' },
      { nameLabel: 'France', name: 'Fr' },
      { nameLabel: 'Italy', name: 'It' }
    ]);
  }, []);
  /* 
  //EliminaMe
  useEffect(() => {
    console.log('representativesTypesList', representativesTypesList);
  }, [representativesTypesList]);

  //EliminaMe
  useEffect(() => {
    console.log('posibleDataProvidersList', posibleDataProvidersList);
  }, [posibleDataProvidersList]);

  //EliminaMe
  useEffect(() => {
    console.log('selectedDataProvider', selectedDataProvider);
  }, [selectedDataProvider]);
 */
  const onDataProviderAdd = async (email, name) => {
    await DataProviderService.add(dataflowId, email, name);
  };

  const onDataProviderDelete = async dataProviderId => {
    await DataProviderService.deleteById(dataflowId, dataProviderId);
  };

  const getDataProvidersListOfSelectedType = async type => {
    return await DataProviderService.allRepresentativesOf(type);
  };

  const emailInputColumnTemplate = rowData => {
    /*    console.log('emailInputColumnTemplate', rowData); */
    return (
      <input
        value={rowData.email}
        placeholder={'Data Providers email...'}
        onChange={e => formDispatcher({ type: 'ON_LOGIN_CHANGE', payload: e.target.value })}
      />
    );
  };

  const nameDropdownColumnTemplate = rowData => {
    return (
      <>
        <select
          className="p-dropdown p-component"
          onChange={e => {
            setSelectedDataProvider(e.target.value);
          }}>
          {posibleDataProvidersList.map(provider => {
            return (
              <option
                key={provider.name}
                className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
                value={provider.name}>
                {provider.nameLabel}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = rowData => {
    return rowData.dataProviderId !== '' ? (
      <Button
        tooltip={resources.messages.deleteDataProvider}
        tooltipOptions={{ position: 'right' }}
        icon="trash"
        disabled={false}
        className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
        onClick={() => {
          formDispatcher({ type: 'DELETE_DATAPROVIDER', payload: rowData.id });
        }}
      />
    ) : (
      <></>
    );
  };

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>Data providers</div>

        <div>
          <label htmlFor="selectedDataProvider">Representative of </label>

          <Dropdown
            name="selectedDataProvider"
            optionLabel="nameLabel"
            placeholder="Select..."
            value={formState.selectedRepresentativeType}
            options={representativesTypesList}
            onChange={e => formDispatcher({ type: 'SELECT_REPRESENTATIVE_TYPE', payload: e.target.value })}
          />
        </div>
      </div>

      <DataTable
        value={!isUndefined(formState) ? formState.dataProviders : []}
        paginator={false}
        scrollable={true}
        scrollHeight="100vh">
        <Column body={emailInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
    </>
  );
};

export { DataProvidersList };
