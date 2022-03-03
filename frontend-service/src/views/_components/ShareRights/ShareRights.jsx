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
import { MyFilters } from 'views/_components/MyFilters';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';
import ReactTooltip from 'react-tooltip';

import { UserRightService } from 'services/UserRightService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useFilters } from 'views/_functions/Hooks/useFilters';
import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
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
  saveErrorNotificationKey,
  setHasReporters = () => {},
  setIsUserRightManagementDialogVisible,
  setRightPermissionsChange = () => {},
  updateErrorNotificationKey,
  userType
}) => {
  const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
  const notDeletableRolesRequester = [config.permissions.roles.STEWARD.key, config.permissions.roles.CUSTODIAN.key];
  const userTypes = { REPORTER: 'reporter', REQUESTER: 'requester' };

  const { filteredData, isFiltered } = useFilters('shareRights');

  const isReporterManagement = userType === userTypes.REPORTER;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

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
    setHasReporters(!isEmpty(shareRightsState.userRightList));
  }, [shareRightsState.userRightList]);

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

  useCheckNotifications(['VALIDATE_REPORTERS_COMPLETED_EVENT'], onDataChange);

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

  const getAllUserRights = () => {
    if (isReporterManagement) {
      return UserRightService.getReporters(dataflowId, dataProvider);
    } else {
      return UserRightService.getRequesters(dataflowId);
    }
  };

  const updateUserRights = userRightToUpdate => {
    if (isReporterManagement) {
      return UserRightService.updateReporter(userRightToUpdate, dataflowId, dataProvider);
    } else {
      return UserRightService.updateRequester(userRightToUpdate, dataflowId);
    }
  };

  const deleteUserRights = () => {
    if (isReporterManagement) {
      return UserRightService.deleteReporter(shareRightsState.userRightToDelete, dataflowId, dataProvider);
    } else {
      return UserRightService.deleteRequester(shareRightsState.userRightToDelete, dataflowId);
    }
  };

  const getAllUsers = async () => {
    if (shareRightsState.dataUpdatedCount !== 0) {
      setLoadingStatus({ isActionButtonsLoading: true, isInitialLoading: false });
    }

    try {
      const userRightsListResponse = await getAllUserRights();
      const userRightList = userRightsListResponse.map(item => ({
        ...item,
        filteredRole: roleOptions.find(option => option.role === item.role)?.label
      }));

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
      await deleteUserRights();
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

  const onUpdateUser = async userRight => {
    setActions({ isDeleting: false, isEditing: true });
    if (userRight.role !== '') {
      userRight.account = userRight.account.toLowerCase();
      setIsButtonLoading(true);
      setLoadingStatus({ isActionButtonsLoading: true, isInitialLoading: false });

      try {
        await updateUserRights(userRight);
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
          notificationContext.add({ type: saveErrorNotificationKey }, true);
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
    if (!isReporterManagement && notDeletableRolesRequester.includes(userRight?.role) && !isAdmin) {
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
            {isReporterManagement
              ? resourcesContext.messages['userRolesReporterInputLabel']
              : resourcesContext.messages['userRolesRequesterInputLabel']}
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

  const renderIsValidUserIcon = userRight => {
    if (isReporterManagement) {
      return (
        <Fragment>
          <FontAwesomeIcon
            className={styles.isValidUserIcon}
            data-for={userRight.account}
            data-tip
            icon={userRight.isValid ? AwesomeIcons('userCheck') : AwesomeIcons('userTimes')}
          />
          <ReactTooltip border={true} effect="solid" id={userRight.account} place="top">
            {userRight.isValid
              ? resourcesContext.messages['validUserTooltip']
              : resourcesContext.messages['invalidUserTooltip']}
          </ReactTooltip>
        </Fragment>
      );
    }
  };

  const renderAccountTemplate = userRight => (
    <div className={styles.accountWrapper}>
      {userRight.account}
      {renderIsValidUserIcon(userRight)}
    </div>
  );

  const renderDisclaimer = () => {
    if (isReporterManagement) {
      return <span className={styles.shareRightsDisclaimer}>{resourcesContext.messages['shareRightsDisclaimer']}</span>;
    }
  };

  const renderDialogLayout = children => (
    <Fragment>
      <div
        className={styles.shareRightsModal}
        style={{ height: isEmpty(shareRightsState.userRightList) ? 0 : 'inherit' }}>
        {children}
      </div>
      {renderDisclaimer()}
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

  if (loadingStatus.isInitialLoading) {
    return renderDialogLayout(<Spinner />);
  }

  const filterOptions = [
    { key: 'account', label: resourcesContext.messages['account'], type: 'INPUT' },
    {
      key: 'filteredRole',
      label: resourcesContext.messages['role'],
      type: 'MULTI_SELECT'
    }
  ];

  const renderFilters = () => {
    if (!isEmpty(shareRightsState.userRightList)) {
      return (
        <MyFilters
          className="lineItems"
          data={shareRightsState.userRightList}
          options={filterOptions}
          viewType="shareRights"
        />
      );
    }
  };

  const renderShareRightsTable = () => {
    if (isEmpty(shareRightsState.userRightList)) {
      return (
        <div className={getShareRightsTableStyles()}>{resourcesContext.messages[`${userType}EmptyUserRightList`]}</div>
      );
    }

    if (isEmpty(filteredData)) {
      return (
        <div className={getShareRightsTableStyles()}>{resourcesContext.messages[`${userType}NotMatchingFilter`]}</div>
      );
    }

    return (
      <div className={styles.table}>
        <DataTable
          autoLayout
          className={styles.dialogContent}
          hasDefaultCurrentPage
          paginator
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={shareRightsState.userRightList.length}
              filteredDataLength={filteredData.length}
              isFiltered={isFiltered}
            />
          }
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={filteredData.length}
          value={filteredData}>
          {getShareRightsColumns()}
        </DataTable>
      </div>
    );
  };

  const getShareRightsColumns = () => {
    const columns = [
      {
        key: 'account',
        header: columnHeader,
        template: renderAccountTemplate
      },
      {
        key: 'role',
        header: resourcesContext.messages['rolesColumn'],
        template: renderRoleColumnTemplate
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: renderButtonsColumnTemplate
      }
    ];

    return columns.map(column => (
      <Column body={column.template} field={column.key} header={column.header} key={column.key} sortable />
    ));
  };

  const getShareRightsTableStyles = () => {
    if (!isEmpty(shareRightsState.userRightList)) {
      return styles.wrapperNoUserRoles;
    } else {
      if (isEmpty(filteredData)) {
        return styles.wrapperEmptyFilter;
      } else {
        return '';
      }
    }
  };

  return renderDialogLayout(
    <Fragment>
      {renderFilters()}
      {renderShareRightsTable()}

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={shareRightsState.isDeletingUserRight}
          header={deleteConfirmHeader}
          iconConfirm={shareRightsState.isDeletingUserRight ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteUserRight}
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
          onConfirm={updateUserRight}
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
