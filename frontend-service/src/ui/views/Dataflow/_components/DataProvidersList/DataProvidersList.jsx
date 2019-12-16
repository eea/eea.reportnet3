import React, { useContext, useEffect, useState, useReducer } from 'react';

import { includes } from 'lodash';

import styles from './DataProvidersList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const reducer = (state, { type, payload }) => {
  const emptyField = { dataProviderId: '', email: '', name: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATAPROVIDER':
      // await DataProviderService.add(dataflowId, email, name);
      return state;
    case 'INITIAL_LOAD':
      if (!includes(state.dataProviders, emptyField)) {
        payload.dataProviders.push(emptyField);
      }

      return {
        ...state,
        dataProviders: payload.dataProviders,
        selectedRepresentativeType: payload.representativesOf
      };

    case 'SELECT_REPRESENTATIVE_TYPE':
      console.log('SELECT_REPRESENTATIVE_TYPE ', payload);
      return {
        ...state,
        selectedRepresentativeType: payload
      };

    case 'ON_EMAIL_CHANGE':
      updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.email = payload.input;
        }
        return dataProvider;
      });

      return {
        ...state,
        dataProviders: updatedList
      };

    case 'ON_PROVIDER_CHANGE':
      updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.name = payload.name;
        }
        return dataProvider;
      });

      return {
        ...state,
        dataProviders: updatedList
      };

    case 'DELETE_DATAPROVIDER':
      console.log('Delete Provider with id :', state.dataProviderIdToDelete);

      /* await DataProviderService.delete(dataProviderId); */

      return {
        ...state,
        confirmDeleteVisible: false
      };

    case 'SHOW_CONFIRM_DIALOG':
      return {
        ...state,
        confirmDeleteVisible: true,
        dataProviderIdToDelete: payload.dataProviderId
      };
    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        confirmDeleteVisible: false,
        dataProviderIdToDelete: ''
      };

    default:
      return state;
  }
};

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  // const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
  const [dataProviderIdToDelete, setDataProviderIdToDelete] = useState();
  const [possibleDataProvidersList, setPossibleDataProvidersList] = useState([]);
  const [representativesTypesList, setRepresentativesTypesList] = useState([]);
  const [selectedDataProvider, setSelectedDataProvider] = useState(null);
  const [selectedRepresentativeType, setSelectedRepresentativeType] = useState(null);

  const initialState = {
    confirmDeleteVisible: false,
    dataProviderIdToDelete: '',
    dataProviders: [],
    possibleDataProvidersList: [],
    representativesTypesList: [],
    selectedDataProvider: null,
    selectedRepresentativeType: ''
  };

  useEffect(() => {
    console.log('selectedRepresentativeType', selectedRepresentativeType);
  }, [selectedRepresentativeType]);

  useEffect(() => {
    console.log('selectedDataProvider', selectedDataProvider);
  }, [selectedDataProvider]);

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  /*   const onPageLoad = async () => {
    const loadedData = await DataProviderService.all(dataflowId);

    return loadedData;
  }; */
  const onPageLoad = () => {
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

    return loadedData;
  };

  useEffect(() => {
    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: onPageLoad()
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
    getDataProvidersListOfSelectedType(selectedRepresentativeType);
  }, []);

  const onDataProviderAdd = async (email, name) => {
    await DataProviderService.add(dataflowId, email, name);
  };

  const onConfirmDeleteDataProvider = () => {
    /* onDeleteDataProvider(dataProviderIdToDelete); */
    formDispatcher({
      type: 'DELETE_DATAPROVIDER',
      payload: { dataProviderId: dataProviderIdToDelete }
    });
  };
  const getDataProvidersListOfSelectedType = async type => {
    // const dataResponse = await DataProviderService.allRepresentativesOf(type);
    const dataResponse = [
      { nameLabel: 'Select...', name: '' },
      { nameLabel: 'Spain', name: 'Es' },
      { nameLabel: 'Germany', name: 'De' },
      { nameLabel: 'Uk', name: 'UK' },
      { nameLabel: 'France', name: 'Fr' },
      { nameLabel: 'Italy', name: 'It' }
    ];
    setPossibleDataProvidersList(dataResponse);
  };

  const emailInputColumnTemplate = rowData => {
    let inputData = rowData.email;
    return (
      <input
        defaultValue={inputData}
        value={formState.dat}
        placeholder={'Data Providers email...'}
        onChange={e =>
          formDispatcher({
            type: 'ON_EMAIL_CHANGE',
            payload: { input: e.target.value, dataProviderId: rowData.dataProviderId }
          })
        }
      />
    );
  };

  const nameDropdownColumnTemplate = rowData => {
    return (
      <>
        <select
          className="p-dropdown p-component"
          onChange={e => {
            formDispatcher({
              type: 'ON_PROVIDER_CHANGE',
              payload: { name: e.target.value, dataProviderId: rowData.dataProviderId }
            });
          }}
          value={rowData.name}>
          {possibleDataProvidersList.map(provider => {
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
          formDispatcher({ type: 'SHOW_CONFIRM_DIALOG', payload: { dataProviderId: rowData.dataProviderId } });
        }}
      />
    ) : (
      <></>
    );
  };

  console.log('formState.selectedRepresentativeType', representativesTypesList);
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
            onChange={e => {
              setSelectedRepresentativeType(e.target.value);
              return formDispatcher({ type: 'SELECT_REPRESENTATIVE_TYPE', payload: e.target.value });
            }}
          />
        </div>
      </div>

      <DataTable value={formState.dataProviders} paginator={false} scrollable={true} scrollHeight="100vh">
        <Column body={emailInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
      <ConfirmDialog
        onConfirm={() => onConfirmDeleteDataProvider()}
        onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
        visible={formState.confirmDeleteVisible}
        header={'Delete data provider'}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {'Do you really want to delete this data provider?'}
      </ConfirmDialog>
    </>
  );
};

export { DataProvidersList };
