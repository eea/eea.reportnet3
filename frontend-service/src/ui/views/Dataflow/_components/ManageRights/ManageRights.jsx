import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import uuid from 'uuid';
import styles from './ManageRights.module.scss';

import { reducer } from './_functions/Reducers/representativeReducer.js';
import {
  autofocusOnEmptyInput,
  getInitialData,
  onAddProvider,
  onDataProviderIdChange,
  onDeleteConfirm,
  onKeyDown
} from './_functions/Utils/representativeUtils';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ManageRights = ({ dataflowState, dataflowRepresentatives, dataflowId, isActiveManageRightsDialog }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    dataflowRepresentatives: dataflowRepresentatives,
    initialRepresentatives: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    representativeHasError: [],
    representativeIdToDelete: '',
    representatives: []
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, formState);
  }, [formState.refresher]);

  useEffect(() => {
    if (isActiveManageRightsDialog === false && !isEmpty(formState.representativeHasError)) {
      formDispatcher({
        type: 'REFRESH'
      });
    }
  }, [isActiveManageRightsDialog]);

  useEffect(() => {
    autofocusOnEmptyInput(formState);
  }, [formState.representativeHasError]);

  const providerAccountInputColumnTemplate = representative => {
    let inputData = representative.providerAccount;

    let hasError = formState.representativeHasError.includes(representative.representativeId);

    const onAccountChange = (value, dataProviderId) => {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.dataProviderId === dataProviderId
      );
      thisRepresentative.providerAccount = value;

      formDispatcher({
        type: 'ON_ACCOUNT_CHANGE',
        payload: {
          representatives
        }
      });
    };

    return (
      <>
        <div className={`formField ${hasError && 'error'}`} style={{ marginBottom: '0rem' }}>
          <input
            autoFocus={isNil(representative.representativeId)}
            className={representative.hasDatasets ? styles.disabled : undefined}
            disabled={representative.hasDatasets}
            id={isEmpty(inputData) ? 'emptyInput' : undefined}
            onBlur={() => {
              representative.providerAccount = representative.providerAccount.toLowerCase();
              onAddProvider(formDispatcher, formState, representative, dataflowId);
            }}
            onChange={event => onAccountChange(event.target.value, representative.dataProviderId)}
            onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
            placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
            value={inputData}
          />
        </div>
      </>
    );
  };

  const dropdownColumnTemplate = representative => {
    const permissionsOptions = [
      { label: resources.messages['selectPermission'], type: '' },
      { label: resources.messages['readPermission'], type: 'read' },
      { label: resources.messages['readAndWritePermission'], type: 'read_write' }
    ];

    return (
      <>
        <select
          disabled={representative.hasDatasets}
          onBlur={() => onAddProvider(formDispatcher, formState, representative, dataflowId)}
          onChange={event => {
            onDataProviderIdChange(formDispatcher, event.target.value, representative, formState);
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
          value={representative.permissionType}>
          {permissionsOptions.map(permission => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={permission.type}>
                {permission.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = representative => {
    return isNil(representative.representativeId) /*||  representative.permissionsType */ ? (
      <></>
    ) : (
      <ActionsColumn
        onDeleteClick={() => {
          formDispatcher({
            type: 'SHOW_CONFIRM_DIALOG',
            payload: { representativeId: representative.representativeId }
          });
        }}
      />
    );
  };

  if (isEmpty(formState.representatives)) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.container}>
      <DataTable value={formState.representatives}>
        <Column
          body={providerAccountInputColumnTemplate}
          header={resources.messages['manageRolesDialogAccountColumn']}
        />
        <Column body={dropdownColumnTemplate} header={'Permissions'} />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>

      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
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
      )}
    </div>
  );
};

export { ManageRights };
