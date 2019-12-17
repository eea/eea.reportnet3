import React, { useContext, useEffect, useReducer } from 'react';

import styles from './DataProvidersList.module.scss';

import { reducer } from './_components/reducer.jsx';
import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { DataProviderService } from 'core/services/DataProvider';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataProvidersList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    isVisibleConfirmDeleteDialog: false,
    dataProviderIdToDelete: '',
    dataProviders: [],
    allPossibleDataProviders: [],
    chosenDataProviders: [],
    representativesTypesList: [],
    representativesDropdown: null,
    selectedRepresentativeType: null
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  /*   const onPageLoad = async () => {
    const loadedData = await DataProviderService.all(dataflowId);

    return loadedData;
  }; */
  const onPageLoad = () => {
    const loadedData = {
      representativesOf: { nameLabel: 'Countries', name: 'countries' },
      dataProviders: [
        { dataProviderId: '1111', email: 'spain@es.es', id: 1 },
        { dataProviderId: '2222', email: 'germany@de.de', id: 2 },
        {
          dataProviderId: '3333',
          email: 'greatbr@uk.uk',
          id: 3
        } /* ,
        { dataProviderId: '4444', email: 'france@fr.fr', name: 'Fr' },
        { dataProviderId: '5555', email: 'italy@it.it', name: 'It' } */
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
    formDispatcher({
      type: 'GET_REPRESENTATIVES_TYPES_LIST'
    });

    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_TYPE'
    });

    formDispatcher({
      type: 'FILTER_CHOSEN_OPTIONS'
    });
  }, []);

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
              payload: { label: e.target.value, dataProviderId: rowData.dataProviderId }
            });
          }}
          value={rowData.id}>
          {formState.allPossibleDataProviders.map(provider => {
            /* formDispatcher({
              type: 'SET_CHOSEN_OPTIONS',
              payload: { id: rowData.id }
            }); */

            return (
              <option
                key={provider.id}
                className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
                value={provider.id}>
                {provider.label}
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

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>Data providers</div>

        <div>
          <label htmlFor="representativesDropdown">Representative of </label>

          <Dropdown
            disabled={
              formState.selectedRepresentativeType !== null && formState.representativesTypesList !== [] ? true : false
            }
            name="representativesDropdown"
            optionLabel="nameLabel"
            placeholder="Select..."
            value={formState.selectedRepresentativeType}
            options={formState.representativesTypesList}
            onChange={e => {
              return formDispatcher({ type: 'SELECT_REPRESENTATIVE_TYPE', payload: e.target.value });
            }}
          />
        </div>
      </div>

      <DataTable value={formState.dataProviders} scrollable={true} scrollHeight="100vh">
        <Column body={emailInputColumnTemplate} header="Email" />
        <Column body={nameDropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
      <ConfirmDialog
        onConfirm={() =>
          formDispatcher({
            type: 'DELETE_DATA_PROVIDER',
            payload: { dataProviderId: formState.dataProviderIdToDelete }
          })
        }
        onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
        visible={formState.isVisibleConfirmDeleteDialog}
        header={'Delete data provider'}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {'Do you really want to delete this data provider?'}
      </ConfirmDialog>
    </>
  );
};

export { DataProvidersList };
