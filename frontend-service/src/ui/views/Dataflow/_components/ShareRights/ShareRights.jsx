import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ShareRights.module.scss';
import { config } from 'conf';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { UserRightService } from 'core/services/UserRight';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

export const ShareRights = ({
  addConfirmHeader,
  addErrorNotificationKey,
  columnHeader,
  dataflowId,
  dataProviderId,
  deleteColumnHeader,
  deleteConfirmHeader,
  deleteConfirmMessage,
  deleteErrorNotificationKey,
  editConfirmHeader,
  getErrorNotificationKey,
  isUserRightManagementDialogVisible,
  placeholder,
  representativeId,
  roleOptions,
  setIsUserRightManagementDialogVisible,
  updateErrorNotificationKey,
  userType
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    accountNotFound: false,
    clonedUserRightList: [],
    dataUpdatedCount: 0,
    isDeleteDialogVisible: false,
    isDeletingUserRight: false,
    loadingStatus: { isActionButtonsLoading: false, isInitialLoading: true },
    isLoadingButton: false,
    userRight: { account: '', isNew: true, role: '' },
    userRightList: [],
    userRightToDelete: ''
  });

  const { loadingStatus, isLoadingButton } = shareRightsState;

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    getAllUsers();
  }, [shareRightsState.dataUpdatedCount]);

  useEffect(() => {
    if (!shareRightsState.userRight.isNew && dropdownRef.current && isUserRightManagementDialogVisible) {
      dropdownRef.current.focusInput.focus();
    }
  }, [dropdownRef.current, isUserRightManagementDialogVisible]);

  useInputTextFocus(isUserRightManagementDialogVisible, inputRef);

  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
  const notDeletableRoles = [config.permissions.roles.STEWARD.key, config.permissions.roles.CUSTODIAN.key];

  const callEndPoint = async (method, userRight) => {
    if (userType === 'reporter') {
      if (method === 'getAll') {
        return await UserRightService.allReporters(dataflowId, dataProvider);
      } else if (method === 'delete') {
        return await UserRightService.deleteReporter(shareRightsState.userRightToDelete, dataflowId, dataProvider);
      } else if (method === 'update') {
        return await UserRightService.updateReporter(userRight, dataflowId, dataProvider);
      }
    }

    if (userType === 'requester') {
      if (method === 'getAll') {
        return await UserRightService.allRequesters(dataflowId);
      } else if (method === 'delete') {
        return await UserRightService.deleteRequester(shareRightsState.userRightToDelete, dataflowId);
      } else if (method === 'update') {
        return await UserRightService.updateRequester(userRight, dataflowId);
      }
    }
  };

  const getAllUsers = async () => {
    if (shareRightsState.dataUpdatedCount !== 0) setLoadingStatus(true, false);

    try {
      const userRightList = await callEndPoint('getAll');

      shareRightsDispatch({
        type: 'GET_USER_RIGHT_LIST',
        payload: { userRightList, clonedUserRightList: cloneDeep(userRightList) }
      });
    } catch (error) {
      notificationContext.add({ type: getErrorNotificationKey });
    } finally {
      setLoadingStatus(false, false);
    }
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
    return sameAccounts.length > 0;
  };

  const isRoleChanged = userRight => {
    const [initialUser] = shareRightsState.clonedUserRightList.filter(fUserRight => fUserRight.id === userRight.id);

    if (userRight.isNew) {
      return true;
    }

    return JSON.stringify(initialUser?.role) !== JSON.stringify(userRight?.role);
  };

  const updateUserRight = () => {
    const { userRight } = shareRightsState;

    const isRepeated = userRight.isNew ? isRepeatedAccount(userRight.account) : false;

    const accountHasError = !isValidEmail(userRight.account) || isRepeated || shareRightsState.accountNotFound;

    shareRightsDispatch({ type: 'SET_ACCOUNT_HAS_ERROR', payload: { accountHasError } });

    if (!userRight.isNew && isRoleChanged(userRight)) {
      onUpdateUser(userRight);
    } else {
      if (isValidEmail(userRight.account) && !shareRightsState.accountHasError) {
        onUpdateUser(userRight);
      }
    }
  };

  const onDeleteUserRight = async () => {
    onToggleDeletingUser(true);

    try {
      const response = await callEndPoint('delete');
      if (response.status >= 200 && response.status <= 299) {
        onDataChange();
      }
    } catch (error) {
      notificationContext.add({ type: deleteErrorNotificationKey });
    } finally {
      onToggleDeletingUser(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onDataChange = () => shareRightsDispatch({ type: 'ON_DATA_CHANGE' });

  const onUpdateUser = async userRight => {
    if (userRight.role !== '') {
      userRight.account = userRight.account.toLowerCase();
      setIsButtonLoading(true);
      setLoadingStatus(true, false);

      try {
        const response = await callEndPoint('update', userRight);
        if (response.status >= 200 && response.status <= 299) {
          onDataChange();
        }
        onCloseManagementDialog();
      } catch (error) {
        if (error?.response?.status === 404) {
          shareRightsDispatch({
            type: 'SET_ACCOUNT_NOT_FOUND',
            payload: { accountNotFound: true, accountHasError: true }
          });
        }
        //change to 403
        else if (error?.response?.status === 500) {
          getAllUsers();
        } else {
          notificationContext.add({ type: userRight.isNew ? addErrorNotificationKey : updateErrorNotificationKey });
        }
      } finally {
        setIsButtonLoading(false);
      }
    }
  };

  const onRoleChange = newRole => shareRightsDispatch({ type: 'ON_ROLE_CHANGE', payload: { role: newRole } });

  const onSetAccount = inputValue => {
    shareRightsDispatch({
      type: 'ON_SET_ACCOUNT',
      payload: {
        account: inputValue,
        accountHasError: !isValidEmail(inputValue) || isRepeatedAccount(inputValue),
        accountNotFound: false
      }
    });
  };

  const onToggleDeletingUser = value => {
    shareRightsDispatch({ type: 'TOGGLE_DELETING_USER_RIGHT', payload: { isDeleting: value } });
  };

  const onCloseManagementDialog = () => {
    setIsUserRightManagementDialogVisible(false);
    shareRightsDispatch({ type: 'ON_CLOSE_MANAGEMENT_DIALOG' });
  };

  const onEnterKey = (key, userRight) => {
    if (
      key === 'Enter' &&
      isValidEmail(userRight.account) &&
      !shareRightsState.accountHasError &&
      isRoleChanged(userRight)
    ) {
      onUpdateUser(userRight);
    }
  };

  const onEditUserRight = userRight => {
    shareRightsDispatch({ type: 'ON_EDIT_USER_RIGHT', payload: { isEditing: true, userRight } });
    setIsUserRightManagementDialogVisible(true);
  };

  const setLoadingStatus = (isActionButtonsLoading, isInitialLoading) => {
    shareRightsDispatch({ type: 'SET_IS_LOADING', payload: { isActionButtonsLoading, isInitialLoading } });
  };

  const setIsButtonLoading = isLoadingButton => {
    shareRightsDispatch({ type: 'SET_IS_LOADING_BUTTON', payload: { isLoadingButton } });
  };

  const renderButtonsColumnTemplate = userRight =>
    notDeletableRoles.includes(userRight?.role) ? null : (
      <ActionsColumn
        disabledButtons={loadingStatus.isActionButtonsLoading}
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'ON_DELETE_USER_RIGHT',
            payload: { isDeleteDialogVisible: true, userRightToDelete: userRight }
          })
        }
        onEditClick={() => onEditUserRight(userRight)}
      />
    );

  const renderRoleColumnTemplate = userRight => {
    const [option] = roleOptions.filter(option => option.role === userRight.role);
    return <div>{option.label}</div>;
  };

  const renderRightManagement = () => {
    const { userRight } = shareRightsState;

    const hasError = !isEmpty(userRight.account) && userRight.isNew && shareRightsState.accountHasError;

    return (
      <div className={styles.manageDialog}>
        <div>
          <label className={styles.label} htmlFor="accountInput">
            {resources.messages['account']}
          </label>
          <InputText
            className={`formField ${hasError ? styles.error : ''}`}
            disabled={!userRight.isNew}
            id="accountInput"
            onChange={event => onSetAccount(event.target.value)}
            placeholder={placeholder}
            ref={inputRef}
            style={{ marginBottom: '0rem' }}
            value={userRight.account}
          />
        </div>
        <div>
          <label className={styles.label} htmlFor="rolesDropdown">
            {resources.messages['role']}
          </label>
          <Dropdown
            appendTo={document.body}
            id="rolesDropdown"
            onChange={event => onRoleChange(event.target.value.role)}
            onKeyPress={event => onEnterKey(event.key, userRight)}
            optionLabel="label"
            options={roleOptions}
            placeholder={resources.messages['selectRole']}
            ref={dropdownRef}
            value={first(roleOptions.filter(option => option.role === userRight.role))}
          />
        </div>
      </div>
    );
  };

  const renderAccountTemplate = userRight => <div>{userRight.account}</div>;

  if (loadingStatus.isInitialLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.userRightList) ? (
          <h3>{resources.messages[`${userType}EmptyUserRightList`]}</h3>
        ) : (
          <div className={styles.table}>
            <DataTable value={shareRightsState.userRightList}>
              <Column body={renderAccountTemplate} header={columnHeader} />
              <Column body={renderRoleColumnTemplate} header={resources.messages['rolesColumn']} />
              <Column
                body={renderButtonsColumnTemplate}
                className={styles.emptyTableHeader}
                header={deleteColumnHeader}
                style={{ width: '100px' }}
              />
            </DataTable>
          </div>
        )}
      </div>

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={shareRightsState.isDeletingUserRight}
          header={deleteConfirmHeader}
          iconConfirm={shareRightsState.isDeletingUserRight ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteUserRight()}
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

      {isUserRightManagementDialogVisible && (
        <ConfirmDialog
          disabledConfirm={
            isLoadingButton || (!shareRightsState.userRight.isNew && !isRoleChanged(shareRightsState.userRight))
          }
          header={shareRightsState.isEditing ? editConfirmHeader : addConfirmHeader}
          iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['cancel']}
          labelConfirm={resources.messages['save']}
          onConfirm={() => updateUserRight()}
          onHide={() => onCloseManagementDialog()}
          visible={isUserRightManagementDialogVisible}>
          {renderRightManagement()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
