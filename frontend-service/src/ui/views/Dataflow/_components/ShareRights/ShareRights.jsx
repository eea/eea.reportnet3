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
import { RegularExpressions } from 'ui/views/_functions/Utils/RegularExpressions';

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
  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
  const methodTypes = { DELETE: 'delete', GET_ALL: 'getAll', UPDATE: 'update' };
  const notDeletableRoles = [config.permissions.roles.STEWARD.key, config.permissions.roles.CUSTODIAN.key];
  const userTypes = { REPORTER: 'reporter', REQUESTER: 'requester' };

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    accountNotFound: false,
    actionsButtons: { id: null, isDeleting: false, isEditing: false },
    clonedUserRightList: [],
    dataUpdatedCount: 0,
    isDeleteDialogVisible: false,
    isDeletingUserRight: false,
    isEditingModal: false,
    isLoadingButton: false,
    loadingStatus: { isActionButtonsLoading: false, isInitialLoading: true },
    userRight: { account: '', isNew: true, role: '' },
    userRightList: [],
    userRightToDelete: {}
  });

  const { actionsButtons, isLoadingButton, loadingStatus, userRight } = shareRightsState;

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    getAllUsers();
  }, [shareRightsState.dataUpdatedCount]);

  useEffect(() => {
    if (!userRight.isNew && dropdownRef.current && isUserRightManagementDialogVisible) {
      dropdownRef.current.focusInput.focus();
    }
  }, [dropdownRef.current, isUserRightManagementDialogVisible]);

  useInputTextFocus(isUserRightManagementDialogVisible, inputRef);

  const isValidEmail = email => {
    return RegularExpressions['email'].test(email);
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

  const onCloseManagementDialog = () => {
    setIsUserRightManagementDialogVisible(false);
    shareRightsDispatch({ type: 'ON_CLOSE_MANAGEMENT_DIALOG' });
  };

  const onDataChange = () => shareRightsDispatch({ type: 'ON_DATA_CHANGE' });

  const onEditUserRight = userRight => {
    shareRightsDispatch({ type: 'ON_EDIT_USER_RIGHT', payload: { isEditingModal: true, userRight } });
    setIsUserRightManagementDialogVisible(true);
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

  const onResetAll = () => shareRightsDispatch({ type: 'ON_RESET_ALL' });

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

  const setActions = ({ isDeleting, isEditing }) => {
    shareRightsDispatch({ type: 'SET_ACTIONS', payload: { isDeleting, isEditing } });
  };

  const setIsButtonLoading = isLoadingButton => {
    shareRightsDispatch({ type: 'SET_IS_LOADING_BUTTON', payload: { isLoadingButton } });
  };

  const setLoadingStatus = ({ isActionButtonsLoading, isInitialLoading }) => {
    shareRightsDispatch({ type: 'SET_IS_LOADING', payload: { isActionButtonsLoading, isInitialLoading } });
  };

  const setUserRightId = id => shareRightsDispatch({ type: 'SET_USER_RIGHT_ID', payload: { id } });

  const callEndPoint = async (method, userRight) => {
    if (userType === userTypes.REPORTER) {
      switch (method) {
        case methodTypes.DELETE:
          return await UserRightService.deleteReporter(shareRightsState.userRightToDelete, dataflowId, dataProvider);

        case methodTypes.GET_ALL:
          return await UserRightService.allReporters(dataflowId, dataProvider);

        case methodTypes.UPDATE:
          return await UserRightService.updateReporter(userRight, dataflowId, dataProvider);

        default:
          break;
      }
    }

    if (userType === userTypes.REQUESTER) {
      switch (method) {
        case methodTypes.DELETE:
          return await UserRightService.deleteRequester(shareRightsState.userRightToDelete, dataflowId);

        case methodTypes.GET_ALL:
          return await UserRightService.allRequesters(dataflowId);

        case methodTypes.UPDATE:
          return await UserRightService.updateRequester(userRight, dataflowId);

        default:
          break;
      }
    }
  };

  const getAllUsers = async () => {
    if (shareRightsState.dataUpdatedCount !== 0) {
      setLoadingStatus({ isActionButtonsLoading: true, isInitialLoading: false });
    }

    try {
      const userRightList = await callEndPoint(methodTypes.GET_ALL);

      shareRightsDispatch({
        type: 'GET_USER_RIGHT_LIST',
        payload: { userRightList, clonedUserRightList: cloneDeep(userRightList) }
      });
    } catch (error) {
      notificationContext.add({ type: getErrorNotificationKey });
    } finally {
      onResetAll();
    }
  };

  const updateUserRight = () => {
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
    setActions({ isDeleting: true, isEditing: false });

    try {
      const response = await callEndPoint(methodTypes.DELETE);
      if (response.status >= 200 && response.status <= 299) {
        onDataChange();
      }
    } catch (error) {
      notificationContext.add({ type: deleteErrorNotificationKey });
      onResetAll();
    } finally {
      onToggleDeletingUser(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onUpdateUser = async userRight => {
    setActions({ isDeleting: false, isEditing: true });
    if (userRight.role !== '') {
      userRight.account = userRight.account.toLowerCase();
      setIsButtonLoading(true);
      setLoadingStatus({ isActionButtonsLoading: true, isInitialLoading: false });

      try {
        const response = await callEndPoint(methodTypes.UPDATE, userRight);
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
        } else if (error?.response?.status === 403) {
          shareRightsDispatch({ type: 'SET_ACCOUNT_HAS_ERROR', payload: { accountHasError: true } });
        } else {
          notificationContext.add({ type: userRight.isNew ? addErrorNotificationKey : updateErrorNotificationKey });
        }
      } finally {
        setIsButtonLoading(false);
      }
    }
  };

  const renderButtonsColumnTemplate = userRight => {
    return notDeletableRoles.includes(userRight?.role) ? null : (
      <ActionsColumn
        disabledButtons={isNil(actionsButtons.id) && loadingStatus.isActionButtonsLoading}
        isDeletingDocument={actionsButtons.isDeleting}
        isUpdating={actionsButtons.isEditing}
        onDeleteClick={() => {
          setUserRightId(userRight.id);
          shareRightsDispatch({
            type: 'ON_DELETE_USER_RIGHT',
            payload: { isDeleteDialogVisible: true, userRightToDelete: userRight }
          });
        }}
        onEditClick={() => {
          setUserRightId(userRight.id);
          onEditUserRight(userRight);
        }}
        rowDataId={userRight.id}
        rowDeletingId={actionsButtons.id}
        rowUpdatingId={actionsButtons.id}
      />
    );
  };

  const renderRoleColumnTemplate = userRight => {
    const [option] = roleOptions.filter(option => option.role === userRight.role);
    return <div>{option.label}</div>;
  };

  const renderRightManagement = () => {
    const hasError = !isEmpty(userRight.account) && userRight.isNew && shareRightsState.accountHasError;

    return (
      <div className={styles.manageDialog}>
        <div>
          <label className={styles.label} htmlFor="accountInput">
            {resources.messages['account']}
          </label>
          <InputText
            className={hasError ? styles.error : ''}
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
          onHide={() => {
            onResetAll();
            shareRightsDispatch({
              type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG',
              payload: { isDeleteDialogVisible: false }
            });
          }}
          visible={shareRightsState.isDeleteDialogVisible}>
          {deleteConfirmMessage}
        </ConfirmDialog>
      )}

      {isUserRightManagementDialogVisible && (
        <ConfirmDialog
          disabledConfirm={isLoadingButton || (!userRight.isNew && !isRoleChanged(userRight))}
          header={shareRightsState.isEditingModal ? editConfirmHeader : addConfirmHeader}
          iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['cancel']}
          labelConfirm={resources.messages['save']}
          onConfirm={() => updateUserRight()}
          onHide={() => {
            onResetAll();
            onCloseManagementDialog();
          }}
          visible={isUserRightManagementDialogVisible}>
          {renderRightManagement()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
