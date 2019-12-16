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
      return {
        ...state,
        selectedRepresentativeType: payload
      };

    case 'ON_LOGIN_CHANGE':
      const updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.email = payload.input;
          return dataProvider;
        } else {
          return dataProvider;
        }
      });
      return {
        ...state,
        dataProviders: updatedList
      };
    case 'DELETE_DATAPROVIDER':
      console.log('Delete Provider with id :', payload.dataProviderId);

      /* onDataProviderDelete(payload.dataProviderId); */

      return state;
    /* case 'ADD_DATAPROVIDER':
      newState = { ...state, name: payload.name, email: payload.email };

      onDataProviderAdd(newState.email, newState.name);

      return newState;

    
*/
    default:
      return state;
  }
};

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  const [possibleDataProvidersList, setPosibleDataProvidersList] = useState([]);
  const [representativesTypesList, setRepresentativesTypesList] = useState([]);
  const [dataProviderIdToDelete, setDataProviderIdToDelete] = useState();
  const [confirmDeleteVisible, setConfirmDeleteVisible] = useState(false);
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
    setPosibleDataProvidersList([
      { nameLabel: 'Select...', name: '' },
      { nameLabel: 'Spain', name: 'Es' },
      { nameLabel: 'Germany', name: 'De' },
      { nameLabel: 'Uk', name: 'UK' },
      { nameLabel: 'France', name: 'Fr' },
      { nameLabel: 'Italy', name: 'It' }
    ]);
  }, []);

  const onDataProviderAdd = async (email, name) => {
    await DataProviderService.add(dataflowId, email, name);
  };

  const onDeleteBtnPressed = dataProviderId => {
    setConfirmDeleteVisible(true);
    setDataProviderIdToDelete(dataProviderId);
  };

  const onHideConfirmDeleteDialog = () => {
    setConfirmDeleteVisible(false);
  };

  const onDeleteDataProvider = async dataProviderId => {
    console.log('deleting ', dataProviderId);
    /* await DataProviderService.delete(dataProviderId); */
  };

  const onConfirmDeleteDataProvider = () => {
    onDeleteDataProvider(dataProviderIdToDelete);
    onHideConfirmDeleteDialog();
  };
  const getDataProvidersListOfSelectedType = async type => {
    return await DataProviderService.allRepresentativesOf(type);
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
            type: 'ON_LOGIN_CHANGE',
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
            setSelectedDataProvider(e.target.value);
          }}>
          {possibleDataProvidersList.map(provider => {
            let selected = provider.name === rowData.name ? 'selected' : '';
            return (
              <option
                key={provider.name}
                className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
                selected={selected}
                value={rowData.name}>
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
          onDeleteBtnPressed(rowData.dataProviderId);
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
            defaultValue={formState.selectedRepresentativeType}
            value={formState.selectedRepresentativeType}
            options={representativesTypesList}
            onChange={e => formDispatcher({ type: 'SELECT_REPRESENTATIVE_TYPE', payload: e.target.value })}
          />
        </div>
      </div>

      <DataTable value={formState.dataProviders} paginator={false} scrollable={true} scrollHeight="100vh">
        <Column body={emailInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
      <ConfirmDialog
        onConfirm={onConfirmDeleteDataProvider}
        onHide={onHideConfirmDeleteDialog}
        visible={confirmDeleteVisible}
        header={'Delete data provider'}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {'Do you realy want to delete this data provider?'}
      </ConfirmDialog>
    </>
  );
};

export { DataProvidersList };
