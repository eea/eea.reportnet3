import React, { useContext, useEffect } from 'react';

import isNil from 'lodash/isNil';

import uuid from 'uuid';
import styles from './ManageRights.module.scss';
import isEmpty from 'lodash/isEmpty';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import {
  autofocusOnEmptyInput,
  onAddRepresentative,
  onPermissionsChange,
  onDeleteConfirm,
  onKeyDown
} from '../_functions/Utils/rightsUtils';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ManageRights = ({ formState, formDispatcher, dataflowId }) => {
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    autofocusOnEmptyInput(formState);
    console.log('formState.representativesHaveError', formState.representativesHaveError);
  }, [formState.representativesHaveError]);

  const accountInputColumnTemplate = representative => {
    let inputData = representative.account;

    let hasError = formState.representativesHaveError.includes(representative.representativeId);

    const onAccountChange = (value, representativeId) => {
      const { representatives } = formState;

      const [thisRepresentative] = representatives.filter(
        thisRepresentative => thisRepresentative.representativeId === representativeId
      );
      thisRepresentative.account = value;

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
            id={isEmpty(inputData) ? 'emptyInput' : undefined}
            onBlur={() => {
              representative.account = representative.account.toLowerCase();
              onAddRepresentative(formDispatcher, formState, representative, dataflowId);
            }}
            onChange={event => onAccountChange(event.target.value, representative.representativeId)}
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
      { label: resources.messages['selectPermission'], permission: '' },
      { label: resources.messages['readPermission'], permission: 'false' },
      { label: resources.messages['readAndWritePermission'], permission: 'true' }
    ];

    return (
      <>
        <select
          onBlur={() => onAddRepresentative(formDispatcher, formState, representative, dataflowId)}
          onChange={event => {
            onPermissionsChange(formDispatcher, event.target.value, representative, formState);
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, representative, dataflowId)}
          value={representative.permission}>
          {permissionsOptions.map(option => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={option.permission}>
                {option.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = representative => {
    return isNil(representative.representativeId) /*||  representative.permission*/ ? (
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
        <Column body={accountInputColumnTemplate} header={formState.accountInputHeader} />
        <Column body={dropdownColumnTemplate} header={resources.messages['permissionsColumn']} />
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
          header={formState.deleteConfirmHeader}
          labelConfirm={resources.messages['yes']}
          labelCancel={resources.messages['no']}>
          {formState.deleteConfirmMessage}
        </ConfirmDialog>
      )}
    </div>
  );
};
