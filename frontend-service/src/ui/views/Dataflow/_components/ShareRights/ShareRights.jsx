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

import { UserRights } from 'core/domain/model/Rights/UserRights';
import { RightsService } from 'core/services/Rights';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

export const ShareRights = ({
  userType,
  rightsOptions,
  columnHeader,
  dataflowId,
  dataProviderId,
  deleteConfirmHeader,
  deleteConfirmMessage,
  notificationKey,
  placeholder,
  representativeId
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    accountNotFound: false,
    userAccountToDelete: '',
    users: [],
    user: {},
    clonedUsers: [],
    isUserDeleting: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false
  });

  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    getAllUsers();
  }, [shareRightsState.isDataUpdated]);

  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;

  const callEndPoint = async (method, user) => {
    if (userType === 'editor') {
      if (method === 'getAll') {
        return await RightsService.allEditors(dataflowId, dataProvider);
      }
      if (method === 'delete') {
        return await RightsService.deleteEditor(shareRightsState.userAccountToDelete, dataflowId, dataProvider);
      }
      if (method === 'update') {
        return await RightsService.updateEditor(user, dataflowId, dataProvider);
      }
    }

    if (userType === 'reporter') {
      if (method === 'getAll') {
        return await RightsService.allReporters(dataflowId, dataProvider);
      }
      if (method === 'delete') {
        return await RightsService.deleteReporter(shareRightsState.userAccountToDelete, dataflowId, dataProvider);
      }
      if (method === 'update') {
        return await RightsService.updateReporter(user, dataflowId, dataProvider);
      }
    }

    if (userType === 'requester') {
      if (method === 'getAll') {
        return await RightsService.allRequester(dataflowId, dataProvider);
      }
      if (method === 'delete') {
        return await RightsService.deleteRequester(shareRightsState.userAccountToDelete, dataflowId, dataProvider);
      }
      if (method === 'update') {
        return await RightsService.updateRequester(user, dataflowId, dataProvider);
      }
    }
  };

  const getAllUsers = async () => {
    // const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;

    try {
      // const users = await RightsService.allEditors(dataflowId, dataProvider);
      const users = await callEndPoint('getAll');
      console.log(`users`, users);
      const emptyUser = new UserRights({ account: '', dataProviderId: '', isNew: true, writePermission: '' });
      const usersWithNew = [...users, emptyUser];
      const clonedUsers = cloneDeep(usersWithNew);

      shareRightsDispatch({
        type: 'GET_ALL_USERS',
        payload: { users: usersWithNew, clonedUsers }
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
    const sameAccounts = shareRightsState.users.filter(user => user.account === account);

    return sameAccounts.length > 1;
  };

  const isPermissionChanged = user => {
    const [initialUser] = shareRightsState.clonedUsers.filter(fUser => fUser.id === user.id);

    return JSON.stringify(initialUser.writePermission) !== JSON.stringify(user.writePermission);
  };

  const updateUser = user => {
    shareRightsDispatch({
      type: 'SET_ACCOUNT_HAS_ERROR',
      payload: {
        accountHasError:
          !isValidEmail(user.account) || isRepeatedAccount(user.account) || shareRightsState.accountNotFound
      }
    });

    if (!user.isNew && isPermissionChanged(user)) {
      onUpdateUser(user);
    } else {
      if (isValidEmail(user.account) && !shareRightsState.accountHasError) {
        onUpdateUser(user);
      }
    }
  };

  const onDeleteUser = async () => {
    onToggleDeletingUser(true);

    try {
      const response = await callEndPoint('delete');
      if (response.status >= 200 && response.status <= 299) {
        onDataChange();
      }
    } catch (error) {
      notificationContext.add({ type: notificationKey });
    } finally {
      onToggleDeletingUser(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onDataChange = () => {
    shareRightsDispatch({ type: 'ON_DATA_CHANGE', payload: { isDataUpdated: !shareRightsState.isDataUpdated } });
  };

  const onEnterKey = (key, user) => {
    if (key === 'Enter' && isValidEmail(user.account) && isPermissionChanged(user)) {
      onUpdateUser(user);
    }
  };

  const onUpdateUser = async user => {
    if (user.writePermission !== '') {
      user.account = user.account.toLowerCase();
      setIsLoading(true);
      try {
        const response = await callEndPoint('update', user);
        if (response.status >= 200 && response.status <= 299) {
          onDataChange();
        }
      } catch (error) {
        if (error?.response?.status === 404) {
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

  const onWritePermissionChange = async (user, newWritePermission) => {
    const { users } = shareRightsState;
    const [thisUser] = users.filter(thisUser => thisUser.id === user.id);
    thisUser.writePermission = newWritePermission;

    shareRightsDispatch({ type: 'ON_WRITE_PERMISSION_CHANGE', payload: { users } });
  };

  const onSetAccount = inputValue => {
    const { users } = shareRightsState;
    const [newUser] = users.filter(user => user.isNew);
    newUser.account = inputValue;

    shareRightsDispatch({
      type: 'ON_SET_ACCOUNT',
      payload: {
        users,
        accountHasError: !isValidEmail(inputValue) || isRepeatedAccount(inputValue),
        accountNotFound: false
      }
    });
  };

  const onToggleDeletingUser = value => {
    shareRightsDispatch({ type: 'TOGGLE_DELETING_USER', payload: { isDeleting: value } });
  };

  const renderDeleteColumnTemplate = user =>
    user.isNew ? (
      <Fragment />
    ) : (
      <ActionsColumn
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'ON_DELETE_USER',
            payload: { isDeleteDialogVisible: true, userAccountToDelete: user.account }
          })
        }
      />
    );

  const renderRightsTypeColumnTemplate = user => {
    const rightsTypeOptions = user.isNew
      ? [{ label: resources.messages['selectPermission'], writePermission: '' }, ...rightsOptions]
      : rightsOptions;

    return (
      <>
        <select
          id="dataProvider" // check
          onKeyDown={event => onEnterKey(event.key, user)}
          onBlur={() => updateUser(user)}
          onChange={event => onWritePermissionChange(user, event.target.value)}
          value={user.writePermission}>
          {rightsTypeOptions.map(option => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={option.writePermission}>
                {option.label}
              </option>
            );
          })}
        </select>
        <label htmlFor="dataProvider" className="srOnly">
          {resources.messages['manageRolesEditorDialogInputPlaceholder']} {/* CHECK MESSAGE*/}
        </label>
      </>
    );
  };

  const renderAccountTemplate = user => {
    const hasError = !isEmpty(user.account) && user.isNew && shareRightsState.accountHasError;

    return (
      <div className={`formField ${hasError ? 'error' : ''}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus={user.isNew}
          disabled={!user.isNew}
          className={!user.isNew ? styles.disabledInput : ''}
          id={isEmpty(user.account) ? 'emptyInput' : user.account}
          onBlur={() => updateUser(user)}
          onChange={event => onSetAccount(event.target.value)}
          placeholder={placeholder}
          value={user.account}
        />
        <label htmlFor="emptyInput" className="srOnly">
          {resources.messages['manageRolesEditorDialogInputPlaceholder']} {/*CHECK MESSAGE*/}
        </label>
      </div>
    );
  };

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.users) ? (
          <Spinner style={{ top: 0 }} />
        ) : (
          <div className={styles.table}>
            {isLoading && <Spinner className={styles.spinner} style={{ top: 0, left: 0, zIndex: 6000 }} />}
            <DataTable value={shareRightsState.users}>
              <Column body={renderAccountTemplate} header={columnHeader} />
              <Column body={renderRightsTypeColumnTemplate} header={resources.messages['writePermissionsColumn']} />
              <Column
                body={renderDeleteColumnTemplate}
                className={styles.emptyTableHeader}
                header={resources.messages['deleteContributorButtonTableHeader']} // CHECK MESSAGE
                style={{ width: '60px' }}
              />
            </DataTable>
          </div>
        )}
      </div>

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={shareRightsState.isUserDeleting}
          header={deleteConfirmHeader}
          iconConfirm={shareRightsState.isUserDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteUser()}
          onHide={() =>
            shareRightsDispatch({
              type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG',
              payload: { isDeleteDialogVisible: false }
            })
          }
          visible={shareRightsState.isDeleteDialogVisible}>
          {deleteConfirmMessage}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
