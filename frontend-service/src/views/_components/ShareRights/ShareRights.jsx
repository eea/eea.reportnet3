import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ShareRights.module.scss';

import { config } from 'conf';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dropdown } from 'views/_components/Dropdown';
import { Filters } from 'views/_components/Filters';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';
import ReactTooltip from 'react-tooltip';

import { UserRightService } from 'services/UserRightService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const ShareRights = ({
  addConfirmHeader,
  addErrorNotificationKey,
  columnHeader,
  dataflowId,
  dataProviderId,
  deleteConfirmHeader,
  deleteConfirmMessage,
  deleteErrorNotificationKey,
  editConfirmHeader,
  getErrorNotificationKey,
  isAdmin = false,
  isUserRightManagementDialogVisible,
  placeholder,
  representativeId,
  roleOptions,
  setRightPermissionsChange = () => {},
  setIsUserRightManagementDialogVisible,
  updateErrorNotificationKey,
  userType
}) => {
  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
  const methodTypes = { DELETE: 'delete', GET_ALL: 'getAll', UPDATE: 'update' };
  const notDeletableRolesRequester = [config.permissions.roles.STEWARD.key, config.permissions.roles.CUSTODIAN.key];
  const userTypes = { REPORTER: 'reporter', REQUESTER: 'requester' };

  const filterOptions = [
    { type: 'input', properties: [{ name: 'account' }] },
    { type: 'multiselect', properties: [{ name: 'role' }] }
  ];

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    accountNotFound: false,
    actionsButtons: { id: null, isDeleting: false, isEditing: false },
    clonedUserRightList: [],
    dataUpdatedCount: 0,
    filteredData: [],
    isDeleteDialogVisible: false,
    isDeletingUserRight: false,
    isEditingModal: false,
    isLoadingButton: false,
    loadingStatus: { isActionButtonsLoading: false, isInitialLoading: true },
    pagination: { first: 0, page: 0, rows: 10 },
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

  const isValidEmail = email => RegularExpressions['email'].test(email);

  const isRepeatedAccount = account => {
    const sameAccounts = shareRightsState.userRightList.filter(userRight =>
      TextUtils.areEquals(userRight.account, account)
    );
    return sameAccounts.length > 0;
  };

  const hasEmptyData = userRight => isEmpty(userRight.account) || isEmpty(userRight.role);

  const isRoleChanged = userRight => {
    const [initialUser] = shareRightsState.clonedUserRightList.filter(fUserRight => fUserRight.id === userRight.id);

    if (userRight.isNew) {
      return true;
    }

    return !TextUtils.areEquals(JSON.stringify(initialUser?.role), JSON.stringify(userRight?.role));
  };

  const onCloseManagementDialog = () => {
    setIsUserRightManagementDialogVisible(false);
    shareRightsDispatch({ type: 'ON_CLOSE_MANAGEMENT_DIALOG' });
  };

  const onDataChange = () => {
    shareRightsDispatch({ type: 'ON_DATA_CHANGE', payload: { isDataUpdated: true } });
    setRightPermissionsChange(
      TextUtils.areEquals(userRight.account, userContext.email) ||
        TextUtils.areEquals(shareRightsState.userRightToDelete.account, userContext.email)
    );
  };

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

  const onPaginate = event => {
    const pagination = { first: event.first, page: event.page, rows: event.rows };
    shareRightsDispatch({ type: 'ON_PAGINATE', payload: { pagination } });
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
          return await UserRightService.getReporters(dataflowId, dataProvider);
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
          return await UserRightService.getRequesters(dataflowId);
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
      console.error('ShareRights - getAllUsers.', error);
      notificationContext.add({ type: getErrorNotificationKey }, true);
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
      await callEndPoint(methodTypes.DELETE);
      onDataChange();
    } catch (error) {
      console.error('ShareRights - onDeleteUserRight.', error);
      notificationContext.add({ type: deleteErrorNotificationKey }, true);
      onResetAll();
    } finally {
      onToggleDeletingUser(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onLoadFilteredData = userRightList => {
    shareRightsDispatch({ type: 'ON_LOAD_FILTERED_DATA', payload: { userRightList } });
  };

  const onUpdateUser = async userRight => {
    setActions({ isDeleting: false, isEditing: true });
    if (userRight.role !== '') {
      userRight.account = userRight.account.toLowerCase();
      setIsButtonLoading(true);
      setLoadingStatus({ isActionButtonsLoading: true, isInitialLoading: false });

      try {
        await callEndPoint(methodTypes.UPDATE, userRight);
        onDataChange();
        onCloseManagementDialog();
      } catch (error) {
        if (error?.response?.status === 404) {
          shareRightsDispatch({
            type: 'SET_ACCOUNT_NOT_FOUND',
            payload: { accountNotFound: true, accountHasError: true }
          });
          notificationContext.add({ type: 'EMAIL_NOT_FOUND_ERROR' }, true);
        } else if (error?.response?.status === 400) {
          shareRightsDispatch({ type: 'SET_ACCOUNT_HAS_ERROR', payload: { accountHasError: true } });
          notificationContext.add({ type: 'IMPOSSIBLE_ROLE_ERROR' }, true);
        } else {
          notificationContext.add(
            { type: userRight.isNew ? addErrorNotificationKey : updateErrorNotificationKey },
            true
          );
        }
      } finally {
        setIsButtonLoading(false);
      }
    }
  };

  const renderButtonsColumnTemplate = userRight => {
    if (userType === userTypes.REQUESTER && notDeletableRolesRequester.includes(userRight?.role) && !isAdmin) {
      return null;
    }

    return (
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
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="accountInput">
            {userType === userTypes.REQUESTER
              ? resourcesContext.messages['userRolesRequesterInputLabel']
              : resourcesContext.messages['userRolesReporterInputLabel']}
          </label>
          <InputText
            className={hasError ? styles.error : ''}
            disabled={!userRight.isNew}
            id="accountInput"
            onChange={event => onSetAccount(event.target.value)}
            placeholder={placeholder}
            ref={inputRef}
            style={{ margin: '0.3rem 0' }}
            value={userRight.account}
          />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="rolesDropdown">
            {resourcesContext.messages['role']}
          </label>
          <Dropdown
            appendTo={document.body}
            id="rolesDropdown"
            onChange={event => onRoleChange(event.target.value.role)}
            onKeyPress={event => onEnterKey(event.key, userRight)}
            optionLabel="label"
            options={roleOptions}
            placeholder={resourcesContext.messages['selectRole']}
            ref={dropdownRef}
            style={{ margin: '0.3rem 0' }}
            value={first(roleOptions.filter(option => option.role === userRight.role))}
          />
        </div>
      </div>
    );
  };

  const renderAccountTemplate = userRight => (
    <div className={styles.accountWrapper}>
      {userRight.account}
      <FontAwesomeIcon
        className={styles.isValidUserIcon}
        data-for={userRight.account}
        data-tip
        icon={userRight.isValid ? AwesomeIcons('userCheck') : AwesomeIcons('userTimes')}
        style={{
          color: 'var(--isValid-user-icon-color)',
          fontSize: '1.1rem'
        }}
      />
      <ReactTooltip border={true} effect="solid" id={userRight.account} place="top">
        {userRight.isValid
          ? resourcesContext.messages['validUserTooltip']
          : resourcesContext.messages['invalidUserTooltip']}
      </ReactTooltip>
    </div>
  );

  const renderDialogLayout = children => (
    <Fragment>
      <div
        className={styles.shareRightsModal}
        style={{ height: isEmpty(shareRightsState.userRightList) ? 0 : 'inherit' }}>
        {children}
      </div>
      {userType === userTypes.REPORTER && (
        <span className={styles.shareRightsDisclaimer}>{resourcesContext.messages['shareRightsDisclaimer']}</span>
      )}
    </Fragment>
  );

  const getTooltipMessage = userRight => {
    if (hasEmptyData(userRight)) {
      return resourcesContext.messages['incompleteDataTooltip'];
    } else if (userRight.isNew && isRepeatedAccount(userRight.account)) {
      return resourcesContext.messages['emailAlreadyAssignedTooltip'];
    } else if (!isValidEmail(userRight.account)) {
      return resourcesContext.messages['notValidEmailTooltip'];
    } else if (shareRightsState.accountHasError) {
      return resourcesContext.messages['emailHasErrorTooltip'];
    } else {
      return null;
    }
  };

  if (loadingStatus.isInitialLoading) return renderDialogLayout(<Spinner />);

  return renderDialogLayout(
    <Fragment>
      {!isEmpty(shareRightsState.userRightList) && (
        <Filters data={shareRightsState.userRightList} getFilteredData={onLoadFilteredData} options={filterOptions} />
      )}
      <div
        className={
          isEmpty(shareRightsState.userRightList)
            ? styles.wrapperNoUserRoles
            : isEmpty(shareRightsState.filteredData)
            ? styles.wrapperEmptyFilter
            : ''
        }>
        {isEmpty(shareRightsState.userRightList) ? (
          <h3>{resourcesContext.messages[`${userType}EmptyUserRightList`]}</h3>
        ) : isEmpty(shareRightsState.filteredData) ? (
          <h3>{resourcesContext.messages[`${userType}NotMatchingFilter`]}</h3>
        ) : (
          <div className={styles.table}>
            <DataTable
              first={shareRightsState.pagination.first}
              getPageChange={onPaginate}
              paginator={true}
              rows={shareRightsState.pagination.rows}
              rowsPerPageOptions={[5, 10, 15]}
              summary="shareRights"
              value={shareRightsState.filteredData}>
              <Column body={renderAccountTemplate} header={columnHeader} />
              <Column body={renderRoleColumnTemplate} header={resourcesContext.messages['rolesColumn']} />
              <Column
                body={renderButtonsColumnTemplate}
                header={resourcesContext.messages['actions']}
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
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
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
          confirmTooltip={getTooltipMessage(userRight)}
          dialogStyle={{ minWidth: '400px', maxWidth: '600px' }}
          disabledConfirm={
            hasEmptyData(userRight) ||
            isLoadingButton ||
            (!userRight.isNew && !isRoleChanged(userRight)) ||
            shareRightsState.accountHasError
          }
          header={shareRightsState.isEditingModal ? editConfirmHeader : addConfirmHeader}
          iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
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
