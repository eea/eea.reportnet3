import { Fragment, useContext, useEffect, useLayoutEffect, useReducer, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useRecoilValue, useResetRecoilState } from 'recoil';

import isNil from 'lodash/isNil';
import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import pull from 'lodash/pull';
import ReactTooltip from 'react-tooltip';

import styles from './Dataflows.module.scss';

import { config } from 'conf';
import { DataflowsReporterHelpConfig } from 'conf/help/dataflows/reporter';
import { DataflowsRequesterHelpConfig } from 'conf/help/dataflows/requester';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataflowsList } from './_components/DataflowsList';
import { Dialog } from 'views/_components/Dialog';
import { GoTopButton } from 'views/_components/GoTopButton';
import { InputText } from 'views/_components/InputText';
import { MainLayout } from 'views/_components/Layout';
import { ManageBusinessDataflow } from 'views/_components/ManageBusinessDataflow';
import { ManageDataflow } from 'views/_components/ManageDataflow';
import { ManageReferenceDataflow } from 'views/_components/ManageReferenceDataflow';
import { ManageWebforms } from './_components/ManageWebforms';
import { MyFilters } from 'views/_components/MyFilters';
import { Paginator } from 'views/_components/DataTable/_components/Paginator';
import { ReportingObligations } from 'views/_components/ReportingObligations';
import { TabMenu } from './_components/TabMenu';
import { UserList } from 'views/_components/UserList';

import { BusinessDataflowService } from 'services/BusinessDataflowService';
import { CitizenScienceDataflowService } from 'services/CitizenScienceDataflowService';
import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';
import { UserService } from 'services/UserService';

import { dialogsStore } from 'views/_components/Dialog/_functions/Stores/dialogsStore';
import { filterByState, sortByState } from 'views/_components/MyFilters/_functions/Stores/filtersStores';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { dataflowsReducer } from './_functions/Reducers/dataflowsReducer';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useFilters } from 'views/_functions/Hooks/useFilters';
import { useReportingObligations } from 'views/_components/ReportingObligations/_functions/Hooks/useReportingObligations';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowsUtils } from './_functions/Utils/DataflowsUtils';
import { ErrorUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const { permissions } = config;

export const Dataflows = () => {
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
    filteredRecords: 0,
    goToPage: 1,
    isAdmin: null,
    isBusinessDataflowDialogVisible: false,
    isCitizenScienceDataflowDialogVisible: false,
    isCustodian: null,
    isFiltered: false,
    isManageWebformsDialogVisible: false,
    isNationalCoordinator: false,
    isRecreatePermissionsDialogVisible: false,
    isReferencedDataflowDialogVisible: false,
    isReportingDataflowDialogVisible: false,
    isReportingObligationsDialogVisible: false,
    isUserListVisible: false,
    isValidatingAllDataflowsUsers: false,
    loadingStatus: { reporting: true, business: true, citizenScience: true, reference: true },
    pageInputTooltip: resourcesContext.messages['currentPageInfoMessage'],
    pagination: { firstRow: 0, numberRows: 100, pageNum: 0 },
    pinnedSeparatorIndex: -1,
    reference: [],
    reporting: [],
    totalRecords: 0
  });

  const { obligation, resetObligations, setObligationToPrevious, setCheckedObligation, setToCheckedObligation } =
    useReportingObligations();

  const {
    activeIndex,
    dataflowsCount,
    filteredRecords,
    goToPage,
    isAdmin,
    isCustodian,
    isFiltered,
    isNationalCoordinator,
    loadingStatus,
    pagination,
    pinnedSeparatorIndex,
    totalRecords
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

  const filterBy = useRecoilValue(filterByState(tabId));
  const sortByOptions = useRecoilValue(sortByState(tabId));

  const { resetFiltersState: resetUserListFiltersState } = useFilters('userList');
  const { resetFiltersState: resetReportingObligationsFiltersState } = useFilters('reportingObligations');

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

    const adminManageWebformsBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'table',
      isVisible: isAdmin,
      label: 'manageWebformsLeftBarButton',
      onClick: () => manageDialogs('isManageWebformsDialogVisible', true),
      title: 'manageWebformsLeftBarButton'
    };

    leftSideBarContext.addModels(
      [
        adminCreateNewPermissionsBtn,
        adminManageWebformsBtn,
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
  }, [tabId, pagination]);

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

  const getDataflows = async (sortBy = sortByOptions) => {
    setLoading(true);

    const { accessRole: accessRoles, contextRoles } = userContext;
    const { numberRows, pageNum } = pagination;

    try {
      if (TextUtils.areEquals(tabId, 'reporting')) {
        const data = await DataflowService.getAll({ accessRoles, contextRoles, filterBy, numberRows, pageNum, sortBy });
        const { dataflows, filteredRecords, totalRecords } = data;

        setStatusDataflowLabel(dataflows);
        setDataflows({ dataflows, filteredRecords, totalRecords, type: tabId });
      } else if (TextUtils.areEquals(tabId, 'reference')) {
        const data = await ReferenceDataflowService.getAll({
          accessRoles,
          contextRoles,
          filterBy,
          numberRows,
          pageNum,
          sortBy
        });
        const { dataflows, filteredRecords, totalRecords } = data;

        setStatusDataflowLabel(dataflows);
        setDataflows({ dataflows, filteredRecords, totalRecords, type: tabId });
      } else if (TextUtils.areEquals(tabId, 'business')) {
        const data = await BusinessDataflowService.getAll({
          accessRoles,
          contextRoles,
          filterBy,
          numberRows,
          pageNum,
          sortBy
        });
        const { dataflows, filteredRecords, totalRecords } = data;

        setStatusDataflowLabel(dataflows);
        setDataflows({ dataflows, filteredRecords, totalRecords, type: tabId });
      } else if (TextUtils.areEquals(tabId, 'citizenScience')) {
        const data = await CitizenScienceDataflowService.getAll({
          accessRoles,
          contextRoles,
          filterBy,
          numberRows,
          pageNum,
          sortBy
        });
        const { dataflows, filteredRecords, totalRecords } = data;

        setStatusDataflowLabel(dataflows);
        setDataflows({ dataflows, filteredRecords, totalRecords, type: tabId });
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

  const renderPaginatorRecordsCount = () => (
    <Fragment>
      {isFiltered ? `${resourcesContext.messages['filtered']}: ${filteredRecords} | ` : ''}
      {`${resourcesContext.messages['totalRecords']} ${totalRecords} ${' '} ${resourcesContext.messages[
        'records'
      ].toLowerCase()}`}
    </Fragment>
  );

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
    onChangePagination({ firstRow: 0, numberRows: 100, pageNum: 0 });
    setGoToPage(1);
  };

  const onHideObligationDialog = () => {
    manageDialogs('isReportingObligationsDialogVisible', false);
    setObligationToPrevious();
    resetReportingObligationsFiltersState();
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

    const copyData = [...reporting, ...reference, ...business, ...citizenScience];

    const userProperties = updateUserPropertiesPinnedDataflows({ pinnedItem, data: copyData });

    await onUpdateUserProperties(userProperties);
    userContext.onChangePinnedDataflows(userProperties.pinnedDataflows);

    const changedInitialData = onUpdatePinnedStatus({ dataflows: dataflowsState[tabId], isPinned, pinnedItem });

    const orderedFilteredData = DataflowsUtils.sortDataflows(changedInitialData);
    const orderedPinned = orderedFilteredData.map(el => el.pinned);

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf('pinned'));

    dataflowsDispatch({
      type: 'SET_DATAFLOWS',
      payload: {
        contextCurrentDataflowType: userContext.currentDataflowType,
        data: DataflowsUtils.sortDataflows(changedInitialData),
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
      const copyDataflow = { ...dataflow };

      if (copyDataflow.id === pinnedItem.id) copyDataflow.pinned = isPinned ? 'pinned' : 'unpinned';

      return copyDataflow;
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

  const setDataflows = ({ dataflows = [], filteredRecords, totalRecords, type }) => {
    const parsedDataflows = DataflowsUtils.parseDataflows(dataflows, userContext.userProps.pinnedDataflows);
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
    dataflowsDispatch({
      type: 'SET_DATAFLOWS',
      payload: {
        contextCurrentDataflowType: userContext.currentDataflowType,
        data: parsedDataflows,
        filteredRecords,
        totalRecords,
        type
      }
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
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => {
        manageDialogs('isUserListVisible', false);
        resetUserListFiltersState();
      }}
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
          resetReportingObligationsFiltersState();
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setObligationToPrevious();
          resetReportingObligationsFiltersState();
        }}
      />
    </Fragment>
  );

  const getFilterOptions = () => {
    const filters = [
      {
        nestedOptions: [
          { key: 'name', label: resourcesContext.messages['name'], isSortable: true },
          { key: 'description', label: resourcesContext.messages['description'], isSortable: true },
          { key: 'legalInstrument', label: resourcesContext.messages['legalInstrument'], isSortable: true },
          { key: 'obligationTitle', label: resourcesContext.messages['obligation'], isSortable: true },
          { key: 'obligationId', label: resourcesContext.messages['obligationId'], isSortable: true }
        ],
        type: 'INPUT'
      },
      {
        nestedOptions: [
          { key: 'status', label: resourcesContext.messages['status'], isSortable: true, template: 'LevelError' },
          { key: 'userRole', label: resourcesContext.messages['userRole'], isSortable: true }
        ],
        type: 'MULTI_SELECT'
      },
      {
        isSortable: true,
        key: 'expirationDate',
        label: resourcesContext.messages['expirationDateFilterLabel'],
        type: 'DATE'
      }
    ];

    if (isCustodian || isAdmin) {
      filters.push({
        isSortable: true,
        key: 'creationDate',
        label: resourcesContext.messages['creationDateFilterLabel'],
        type: 'DATE'
      });
    }

    return filters;
  };

  const dataflowsFilterOptions = getFilterOptions();

  const referenceDataflowFilterOptions = [
    {
      nestedOptions: [
        { key: 'name', label: resourcesContext.messages['name'], isSortable: true },
        { key: 'description', label: resourcesContext.messages['description'], isSortable: true }
      ],
      type: 'INPUT'
    },
    {
      nestedOptions: [
        { key: 'status', label: resourcesContext.messages['status'], isSortable: true, template: 'LevelError' },
        { key: 'pinned', label: resourcesContext.messages['pinned'], isSortable: true }
      ],
      type: 'MULTI_SELECT'
    }
  ];

  const options = {
    business: dataflowsFilterOptions,
    citizenScience: dataflowsFilterOptions,
    reference: referenceDataflowFilterOptions,
    reporting: dataflowsFilterOptions
  };

  const onChangeCurrentPage = event => {
    if (event.key === 'Enter' && goToPage !== '' && goToPage !== pagination.first + 1) {
      const pc = Math.ceil(filteredRecords / pagination.numberRows) || 1;
      const p = Math.floor(event.target.value - 1);

      if (p >= 0 && p < pc) {
        const newPageState = {
          firstRow: (event.target.value - 1) * pagination.numberRows,
          numberRows: pagination.numberRows,
          pageNum: p
        };
        onChangePagination(newPageState);
      }
    } else {
      setGoToPage(event.target.value);
      if (event.target.value <= 0 || event.target.value > Math.ceil(filteredRecords / pagination.numberRows)) {
        setPageInputTooltip(
          `${resourcesContext.messages['currentPageErrorMessage']} ${Math.ceil(
            filteredRecords / pagination.numberRows
          )}`
        );
      } else {
        setPageInputTooltip(resourcesContext.messages['currentPageInfoMessage']);
      }
    }
  };

  const onChangePagination = pagination => dataflowsDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onPaginate = event => {
    setGoToPage(event.page + 1);
    onChangePagination({ firstRow: event.first, numberRows: event.rows, pageNum: event.page });
  };

  const setGoToPage = value => dataflowsDispatch({ type: 'SET_GO_TO_PAGE', payload: value });

  const setPageInputTooltip = value => dataflowsDispatch({ type: 'SET_PAGE_INPUT_TOOLTIP', payload: value });

  const currentPageTemplate = {
    layout: 'FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport',
    CurrentPageReport: options => {
      return (
        <span className={styles.currentPageWrapper}>
          <label className={styles.currentPageLabel}>{resourcesContext.messages['goTo']}</label>
          <InputText
            className={styles.currentPageInput}
            data-for="pageInputTooltip"
            data-tip
            id="currentPageInput"
            keyfilter="pint"
            onChange={onChangeCurrentPage}
            onKeyDown={onChangeCurrentPage}
            style={{
              border: (goToPage <= 0 || goToPage > options.totalPages) && '1px solid var(--errors)',
              boxShadow:
                goToPage <= 0 || goToPage > options.totalPages ? 'var(--inputtext-box-shadow-focus-error)' : 'none'
            }}
            value={goToPage}
          />
          <ReactTooltip border={true} effect="solid" id="pageInputTooltip" place="bottom">
            {dataflowsState.pageInputTooltip}
          </ReactTooltip>
          <label className={styles.currentPageOf}>
            {filteredRecords > 0
              ? `${resourcesContext.messages['of']} ${Math.ceil(filteredRecords / pagination.numberRows)}`
              : 1}
          </label>
        </span>
      );
    }
  };

  const renderPaginator = () => {
    if (!loadingStatus[tabId] && filteredRecords > 100) {
      return (
        <Paginator
          className={`p-paginator-bottom ${styles.paginator}`}
          first={pagination.firstRow}
          isDataflowsList={true}
          onPageChange={onPaginate}
          rightContent={renderPaginatorRecordsCount()}
          rows={pagination.numberRows}
          rowsPerPageOptions={[100, 150, 200]}
          template={currentPageTemplate}
          totalRecords={filteredRecords}
        />
      );
    }
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
          className="dataflowsFilters"
          data={dataflowsState[tabId]}
          onFilter={getDataflows}
          onSort={getDataflows}
          options={options[tabId]}
          viewType={tabId}
        />
        {renderPaginator()}
        <DataflowsList
          className="dataflowList-accepted-help-step"
          data={dataflowsState[tabId]}
          isAdmin={isAdmin}
          isCustodian={isCustodian}
          isFiltered={isFiltered}
          isLoading={loadingStatus[tabId]}
          pinnedSeparatorIndex={pinnedSeparatorIndex}
          reorderDataflows={onReorderPinnedDataflows}
          visibleTab={tabId}
        />
        <div className={styles.bottomPaginator}>{renderPaginator()}</div>
      </div>

      <GoTopButton parentRef={containerRef} referenceMargin={70} />

      {dataflowsState.isUserListVisible && (
        <Dialog
          footer={renderUserListDialogFooter()}
          header={resourcesContext.messages['allDataflowsUserListHeader']}
          onHide={() => {
            manageDialogs('isUserListVisible', false);
            resetUserListFiltersState();
          }}
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

      {dataflowsState.isManageWebformsDialogVisible && (
        <ManageWebforms
          isDialogVisible={dataflowsState.isManageWebformsDialogVisible}
          onCloseDialog={() => manageDialogs('isManageWebformsDialogVisible', false)}
        />
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
