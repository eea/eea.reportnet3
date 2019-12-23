import React, { useContext, useEffect, useReducer } from 'react';

import styles from './RepresentativesList.module.scss';

import { reducer } from './_components/reducer.jsx';
import { Button } from 'ui/views/_components/Button';
import { DataTable } from 'ui/views/_components/DataTable';

import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { RepresentativeService } from 'core/services/Representative';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const RepresentativesList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    dataProvidersTypesList: [],
    isVisibleConfirmDeleteDialog: false,
    representativeIdToDelete: '',
    representatives: [],
    selectedDataProviderGroupId: null,
    unusedDataProvidersOptions: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(async () => {
    const response = await RepresentativeService.getProviderTypes();

    formDispatcher({
      type: 'GET_PROVIDERS_TYPES_LIST',
      payload: response
    });
  }, []);

  useEffect(async () => {
    const response = await RepresentativeService.allRepresentatives(dataflowId);

    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: response
    });
  }, []);

  useEffect(async () => {
    const response = await RepresentativeService.allDataProviders(formState.selectedGroupId);
    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
      payload: response
    });

    formDispatcher({
      type: 'CREATE_UNUSED_OPTIONS_LIST'
    });
  }, []);

  const providerAccountInputColumnTemplate = rowData => {
    let inputData = rowData.providerAccount;
    return (
      <input
        defaultValue={inputData}
        placeholder={'Data provider account...'}
        onChange={e =>
          formDispatcher({
            type: 'ON_EMAIL_CHANGE',
            payload: { input: e.target.value, dataProviderId: rowData.dataProviderId }
          })
        }
        onBlur={e =>
          formDispatcher({
            type: 'UPDATE_EMAIL',
            payload: { input: e.target.value, dataProviderId: rowData.dataProviderId }
          })
        }
      />
    );
  };

  const dropdownColumnTemplate = rowData => {
    const selectedOptionForThisSelect = formState.allPossibleDataProviders.filter(
      option => option.dataProviderId === rowData.dataProviderId
    );

    const remainingOptionsAndSelectedOption = selectedOptionForThisSelect.concat(formState.unusedDataProvidersOptions);

    return (
      <>
        <select
          className="p-dropdown p-component"
          onChange={e => {
            formDispatcher({
              type: 'ON_PROVIDER_CHANGE',
              payload: { dataProviderId: e.target.value, representativeId: rowData.representativeId }
            });
          }}
          value={rowData.dataProviderId}>
          {remainingOptionsAndSelectedOption.map(provider => {
            return (
              <option
                key={provider.dataProviderId}
                className="p-dropdown-item p-dropdown-items p-dropdown-list p-component"
                value={provider.dataProviderId}>
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
          formDispatcher({ type: 'SHOW_CONFIRM_DIALOG', payload: { representativeId: rowData.representativeId } });
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
          <label htmlFor="dataProvidersDropdown">Representative of </label>

          <Dropdown
            disabled={
              formState.selectedDataProviderGroupId !== null && formState.dataProvidersTypesList !== [] ? true : false
            }
            name="dataProvidersDropdown"
            optionLabel="label"
            placeholder="Select..."
            value={formState.selectedDataProviderGroupId}
            options={formState.dataProvidersTypesList}
            onChange={e => {
              return formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: e.target.value });
            }}
          />
        </div>
      </div>

      <DataTable value={formState.representatives} scrollable={true} scrollHeight="100vh">
        <Column body={providerAccountInputColumnTemplate} header="Data provider account" />
        <Column body={dropdownColumnTemplate} header="Data Provider" />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>
      <ConfirmDialog
        onConfirm={() =>
          formDispatcher({
            type: 'DELETE_REPRESENTATIVE',
            payload: { dataProviderId: formState.representativeIdToDelete }
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

export { RepresentativesList };
