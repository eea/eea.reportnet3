import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import uuid from 'uuid';
import styles from './ManageRights.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { reducer } from './_functions/Reducers/contributorReducer.js';
import {
  getInitialData,
  autofocusOnEmptyInput,
  onAddContributor,
  onWritePermissionChange,
  onDeleteConfirm,
  onKeyDown
} from './_functions/Utils/contributorUtils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ManageRights = ({ dataflowState, dataflowId, dataProviderId, isActiveManageRightsDialog }) => {
  const resources = useContext(ResourcesContext);

  const initialState = {
    initialContributors: [],
    isVisibleConfirmDeleteDialog: false,
    refresher: false,
    contributorsHaveError: [],
    contributorIdToDelete: '',
    contributors: [],
    accountInputHeader: dataflowState.isCustodian
      ? resources.messages['editorsAccountColumn']
      : resources.messages['reportersAccountColumn'],
    deleteConfirmHeader: dataflowState.isCustodian
      ? resources.messages['editorsRightsDialogConfirmDeleteHeader']
      : resources.messages['reportersRightsDialogConfirmDeleteHeader'],
    deleteConfirmMessage: dataflowState.isCustodian
      ? resources.messages['editorsRightsDialogConfirmDeleteQuestion']
      : resources.messages['reportersRightsDialogConfirmDeleteQuestion']
  };

  const [formState, formDispatcher] = useReducer(reducer, initialState);

  useEffect(() => {
    getInitialData(formDispatcher, dataflowId, dataProviderId);
  }, [formState.refresher]);

  useEffect(() => {
    if (isActiveManageRightsDialog === false && !isEmpty(formState.contributorsHaveError)) {
      formDispatcher({
        type: 'REFRESH'
      });
    }
  }, [isActiveManageRightsDialog]);

  useEffect(() => {
    autofocusOnEmptyInput(formState);
  }, [formState.contributorsHaveError]);

  const accountInputColumnTemplate = contributor => {
    let inputData = contributor.account;

    let hasError = formState.contributorsHaveError.includes(contributor.account);

    const onAccountChange = (value, account) => {
      const { contributors } = formState;

      const [thisContributor] = contributors.filter(thisContributor => thisContributor.account === account);
      thisContributor.account = value;

      formDispatcher({
        type: 'ON_ACCOUNT_CHANGE',
        payload: {
          contributors
        }
      });
    };

    return (
      <>
        <div className={`formField ${hasError && 'error'}`} style={{ marginBottom: '0rem' }}>
          <input
            autoFocus={contributor.isNew}
            id={isEmpty(inputData) ? 'emptyInput' : undefined}
            onBlur={() => {
              contributor.account = contributor.account.toLowerCase();
              onAddContributor(formDispatcher, formState, contributor, dataflowId);
            }}
            onChange={event => onAccountChange(event.target.value, contributor.account)}
            onKeyDown={event => onKeyDown(event, formDispatcher, formState, contributor, dataflowId)}
            placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
            value={inputData}
            disabled={!contributor.isNew}
          />
        </div>
      </>
    );
  };

  const writePermissionsColumnTemplate = contributor => {
    const writePermissionsOptions = [
      { label: resources.messages['selectPermission'], writePermission: '' },
      { label: resources.messages['readPermission'], writePermission: 'false' },
      { label: resources.messages['readAndWritePermission'], writePermission: 'true' }
    ];

    return (
      <>
        <select
          onBlur={() => onAddContributor(formDispatcher, formState, contributor, dataflowId)}
          onChange={event => {
            onWritePermissionChange(
              contributor,
              dataflowId,
              dataProviderId,
              formDispatcher,
              formState,
              event.target.value
            );
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, contributor, dataflowId)}
          value={contributor.writePermission}>
          {writePermissionsOptions.map(option => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={option.writePermission}>
                {option.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const deleteBtnColumnTemplate = contributor => {
    return contributor.isNew ? (
      <></>
    ) : (
      <ActionsColumn
        onDeleteClick={() => {
          formDispatcher({
            type: 'SHOW_CONFIRM_DIALOG',
            payload: { contributor }
          });
        }}
      />
    );
  };

  if (isEmpty(formState.contributors)) return <Spinner style={{ top: 0 }} />;
  return (
    <div className={styles.container}>
      <DataTable value={formState.contributors}>
        <Column body={accountInputColumnTemplate} header={formState.accountInputHeader} />
        <Column body={writePermissionsColumnTemplate} header={resources.messages['writePermissionsColumn']} />
        <Column body={deleteBtnColumnTemplate} style={{ width: '60px' }} />
      </DataTable>

      {formState.isVisibleConfirmDeleteDialog && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          onConfirm={() => {
            onDeleteConfirm(formDispatcher, formState, dataflowId, dataProviderId);
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

export { ManageRights };
