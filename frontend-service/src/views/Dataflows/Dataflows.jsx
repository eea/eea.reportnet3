import { Fragment, useContext, useEffect, useLayoutEffect, useReducer, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useResetRecoilState } from 'recoil';

import isNil from 'lodash/isNil';
import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import pull from 'lodash/pull';

import styles from './Dataflows.module.scss';

import { config } from 'conf';
import { DataflowsReporterHelpConfig } from 'conf/help/dataflows/reporter';
import { DataflowsRequesterHelpConfig } from 'conf/help/dataflows/requester';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataflowsList } from './_components/DataflowsList';
import { Dialog } from 'views/_components/Dialog';
import { GoTopButton } from 'views/_components/GoTopButton';
import { MainLayout } from 'views/_components/Layout';
import { ManageBusinessDataflow } from 'views/_components/ManageBusinessDataflow';
import { ManageDataflow } from 'views/_components/ManageDataflow';
import { ManageReferenceDataflow } from 'views/_components/ManageReferenceDataflow';
import { MyFilters } from 'views/_components/MyFilters';
import { ReportingObligations } from 'views/_components/ReportingObligations';
import { TabMenu } from './_components/TabMenu';
import { UserList } from 'views/_components/UserList';

import { BusinessDataflowService } from 'services/BusinessDataflowService';
import { CitizenScienceDataflowService } from 'services/CitizenScienceDataflowService';
import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';
import { UserService } from 'services/UserService';

import { dialogsStore } from 'views/_components/Dialog/_functions/Stores/dialogsStore';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { dataflowsReducer } from './_functions/Reducers/dataflowsReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useReportingObligations } from 'views/_components/ReportingObligations/_functions/Hooks/useReportingObligations';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowsUtils } from './_functions/Utils/DataflowsUtils';
import { ErrorUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const { parseDataflows, sortDataflows } = DataflowsUtils;
const { permissions } = config;

const Dataflows = () => {
  const { errorType: dataflowsErrorType } = useParams();

  const resetDialogsStore = useResetRecoilState(dialogsStore);

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataflowsState, dataflowsDispatch] = useReducer(dataflowsReducer, {
    activeIndex: 0,
    business: [],
    citizenScience: [],
    dataflowsCount: {},
    dataflowsCountFirstLoad: false,
    isAdmin: null,
    isBusinessDataflowDialogVisible: false,
    isCitizenScienceDataflowDialogVisible: false,
    isCustodian: null,
    isNationalCoordinator: false,
    isRecreatePermissionsDialogVisible: false,
    isReferencedDataflowDialogVisible: false,
    isReportingDataflowDialogVisible: false,
    isReportingObligationsDialogVisible: false,
    isValidatingAllDataflowsUsers: false,
    isUserListVisible: false,
    loadingStatus: { reporting: true, business: true, citizenScience: true, reference: true },
    reference: [],
    reporting: [],
    filteredData: { business: [], citizenScience: [], reference: [], reporting: [] },
    pinnedSeparatorIndex: -1
  });

  const { obligation, resetObligations, setObligationToPrevious, setCheckedObligation, setToCheckedObligation } =
    useReportingObligations();

  const {
    activeIndex,
    dataflowsCount,
    filteredData,
    isAdmin,
    isCustodian,
    isNationalCoordinator,
    loadingStatus,
    pinnedSeparatorIndex
  } = dataflowsState;

  const containerRef = useRef(null);

  const tabMenuItems =
    isCustodian || isAdmin
      ? [
          {
            className: styles.flow_tab,
            id: 'reporting',
            label: resourcesContext.messages['reportingDataflowsListTab']
          },
          { className: styles.flow_tab, id: 'business', label: resourcesContext.messages['businessDataflowsListTab'] },
          {
            className: styles.flow_tab,
            id: 'citizenScience',
            label: resourcesContext.messages['citizenScienceDataflowsListTab']
          },
          {
            className: styles.flow_tab,
            disabled: dataflowsState.dataflowsCountFirstLoad,
            id: 'reference',
            label: resourcesContext.messages['referenceDataflowsListTab']
          }
        ]
      : [
          {
            className: styles.flow_tab,
            id: 'reporting',
            label: resourcesContext.messages['reportingDataflowsListTab']
          },
          { className: styles.flow_tab, id: 'business', label: resourcesContext.messages['businessDataflowsListTab'] },
          {
            className: styles.flow_tab,
            id: 'citizenScience',
            label: resourcesContext.messages['citizenScienceDataflowsListTab']
          }
        ];

  const { tabId } = DataflowsUtils.getActiveTab(tabMenuItems, activeIndex);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOWS });

  useEffect(() => {
    getDataflowsCount();
    resetDialogsStore();

    if (!isNil(dataflowsErrorType)) {
      notificationContext.add({ type: ErrorUtils.parseErrorType(dataflowsErrorType) }, true);
    }
  }, []);

  useEffect(() => {
    leftSideBarContext.removeModels();

    const createReportingDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'reporting' && isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isReportingDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const createReferenceDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'reference' && isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isReferencedDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const createBusinessDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'business' && isAdmin,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isBusinessDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const createCitizenScienceDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'citizenScience' && isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isCitizenScienceDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const userListBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'users',
      isVisible: isNationalCoordinator,
      label: 'allDataflowsUserList',
      onClick: () => manageDialogs('isUserListVisible', true),
      title: 'allDataflowsUserList'
    };

    const adminCreateNewPermissionsBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'userShield',
      isVisible: isAdmin,
      label: 'adminCreatePermissions',
      onClick: () => manageDialogs('isRecreatePermissionsDialogVisible', true),
      title: 'adminCreatePermissions'
    };

    leftSideBarContext.addModels(
      [
        adminCreateNewPermissionsBtn,
        createBusinessDataflowBtn,
        createCitizenScienceDataflowBtn,
        createReferenceDataflowBtn,
        createReportingDataflowBtn,
        userListBtn
      ].filter(button => button.isVisible)
    );
  }, [isAdmin, isCustodian, isNationalCoordinator, tabId]);

  useEffect(() => {
    const messageStep0 = isCustodian ? 'dataflowListRequesterHelp' : 'dataflowListReporterHelp';
    leftSideBarContext.addHelpSteps(
      isCustodian ? DataflowsRequesterHelpConfig : DataflowsReporterHelpConfig,
      messageStep0
    );
  }, [dataflowsState]);

  useLayoutEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      onLoadPermissions();
      getDataflows();
    }
  }, [userContext.contextRoles]);

  useLayoutEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      getDataflows();
    }
  }, [tabId]);

  useEffect(() => {
    setActiveIndexTabOnBack();
  }, [isCustodian, isAdmin]);

  const setIsValidatingAllDataflowsUsers = isValidatingAllDataflowsUsers => {
    dataflowsDispatch({ type: 'SET_IS_VALIDATING_ALL_DATAFLOWS_USERS', payload: { isValidatingAllDataflowsUsers } });
  };

  const onValidatingAllDataflowsUsersCompleted = () => {
    setIsValidatingAllDataflowsUsers(false);
    manageDialogs('isRecreatePermissionsDialogVisible', false);
  };

  useCheckNotifications(['VALIDATE_ALL_REPORTERS_FAILED_EVENT'], setIsValidatingAllDataflowsUsers, false);
  useCheckNotifications(['VALIDATE_ALL_REPORTERS_COMPLETED_EVENT'], onValidatingAllDataflowsUsersCompleted);

  const setActiveIndexTabOnBack = () => {
    for (let tabItemIndex = 0; tabItemIndex < tabMenuItems.length; tabItemIndex++) {
      const tabItem = tabMenuItems[tabItemIndex];

      const [currentTabDataflowType] = Object.keys(config.dataflowType).filter(
        type => config.dataflowType[type].key === tabItem.id
      );

      if (TextUtils.areEquals(currentTabDataflowType, userContext.currentDataflowType)) {
        onChangeTab(tabItemIndex);
      }
    }
  };

  const setStatusDataflowLabel = dataflows =>
    dataflows.map(dataflow => {
      dataflow.statusKey = dataflow.status;
      if (dataflow.status === config.dataflowStatus.OPEN) {
        dataflow.status = dataflow.isReleasable
          ? resourcesContext.messages['open'].toUpperCase()
          : resourcesContext.messages['closed'].toUpperCase();
      } else {
        dataflow.status = resourcesContext.messages['design'].toUpperCase();
      }
      return dataflow;
    });

  const getDataflows = async () => {
    setLoading(true);

    try {
      if (TextUtils.areEquals(tabId, 'reporting')) {
        const data = await DataflowService.getAll(userContext.accessRole, userContext.contextRoles);
        setStatusDataflowLabel(data);
        setDataflows({ dataflows: data, type: 'reporting' });
      } else if (TextUtils.areEquals(tabId, 'reference')) {
        const data = await ReferenceDataflowService.getAll(userContext.accessRole, userContext.contextRoles);
        setStatusDataflowLabel(data);
        setDataflows({ dataflows: data, type: 'reference' });
      } else if (TextUtils.areEquals(tabId, 'business')) {
        const data = await BusinessDataflowService.getAll(userContext.accessRole, userContext.contextRoles);
        setStatusDataflowLabel(data);
        setDataflows({ dataflows: data, type: 'business' });
      } else if (TextUtils.areEquals(tabId, 'citizenScience')) {
        const data = await CitizenScienceDataflowService.getAll(userContext.accessRole, userContext.contextRoles);
        setStatusDataflowLabel(data);
        setDataflows({ dataflows: data, type: 'citizenScience' });
      }
    } catch (error) {
      console.error('Dataflows - getDataflows.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' }, true);
    } finally {
      setLoading(false);
    }
  };

  const getDataflowsCount = async () => {
    setLoading(true);

    try {
      const data = await DataflowService.countByType();
      dataflowsDispatch({ type: 'SET_DATAFLOWS_COUNT', payload: data });
    } catch (error) {
      console.error('Dataflows - getDataflows.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' }, true);
    }
  };

  const getFilteredData = data => {
    const orderedFilteredData = sortDataflows(data);
    const orderedPinned = orderedFilteredData.map(el => el.pinned);

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf('pinned'));
    dataflowsDispatch({ type: 'GET_FILTERED_DATA', payload: { type: tabId, data } });
  };

  const manageDialogs = (dialog, value) => {
    dataflowsDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });
  };

  const onConfirmValidateAllDataflowsUsers = async () => {
    setIsValidatingAllDataflowsUsers(true);
    try {
      await DataflowService.validateAllDataflowsUsers();
    } catch (error) {
      console.error('Dataflows -  onConfirmValidateAllDataflowsUsers.', error);
      setIsValidatingAllDataflowsUsers(false);
      notificationContext.add({ type: 'VALIDATE_ALL_REPORTERS_FAILED_EVENT' }, true);
    }
  };

  const onCreateDataflow = dialog => {
    manageDialogs(dialog, false);
    onRefreshToken();
  };

  const onChangeTab = (index, value) => {
    if (!isNil(value)) {
      const [currentTabDataflowType] = Object.keys(config.dataflowType).filter(
        type => config.dataflowType[type].key === value.id
      );
      userContext.setCurrentDataflowType(currentTabDataflowType);
    }
    dataflowsDispatch({ type: 'ON_CHANGE_TAB', payload: { index } });
  };

  const onHideObligationDialog = () => {
    manageDialogs('isReportingObligationsDialogVisible', false);
    setObligationToPrevious();
  };

  const onLoadPermissions = () => {
    const isAdmin = userContext.hasPermission([permissions.roles.ADMIN.key]);
    const isCustodian = userContext.hasPermission([permissions.roles.CUSTODIAN.key, permissions.roles.STEWARD.key]);

    const isNationalCoordinator = userContext.hasContextAccessPermission(
      permissions.prefixes.NATIONAL_COORDINATOR,
      null,
      [permissions.roles.NATIONAL_COORDINATOR.key]
    );

    dataflowsDispatch({ type: 'HAS_PERMISSION', payload: { isAdmin, isCustodian, isNationalCoordinator } });
  };

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      userContext.onTokenRefresh(userObject);
    } catch (error) {
      console.error('Dataflows - onRefreshToken.', error);
      await UserService.logout();
      userContext.onLogout();
    }
  };

  const onReorderPinnedDataflows = async (pinnedItem, isPinned) => {
    const { business, citizenScience, reference, reporting } = dataflowsState;

    const _data = [...reporting, ...reference, ...business, ...citizenScience];
    const _filteredData = [...filteredData[tabId]];

    const userProperties = updateUserPropertiesPinnedDataflows({ pinnedItem, data: _data });

    await onUpdateUserProperties(userProperties);
    userContext.onChangePinnedDataflows(userProperties.pinnedDataflows);

    const changedInitialData = onUpdatePinnedStatus({ dataflows: dataflowsState[tabId], isPinned, pinnedItem });
    const changedFilteredData = onUpdatePinnedStatus({ dataflows: _filteredData, isPinned, pinnedItem });

    const orderedFilteredData = sortDataflows(changedFilteredData);
    const orderedPinned = orderedFilteredData.map(el => el.pinned);

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf('pinned'));

    dataflowsDispatch({
      type: 'SET_DATAFLOWS',
      payload: {
        contextCurrentDataflowType: userContext.currentDataflowType,
        data: sortDataflows(changedInitialData),
        type: tabId
      }
    });

    const notificationType = isPinned ? 'DATAFLOW_PINNED_INIT' : 'DATAFLOW_UNPINNED_INIT';
    notificationContext.add(
      { content: { customContent: { dataflowName: pinnedItem.name } }, type: notificationType },
      true
    );
  };

  const onUpdatePinnedStatus = ({ dataflows = [], isPinned, pinnedItem }) => {
    return dataflows.map(dataflow => {
      let _dataflow = { ...dataflow };

      if (_dataflow.id === pinnedItem.id) _dataflow.pinned = isPinned ? 'pinned' : 'unpinned';

      return _dataflow;
    });
  };

  const onUpdateUserProperties = async userProperties => {
    try {
      return await UserService.updateConfiguration(userProperties);
    } catch (error) {
      console.error('DataflowsList - changeUserProperties.', error);
      notificationContext.add({ type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR' }, true);
    }
  };

  const updateUserPropertiesPinnedDataflows = ({ data = [], pinnedItem }) => {
    const userProperties = { ...userContext.userProps };
    const pinnedDataflows = intersection(
      userProperties.pinnedDataflows,
      data.map(data => data.id.toString())
    );

    if (!isEmpty(pinnedDataflows) && pinnedDataflows.includes(pinnedItem.id.toString())) {
      pull(pinnedDataflows, pinnedItem.id.toString());
    } else {
      pinnedDataflows.push(pinnedItem.id.toString());
    }

    userProperties.pinnedDataflows = pinnedDataflows;

    return userProperties;
  };

  const setDataflows = ({ dataflows = [], type }) => {
    const parsedDataflows = parseDataflows(dataflows, userContext.userProps.pinnedDataflows);
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
    dataflowsDispatch({
      type: 'SET_DATAFLOWS',
      payload: { contextCurrentDataflowType: userContext.currentDataflowType, data: parsedDataflows, type }
    });
  };

  const setLoading = status => dataflowsDispatch({ type: 'SET_LOADING', payload: { tab: tabId, status } });

  const setPinnedSeparatorIndex = index => dataflowsDispatch({ type: 'SET_PINNED_INDEX', payload: { index } });

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const renderUserListDialogFooter = () => (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs('isUserListVisible', false)}
    />
  );

  const renderObligationFooter = () => (
    <Fragment>
      <Button
        icon="check"
        label={resourcesContext.messages['ok']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setToCheckedObligation();
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setObligationToPrevious();
        }}
      />
    </Fragment>
  );

  const _dataflowsOptions = [
    {
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'], order: 0, isSortable: true },
        { key: 'description', label: resourcesContext.messages['description'], order: 1, isSortable: true },
        { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'], order: 2, isSortable: true },
        { key: 'obligationTitle', label: resourcesContext.messages['obligation'], order: 3, isSortable: true },
        { key: 'obligationId', label: resourcesContext.messages['obligationId'], order: 4, isSortable: true }
      ],
      type: 'INPUT'
    },
    {
      nestedOptions: [
        {
          key: 'status',
          label: resourcesContext.messages['status'],
          order: 0,
          category: 'LEVEL_ERROR',
          isSortable: true
        },
        { key: 'userRole', label: resourcesContext.messages['userRole'], order: 1, isSortable: true },
        { key: 'pinned', label: resourcesContext.messages['pinned'], order: 2 }
      ],
      type: 'MULTI_SELECT'
    },
    {
      isSortable: true,
      key: 'expirationDate',
      label: resourcesContext.messages['expirationDateFilterLabel'],
      type: 'DATE'
    },
    (isCustodian || isAdmin) && {
      isSortable: true,
      key: 'creationDate',
      label: resourcesContext.messages['creationDateFilterLabel'],
      type: 'DATE'
    }
  ];

  const _referencesOptions = [
    {
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'], isSortable: true },
        { key: 'description', label: resourcesContext.messages['description'], isSortable: true }
      ],
      type: 'INPUT'
    },
    {
      nestedOptions: [
        { key: 'status', label: resourcesContext.messages['status'], category: 'LEVEL_ERROR', isSortable: true },
        { key: 'pinned', label: resourcesContext.messages['pinned'], isSortable: true }
      ],
      type: 'MULTI_SELECT'
    }
  ];

  const options = {
    business: _dataflowsOptions,
    citizenScience: _dataflowsOptions,
    reference: _referencesOptions,
    reporting: _dataflowsOptions
  };

  return renderLayout(
    <div className="rep-row">
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-12 dataflowList-help-step`}>
        <div ref={containerRef}>
          <TabMenu
            activeIndex={activeIndex}
            headerLabelChildrenCount={dataflowsCount}
            headerLabelLoading={loadingStatus}
            model={tabMenuItems}
            onTabChange={event => onChangeTab(event.index, event.value)}
          />
        </div>
        <MyFilters
          data={dataflowsState[tabId]}
          getFilteredData={getFilteredData}
          options={options[tabId]}
          viewType={tabId}
        />
        <DataflowsList
          className="dataflowList-accepted-help-step"
          filteredData={filteredData[tabId]}
          isAdmin={isAdmin}
          isCustodian={isCustodian}
          isLoading={loadingStatus[tabId]}
          pinnedSeparatorIndex={pinnedSeparatorIndex}
          reorderDataflows={onReorderPinnedDataflows}
          visibleTab={tabId}
        />
      </div>

      <GoTopButton parentRef={containerRef} referenceMargin={70} />

      {dataflowsState.isUserListVisible && (
        <Dialog
          footer={renderUserListDialogFooter()}
          header={resourcesContext.messages['allDataflowsUserListHeader']}
          onHide={() => manageDialogs('isUserListVisible', false)}
          visible={dataflowsState.isUserListVisible}>
          <UserList />
        </Dialog>
      )}

      {dataflowsState.isReferencedDataflowDialogVisible && (
        <ManageReferenceDataflow
          isVisible={dataflowsState.isReferencedDataflowDialogVisible}
          manageDialogs={manageDialogs}
          onCreateDataflow={onCreateDataflow}
        />
      )}

      {dataflowsState.isBusinessDataflowDialogVisible && (
        <ManageBusinessDataflow
          isAdmin={isAdmin}
          isVisible={dataflowsState.isBusinessDataflowDialogVisible}
          manageDialogs={manageDialogs}
          obligation={obligation}
          onCreateDataflow={onCreateDataflow}
          resetObligations={resetObligations}
        />
      )}

      {dataflowsState.isRecreatePermissionsDialogVisible && (
        <ConfirmDialog
          disabledConfirm={dataflowsState.isValidatingAllDataflowsUsers}
          header={resourcesContext.messages['adminNewCreatePermissions']}
          iconConfirm={dataflowsState.isValidatingAllDataflowsUsers ? 'spinnerAnimate' : ''}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmValidateAllDataflowsUsers}
          onHide={() => manageDialogs('isRecreatePermissionsDialogVisible', false)}
          visible={dataflowsState.isRecreatePermissionsDialogVisible}>
          {resourcesContext.messages['confirmCreateNewPermissions']}
        </ConfirmDialog>
      )}

      {dataflowsState.isCitizenScienceDataflowDialogVisible && (
        <ManageDataflow
          isCitizenScienceDataflow
          isVisible={dataflowsState.isCitizenScienceDataflowDialogVisible}
          manageDialogs={manageDialogs}
          obligation={obligation}
          onCreateDataflow={onCreateDataflow}
          resetObligations={resetObligations}
          state={dataflowsState}
        />
      )}

      {dataflowsState.isReportingDataflowDialogVisible && (
        <ManageDataflow
          isVisible={dataflowsState.isReportingDataflowDialogVisible}
          manageDialogs={manageDialogs}
          obligation={obligation}
          onCreateDataflow={onCreateDataflow}
          resetObligations={resetObligations}
          state={dataflowsState}
        />
      )}

      {dataflowsState.isReportingObligationsDialogVisible && (
        <Dialog
          footer={renderObligationFooter()}
          header={resourcesContext.messages['reportingObligations']}
          onHide={onHideObligationDialog}
          style={{ width: '95%' }}
          visible={dataflowsState.isReportingObligationsDialogVisible}>
          <ReportingObligations obligationChecked={obligation} setCheckedObligation={setCheckedObligation} />
        </Dialog>
      )}
    </div>
  );
};

export { Dataflows };
