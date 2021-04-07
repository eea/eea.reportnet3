import React, { Fragment, useContext, useEffect, useState, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uuid from 'uuid';

import styles from './ShareRights.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { Contributor } from 'core/domain/model/Contributor/Contributor';
import { ContributorService } from 'core/services/Contributor';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

export const ShareRights = ({
  dataflowId,
  dataProviderId,
  representativeId,
  showEditorsHeaders,
  showReportersHeaders
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    accountNotFound: false,
    contributorAccountToDelete: '',
    contributors: [],
    clonedContributors: [],
    isContributorDeleting: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false
  });

  const [isLoading, setIsLoading] = useState(false);

  const getColumnHeader = () => {
    if (showReportersHeaders) {
      return resources.messages['reportersAccountColumn'];
    }

    if (showEditorsHeaders) {
      return resources.messages['editorsAccountColumn'];
    }
  };

  const getDeleteConfirmHeader = () => {
    if (showReportersHeaders) {
      return resources.messages[`reportersRightsDialogConfirmDeleteHeader`];
    }

    if (showEditorsHeaders) {
      return resources.messages[`editorsRightsDialogConfirmDeleteHeader`];
    }
  };

  const getDeleteConfirmMessage = () => {
    if (showReportersHeaders) {
      return resources.messages[`reportersRightsDialogConfirmDeleteQuestion`];
    }

    if (showEditorsHeaders) {
      return resources.messages[`editorsRightsDialogConfirmDeleteQuestion`];
    }
  };

  const getPlaceholder = () => {
    if (showReportersHeaders) {
      return resources.messages['manageRolesReporterDialogInputPlaceholder'];
    }

    if (showEditorsHeaders) {
      return resources.messages['manageRolesEditorDialogInputPlaceholder'];
    }
  };

  const getNotificationKey = () => {
    if (showReportersHeaders) {
      return 'DELETE_REPORTER_ERROR';
    }

    if (showEditorsHeaders) {
      return 'DELETE_EDITOR_ERROR';
    }
  };

  useEffect(() => {
    getAllContributors();
  }, [shareRightsState.isDataUpdated]);

  const getAllContributors = async () => {
    const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;

    try {
      const contributors = await ContributorService.all(dataflowId, dataProvider);
      const emptyContributor = new Contributor({ account: '', dataProviderId: '', isNew: true, writePermission: '' });
      const contributorsWithNew = [...contributors, emptyContributor];
      const clonedContributors = cloneDeep(contributorsWithNew);

      shareRightsDispatch({
        type: 'GET_ALL_CONTRIBUTORS',
        payload: { contributors: contributorsWithNew, clonedContributors }
      });
    } catch (error) {}
  };

  const isValidEmail = email => {
    if (isNil(email)) {
      return true;
    }

    const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

    return email.match(expression);
  };

  const isRepeatedAccount = account => {
    const sameAccounts = shareRightsState.contributors.filter(contributor => contributor.account === account);

    return sameAccounts.length > 1;
  };

  const isPermissionChanged = contributor => {
    const [initialContributor] = shareRightsState.clonedContributors.filter(
      fContributor => fContributor.id === contributor.id
    );

    return JSON.stringify(initialContributor.writePermission) !== JSON.stringify(contributor.writePermission);
  };

  const updateContributor = contributor => {
    shareRightsDispatch({
      type: 'SET_ACCOUNT_HAS_ERROR',
      payload: {
        accountHasError:
          !isValidEmail(contributor.account) ||
          isRepeatedAccount(contributor.account) ||
          shareRightsState.accountNotFound
      }
    });

    if (!contributor.isNew && isPermissionChanged(contributor)) {
      onUpdateContributor(contributor);
    } else {
      if (isValidEmail(contributor.account) && !shareRightsState.accountHasError) {
        onUpdateContributor(contributor);
      }
    }
  };

  const onDeleteContributor = async () => {
    onToggleDeletingContributor(true);
    const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
    try {
      const response = await ContributorService.deleteContributor(
        shareRightsState.contributorAccountToDelete,
        dataflowId,
        dataProvider
      );
      if (response.status >= 200 && response.status <= 299) {
        onDataChange();
      }
    } catch (error) {
      notificationContext.add({ type: getNotificationKey() });
    } finally {
      onToggleDeletingContributor(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onDataChange = () => {
    shareRightsDispatch({ type: 'ON_DATA_CHANGE', payload: { isDataUpdated: !shareRightsState.isDataUpdated } });
  };

  const onEnterKey = (key, contributor) => {
    if (key === 'Enter' && isValidEmail(contributor.account) && isPermissionChanged(contributor)) {
      onUpdateContributor(contributor);
    }
  };

  const onUpdateContributor = async contributor => {
    if (contributor.writePermission !== '') {
      const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
      contributor.account = contributor.account.toLowerCase();
      setIsLoading(true);
      try {
        const response = await ContributorService.update(contributor, dataflowId, dataProvider);
        if (response.status >= 200 && response.status <= 299) {
          onDataChange();
        }
      } catch (error) {
        if (error.response.status === 404) {
          shareRightsDispatch({
            type: 'SET_ACCOUNT_NOT_FOUND',
            payload: { accountNotFound: true, accountHasError: true }
          });
        }
      } finally {
        setIsLoading(false);
      }
    }
  };

  const onWritePermissionChange = async (contributor, newWritePermission) => {
    const { contributors } = shareRightsState;
    const [thisContributor] = contributors.filter(thisContributor => thisContributor.id === contributor.id);
    thisContributor.writePermission = newWritePermission;

    shareRightsDispatch({ type: 'ON_WRITE_PERMISSION_CHANGE', payload: { contributors } });
  };

  const onSetAccount = inputValue => {
    const { contributors } = shareRightsState;
    const [newContributor] = contributors.filter(contributor => contributor.isNew);
    newContributor.account = inputValue;

    shareRightsDispatch({
      type: 'ON_SET_ACCOUNT',
      payload: {
        contributors,
        accountHasError: !isValidEmail(inputValue) || isRepeatedAccount(inputValue),
        accountNotFound: false
      }
    });
  };

  const onToggleDeletingContributor = value => {
    shareRightsDispatch({ type: 'TOGGLE_DELETING_CONTRIBUTOR', payload: { isDeleting: value } });
  };

  const renderDeleteColumnTemplate = contributor =>
    contributor.isNew ? (
      <Fragment />
    ) : (
      <ActionsColumn
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'ON_DELETE_CONTRIBUTOR',
            payload: { isDeleteDialogVisible: true, contributorAccountToDelete: contributor.account }
          })
        }
      />
    );

  const renderWritePermissionsColumnTemplate = contributor => {
    const writePermissionsOptions = contributor.isNew
      ? [
          { label: resources.messages['selectPermission'], writePermission: '' },
          { label: resources.messages['readPermission'], writePermission: false },
          { label: resources.messages['readAndWritePermission'], writePermission: true }
        ]
      : [
          { label: resources.messages['readPermission'], writePermission: false },
          { label: resources.messages['readAndWritePermission'], writePermission: true }
        ];

    return (
      <>
        <select
          id="dataProvider"
          onKeyDown={event => onEnterKey(event.key, contributor)}
          onBlur={() => updateContributor(contributor)}
          onChange={event => onWritePermissionChange(contributor, event.target.value)}
          value={contributor.writePermission}>
          {writePermissionsOptions.map(option => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={option.writePermission}>
                {option.label}
              </option>
            );
          })}
        </select>
        <label htmlFor="dataProvider" className="srOnly">
          {resources.messages['manageRolesEditorDialogInputPlaceholder']}
        </label>
      </>
    );
  };

  const renderAccountTemplate = contributor => {
    const hasError = !isEmpty(contributor.account) && contributor.isNew && shareRightsState.accountHasError;

    return (
      <div className={`formField ${hasError ? 'error' : ''}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus={contributor.isNew}
          disabled={!contributor.isNew}
          className={!contributor.isNew ? styles.disabledInput : ''}
          id={isEmpty(contributor.account) ? 'emptyInput' : contributor.account}
          onBlur={() => updateContributor(contributor)}
          onChange={event => onSetAccount(event.target.value)}
          placeholder={getPlaceholder()}
          value={contributor.account}
        />
        <label htmlFor="emptyInput" className="srOnly">
          {resources.messages['manageRolesEditorDialogInputPlaceholder']}
        </label>
      </div>
    );
  };

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.contributors) ? (
          <Spinner style={{ top: 0 }} />
        ) : (
          <div className={styles.table}>
            {isLoading && <Spinner className={styles.spinner} style={{ top: 0, left: 0, zIndex: 6000 }} />}
            <DataTable value={shareRightsState.contributors}>
              <Column body={renderAccountTemplate} header={getColumnHeader()} />
              <Column
                body={renderWritePermissionsColumnTemplate}
                header={resources.messages['writePermissionsColumn']}
              />
              <Column
                body={renderDeleteColumnTemplate}
                className={styles.emptyTableHeader}
                header={resources.messages['deleteContributorButtonTableHeader']}
                style={{ width: '60px' }}
              />
            </DataTable>
          </div>
        )}
      </div>

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={shareRightsState.isContributorDeleting}
          header={getDeleteConfirmHeader()}
          iconConfirm={shareRightsState.isContributorDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteContributor()}
          onHide={() =>
            shareRightsDispatch({
              type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG',
              payload: { isDeleteDialogVisible: false }
            })
          }
          visible={shareRightsState.isDeleteDialogVisible}>
          {getDeleteConfirmMessage()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
