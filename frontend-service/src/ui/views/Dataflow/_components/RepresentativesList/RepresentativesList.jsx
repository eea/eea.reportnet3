import React, { useContext, useEffect, useReducer } from 'react';

import { isEmpty, isNull } from 'lodash';
import styles from './RepresentativesList.module.scss';

import { reducer } from './_functions/Reducers/representativeReducer.js';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import {
  getInitialData,
  getAllDataProviders,
  createUnusedOptionsList,
  onAddProvider,
  onKeyDown,
  onDataProviderIdChange,
  onDeleteConfirm
} from './_functions/Utils/representativeUtils';

const RepresentativesList = ({ dataflowId }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    allPossibleDataProviders: [],
    dataProvidersTypesList: [],
    isVisibleConfirmDeleteDialog: false,
    representativeIdToDelete: '',
    representatives: [],
    initialRepresentatives: [],
    refresher: false,
    selectedDataProviderGroup: null,
    unusedDataProvidersOptions: [],
    representativeHasError: [],
    currentInputRepresentativeId: undefined
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

    let hasError = formState.representativeHasError.includes(representative.representativeId);

    return (
      <div className={`formField ${hasError ? 'error' : ''}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus={isNull(representative.representativeId)}
          onBlur={() => onAddProvider(formDispatcher, formState, representative, dataflowId)}
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
