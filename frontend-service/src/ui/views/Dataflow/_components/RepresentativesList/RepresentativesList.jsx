import React, { useContext, useEffect, useReducer } from 'react';

import { isNull, isUndefined } from 'lodash';
import styles from './RepresentativesList.module.scss';

import { reducer } from './_components/reducer.jsx';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
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
    responseStatus: null,
    refresher: false,
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  /*   useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]); */

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);
    }

    createUnusedOptionsList(formDispatcher);
  }, [formState.selectedDataProviderGroup]);

  const providerAccountInputColumnTemplate = representative => {
    let inputData = representative.providerAccount;
    return (
      <input
        defaultValue={inputData}
        placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
        onChange={e =>
          formDispatcher({
            type: 'ON_ACCOUNT_CHANGE',
            payload: { input: e.target.value, dataProviderId: representative.dataProviderId }
          })
        }
        onBlur={event =>
          isNull(representative.representativeId)
            ? addRepresentative(formDispatcher, formState, representative, dataflowId, event.target.value)
            : updateRepresentative(formDispatcher, representative, dataflowId, event.target.value)
        }
      />
    );
  };

  const dropdownColumnTemplate = representative => {
    const selectedOptionForThisSelect = formState.allPossibleDataProviders.filter(
      option => option.dataProviderId === representative.dataProviderId
    );

    const remainingOptionsAndSelectedOption = selectedOptionForThisSelect.concat(formState.unusedDataProvidersOptions);

    return (
      <>
        <select
          className="p-dropdown p-component"
          onChange={event => {
            onProviderChange(formDispatcher, event.target.value, representative);
          }}
          value={representative.dataProviderId}>
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

  const deleteBtnColumnTemplate = representative => {
    return representative.dataProviderId !== '' ? (
      <Button
        tooltip={resources.messages['manageRolesDialogDeleteTooltip']}
        tooltipOptions={{ position: 'right' }}
        icon="trash"
        disabled={false}
        className={`p-button-rounded p-button-secondary ${styles.btnDelete}`}
        onClick={() => {
          formDispatcher({
            type: 'SHOW_CONFIRM_DIALOG',
            payload: { representativeId: representative.representativeId }
          });
        }}
      />
    ) : (
      <></>
    );
  };

  return (
    <>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>{resources.messages['manageRolesDialogHeader']}</div>

        <div>
          <label htmlFor="dataProvidersDropdown">{resources.messages['manageRolesDialogDropdownLabel']} </label>
          <Dropdown
            /* disabled={
              formState.selectedDataProviderGroup !== null && formState.dataProvidersTypesList !== [] ? true : false
            } */
            name="dataProvidersDropdown"
            optionLabel="label"
            placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
            value={formState.selectedDataProviderGroup}
            options={formState.dataProvidersTypesList}
            onChange={e => {
              return formDispatcher({ type: 'SELECT_PROVIDERS_TYPE', payload: e.target.value });
            }}
          />
        </div>
      </div>

      {formState.representatives.length > 1 && !isNull(formState.selectedDataProviderGroup)}

      <DataTable value={formState.representatives} scrollable={true} scrollHeight="100vh">
        <Column
          body={providerAccountInputColumnTemplate}
          header={resources.messages['manageRolesDialogAccoutColumn']}
        />
        <Column body={dropdownColumnTemplate} header={resources.messages['manageRolesDialogDataProviderColumn']} />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>

      <ConfirmDialog
        onConfirm={() => {
          onDeleteConfirm(formDispatcher, formState);
        }}
        onHide={() => formDispatcher({ type: 'HIDE_CONFIRM_DIALOG' })}
        visible={formState.isVisibleConfirmDeleteDialog}
        header={resources.messages['manageRolesDialogConfirmDeleteHeader']}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {resources.messages['manageRolesDialogConfirmDeleteQuestion']}
      </ConfirmDialog>
    </>
  );
};

export { RepresentativesList };

const getInitialData = async (formDispatcher, dataflowId, formState) => {
  await getProviderTypes(formDispatcher);

  await getAllRepresentatives(dataflowId, formDispatcher);

  await getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);

  createUnusedOptionsList(formDispatcher);
};

const getAllDataProviders = async (selectedDataProviderGroup, formDispatcher) => {
  const responseAllDataProviders = await RepresentativeService.allDataProviders(selectedDataProviderGroup);
  formDispatcher({
    type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
    payload: responseAllDataProviders
  });
};

const onDeleteConfirm = async (formDispatcher, formState) => {
  const responseStatus = await RepresentativeService.deleteById(formState.representativeIdToDelete);

  formDispatcher({
    type: 'DELETE_REPRESENTATIVE',
    payload: responseStatus.status
  });
};

const createUnusedOptionsList = formDispatcher => {
  formDispatcher({
    type: 'CREATE_UNUSED_OPTIONS_LIST'
  });
};

const getAllRepresentatives = async (dataflowId, formDispatcher) => {
  const responseAllRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);
  formDispatcher({
    type: 'INITIAL_LOAD',
    payload: responseAllRepresentatives
  });
};

const getProviderTypes = async formDispatcher => {
  const response = await RepresentativeService.getProviderTypes();
  formDispatcher({
    type: 'GET_PROVIDERS_TYPES_LIST',
    payload: response
  });
};

const addRepresentative = async (formDispatcher, formState, representative, dataflowId, inputValue) => {
  console.log('ON ADD PROVIDER formState.representatives', formState.representatives);
  const responseStatus = await RepresentativeService.add(
    dataflowId,
    inputValue,
    parseInt(representative.dataProviderId)
  );

  formDispatcher({
    type: 'ADD_DATA_PROVIDER',
    payload: responseStatus.status
  });
};

const updateRepresentative = async (formDispatcher, representative, dataflowId, inputValue) => {
  const responseStatus = await RepresentativeService.update(
    dataflowId,
    inputValue,
    parseInt(representative.dataProviderId)
  );

  formDispatcher({
    type: 'UPDATE_ACCOUNT',
    payload: responseStatus.status
  });
};

const updateProviderId = async (formDispatcher, representativeId, newDataProviderId) => {
  const responseStatus = await RepresentativeService.updateDataProviderId(
    parseInt(representativeId),
    parseInt(newDataProviderId)
  );
  formDispatcher({
    type: 'ON_PROVIDER_CHANGE',
    payload: responseStatus.status
  });
};

const onProviderChange = (formDispatcher, newDataProviderId, representative) => {
  if (!isNull(representative.representativeId) && !isUndefined(representative.representativeId)) {
    updateProviderId(formDispatcher, representative.representativeId, newDataProviderId);
  } else {
    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: { dataProviderId: newDataProviderId, representativeId: representative.representativeId }
    });
  }
};
