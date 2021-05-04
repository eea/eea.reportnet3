import { Fragment, useContext, useEffect, useState, useReducer } from 'react';

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

import { UserRight } from 'core/domain/model/UserRight/UserRight';
import { UserRightService } from 'core/services/UserRight';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

export const ShareRights = ({
  userType,
  roleOptions,
  columnHeader,
  dataflowId,
  dataProviderId,
  deleteColumnHeader,
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
    userRightToDelete: '',
    userRightList: [],
    userRight: {},
    clonedUserRightList: [],
    isUserDeleting: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false
  });

  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    getAllUsers();
  }, [shareRightsState.isDataUpdated]);

  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;

  const callEndPoint = async (method, userRight) => {
    if (userType === 'editor') {
      if (method === 'getAll') {
        return await UserRightService.allEditors(dataflowId);
      }
      if (method === 'delete') {
        return await UserRightService.deleteEditor(shareRightsState.userRightToDelete.account, dataflowId);
      }
      if (method === 'update') {
        return await UserRightService.updateEditor(userRight, dataflowId);
      }
    }

    if (userType === 'reporter') {
      if (method === 'getAll') {
        return await UserRightService.allReporters(dataflowId, dataProvider);
      } else if (method === 'delete') {
        return await UserRightService.deleteReporter(shareRightsState.userRightToDelete, dataflowId, dataProvider);
      } else if (method === 'update') {
        return await UserRightService.updateReporter(dataflowId, dataProvider);
      }
    }

    if (userType === 'requester') {
      if (method === 'getAll') {
        return await UserRightService.allRequester(dataflowId);
      } else if (method === 'delete') {
        return await UserRightService.deleteRequester(shareRightsState.userRightToDelete, dataflowId);
      } else if (method === 'update') {
        return await UserRightService.updateRequester(userRight, dataflowId);
      }
    }
  };

  const getAllUsers = async () => {
    try {
      const userRightList = await callEndPoint('getAll');
      const newUserRight = new UserRight({ account: '', dataProviderId: '', isNew: true, role: '' });
      const userRightListWithNew = [...userRightList, newUserRight];
      const clonedUserRightList = cloneDeep(userRightListWithNew);

      shareRightsDispatch({
        type: 'GET_ALL_USERS',
        payload: { userRightList: userRightListWithNew, clonedUserRightList }
      });
    } catch (error) {}
  };

  const isValidEmail = email => {
    if (isNil(email)) {
      return true;
    }

    // eslint-disable-next-line no-useless-escape
    const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

    return email.match(expression);
  };

  const isRepeatedAccount = account => {
    const sameAccounts = shareRightsState.userRightList.filter(userRight => userRight.account === account);

    return sameAccounts.length > 1;
  };

  const isPermissionChanged = userRight => {
    const [initialUser] = shareRightsState.clonedUserRightList.filter(fUserRight => fUserRight.id === userRight.id);

    return JSON.stringify(initialUser.role) !== JSON.stringify(userRight.role);
  };

  const updateUser = userRight => {
    shareRightsDispatch({
      type: 'SET_ACCOUNT_HAS_ERROR',
      payload: {
        accountHasError:
          !isValidEmail(userRight.account) || isRepeatedAccount(userRight.account) || shareRightsState.accountNotFound
      }
    });

    if (!userRight.isNew && isPermissionChanged(userRight)) {
      onUpdateUser(userRight);
    } else {
      if (isValidEmail(userRight.account) && !shareRightsState.accountHasError) {
        onUpdateUser(userRight);
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

  const onEnterKey = (key, userRight) => {
    if (key === 'Enter' && isValidEmail(userRight.account) && isPermissionChanged(userRight)) {
      onUpdateUser(userRight);
    }
  };

  const onUpdateUser = async userRight => {
    if (!isNil(userRight.role)) {
      userRight.account = userRight.account.toLowerCase();
      setIsLoading(true);
      try {
        const response = await callEndPoint('update', userRight);

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

  const onWritePermissionChange = async (userRight, newWritePermission) => {
    const { userRightList } = shareRightsState;
    const [thisUser] = userRightList.filter(thisUser => thisUser.id === userRight.id);
    thisUser.role = newWritePermission;

    shareRightsDispatch({ type: 'ON_WRITE_PERMISSION_CHANGE', payload: { userRightList } });
  };

  const onSetAccount = inputValue => {
    const { userRightList } = shareRightsState;
    const [newUser] = userRightList.filter(userRight => userRight.isNew);
    newUser.account = inputValue;

    shareRightsDispatch({
      type: 'ON_SET_ACCOUNT',
      payload: {
        userRightList,
        accountHasError: !isValidEmail(inputValue) || isRepeatedAccount(inputValue),
        accountNotFound: false
      }
    });
  };

  const onToggleDeletingUser = value => {
    shareRightsDispatch({ type: 'TOGGLE_DELETING_USER', payload: { isDeleting: value } });
  };

  const renderDeleteColumnTemplate = userRight =>
    userRight.isNew ? null : (
      <ActionsColumn
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'ON_DELETE_USER',
            payload: { isDeleteDialogVisible: true, userRightToDelete: userRight }
          })
        }
      />
    );

  const renderRightsTypeColumnTemplate = userRight => {
    const userRightRoleOptions = userRight.isNew
      ? [{ label: resources.messages['selectRole'], role: '' }, ...roleOptions]
      : roleOptions;

    return (
      <>
        <select
          id={userType}
          onBlur={() => updateUser(userRight)}
          onChange={event => onWritePermissionChange(userRight, event.target.value)}
          onKeyDown={event => onEnterKey(event.key, userRight)}
          value={userRight.role}>
          {userRightRoleOptions.map(option => {
            return (
              <option className="p-dropdown-item" key={uuid.v4()} value={option.role}>
                {option.label}
              </option>
            );
          })}
        </select>
        <label className="srOnly" htmlFor={userType}>
          {placeholder}
        </label>
      </>
    );
  };

  const renderAccountTemplate = userRight => {
    const hasError = !isEmpty(userRight.account) && userRight.isNew && shareRightsState.accountHasError;

    return (
      <div className={`formField ${hasError ? 'error' : ''}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus={userRight.isNew}
          className={!userRight.isNew ? styles.disabledInput : ''}
          disabled={!userRight.isNew}
          id={isEmpty(userRight.account) ? 'emptyInput' : userRight.account}
          onBlur={() => updateUser(userRight)}
          onChange={event => onSetAccount(event.target.value)}
          placeholder={placeholder}
          value={userRight.account}
        />
        <label className="srOnly" htmlFor="emptyInput">
          {placeholder}
        </label>
      </div>
    );
  };

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.userRightList) ? (
          <Spinner style={{ top: 0 }} />
        ) : (
          <div className={styles.table}>
            {isLoading && <Spinner className={styles.spinner} style={{ top: 0, left: 0, zIndex: 6000 }} />}
            <DataTable value={shareRightsState.userRightList}>
              <Column body={renderAccountTemplate} header={columnHeader} />
              <Column body={renderRightsTypeColumnTemplate} header={resources.messages['rolesColumn']} />
              <Column
                body={renderDeleteColumnTemplate}
                className={styles.emptyTableHeader}
                header={deleteColumnHeader}
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
