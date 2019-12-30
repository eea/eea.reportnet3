import React, { useContext, useEffect, useReducer } from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';
import styles from './RepresentativesList.module.scss';

import { reducer } from './_components/reducer.jsx';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { RepresentativeService } from 'core/services/Representative';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const RepresentativesList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const initialState = {
    allPossibleDataProviders: [],
    dataProvidersTypesList: [],
    isVisibleConfirmDeleteDialog: false,
    representativeIdToDelete: '',
    representatives: [],
    responseStatus: null,
    refresher: false,
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: [],
    representativeHasError: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  useEffect(() => {
    if (!isNull(formState.selectedDataProviderGroup)) {
      getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);
    }
  }, [formState.selectedDataProviderGroup]);

  useEffect(() => {
    createUnusedOptionsList(formDispatcher);
  }, [formState.allPossibleDataProviders]);

  const providerAccountInputColumnTemplate = representative => {
    let inputData = representative.providerAccount;

    let hasError = representative.representativeId === formState.representativeHasError;

    return (
      <div className={`formField ${hasError ? 'error' : ''}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus
          onBlur={() => onAddProvider(formDispatcher, formState, representative, dataflowId, notificationContext)}
          onChange={e =>
            formDispatcher({
              type: 'ON_ACCOUNT_CHANGE',
              payload: { providerAccount: e.target.value, dataProviderId: representative.dataProviderId }
            })
          }
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
          placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
          value={inputData}
        />
      </div>
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
          className="p-dropdown-items p-dropdown-list p-component"
          onBlur={() => onAddProvider(formDispatcher, formState, representative, dataflowId)}
          onChange={event => {
            onDataProviderIdChange(formDispatcher, event.target.value, representative);
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
          value={representative.dataProviderId}>
          {remainingOptionsAndSelectedOption.map(provider => {
            return (
              <option key={`${provider.dataProviderId}`} className="p-dropdown-item" value={provider.dataProviderId}>
                {provider.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = representative => {
    return !isNull(representative.representativeId) ? (
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
    <div className={styles.container}>
      <div className={styles.selectWrapper}>
        <div className={styles.title}>{resources.messages['manageRolesDialogHeader']}</div>

        <div>
          <label htmlFor="dataProvidersDropdown">{resources.messages['manageRolesDialogDropdownLabel']} </label>
          <Dropdown
            disabled={formState.representatives.length > 1}
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

      {!isNull(formState.selectedDataProviderGroup) && !isEmpty(formState.allPossibleDataProviders) ? (
        <DataTable
          value={formState.representatives}
          scrollable={true}
          scrollHeight="100vh"
          rows={formState.allPossibleDataProviders.length - 1}>
          <Column
            body={providerAccountInputColumnTemplate}
            header={resources.messages['manageRolesDialogAccountColumn']}
          />
          <Column body={dropdownColumnTemplate} header={resources.messages['manageRolesDialogDataProviderColumn']} />
          <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
        </DataTable>
      ) : (
        <p className={styles.chooseRepresentative}>{resources.messages['manageRolesDialogNoRepresentativesMessage']}</p>
      )}

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
    </div>
  );
};

export { RepresentativesList };

const getInitialData = async (formDispatcher, dataflowId, formState) => {
  await getProviderTypes(formDispatcher);

  await getAllRepresentatives(dataflowId, formDispatcher);

  if (!isEmpty(formState.representatives)) {
    await getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);

    createUnusedOptionsList(formDispatcher);
  }
};

const getAllDataProviders = async (selectedDataProviderGroup, formDispatcher) => {
  try {
    const responseAllDataProviders = await RepresentativeService.allDataProviders(selectedDataProviderGroup);

    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
      payload: responseAllDataProviders
    });
  } catch (error) {
    console.log('error', error);
  }
};

const onDeleteConfirm = async (formDispatcher, formState) => {
  try {
    await RepresentativeService.deleteById(formState.representativeIdToDelete);

    formDispatcher({
      type: 'DELETE_REPRESENTATIVE',
      payload: formState.representativeIdToDelete
    });
  } catch (error) {
    console.log('error on RepresentativeService.deleteById: ', error);
  }
};

const createUnusedOptionsList = formDispatcher => {
  formDispatcher({
    type: 'CREATE_UNUSED_OPTIONS_LIST'
  });
};

const getAllRepresentatives = async (dataflowId, formDispatcher) => {
  try {
    const responseAllRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);

    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: responseAllRepresentatives
    });
  } catch (error) {
    console.log('error on RepresentativeService.allRepresentatives', error);
  }
};

const getProviderTypes = async formDispatcher => {
  try {
    const response = await RepresentativeService.getProviderTypes();

    formDispatcher({
      type: 'GET_PROVIDERS_TYPES_LIST',
      payload: response
    });
  } catch (error) {
    console.log('error on  RepresentativeService.getProviderTypes', error);
  }
};

const addRepresentative = async (formDispatcher, representatives, dataflowId) => {
  const newRepresentative = representatives.filter(representative => representative.representativeId == null);

  if (!isEmpty(newRepresentative[0].providerAccount) && !isEmpty(newRepresentative[0].dataProviderId)) {
    try {
      const responseStatus = await RepresentativeService.add(
        dataflowId,
        newRepresentative[0].providerAccount,
        parseInt(newRepresentative[0].dataProviderId)
      );

      formDispatcher({
        type: 'ADD_DATA_PROVIDER',
        payload: responseStatus.status
      });
    } catch (error) {
      console.log('error on RepresentativeService.add', error);
    }
  }
};

const updateRepresentative = async (formDispatcher, representative, notificationContext) => {
  try {
    let responseStatus = await RepresentativeService.updateProviderAccount(
      parseInt(representative.representativeId),
      representative.providerAccount
    );
    if (responseStatus.status >= 200 && responseStatus.status <= 299) {
      formDispatcher({
        type: 'UPDATE_ACCOUNT',
        payload: responseStatus.status
      });
      formDispatcher({
        type: 'REPRESENTATIVE_HAS_ERROR',
        payload: []
      });
    }
  } catch (error) {
    console.log('error on RepresentativeService.updateProviderAccount', error);

    formDispatcher({
      type: 'REPRESENTATIVE_HAS_ERROR',
      payload: representative.representativeId
    });

    // notificationContext.add({
    //   type: 'MANAGE_ROLES_ACCOUNT_ERROR'
    // });
  }
};

const updateProviderId = async (formDispatcher, representativeId, newDataProviderId) => {
  try {
    const responseStatus = await RepresentativeService.updateDataProviderId(
      parseInt(representativeId),
      parseInt(newDataProviderId)
    );
    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: responseStatus.status
    });
  } catch (error) {
    console.log('error on RepresentativeService.updateDataProviderId', error);
  }
};

const onDataProviderIdChange = (formDispatcher, newDataProviderId, representative) => {
  if (!isNull(representative.representativeId) && !isUndefined(representative.representativeId)) {
    updateProviderId(formDispatcher, representative.representativeId, newDataProviderId);
  } else {
    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: { dataProviderId: newDataProviderId, representativeId: representative.representativeId }
    });
  }
};

const onAddProvider = (formDispatcher, formState, representative, dataflowId, notificationContext) => {
  isNull(representative.representativeId)
    ? addRepresentative(formDispatcher, formState.representatives, dataflowId)
    : updateRepresentative(formDispatcher, representative, notificationContext);
};

const onKeyDown = (event, formDispatcher, formState, representative, dataflowId) => {
  if (event.key === 'Enter') {
    onAddProvider(formDispatcher, formState, representative, dataflowId);
  }
};
