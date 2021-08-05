import { Fragment, useContext, useEffect, useLayoutEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Dataflows.module.scss';

import { config } from 'conf';
import { DataflowsReporterHelpConfig } from 'conf/help/dataflows/reporter';
import { DataflowsRequesterHelpConfig } from 'conf/help/dataflows/requester';

import { Button } from 'views/_components/Button';
import { DataflowManagement } from 'views/_components/DataflowManagement';
import { DataflowsList } from './_components/DataflowsList';
import { Dialog } from 'views/_components/Dialog';
import { MainLayout } from 'views/_components/Layout';
import { TabMenu } from './_components/TabMenu';
import { UserList } from 'views/_components/UserList';

import { DataflowService } from 'services/DataflowService';
import { BusinessDataflowService } from 'services/BusinessDataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';
import { UserService } from 'services/UserService';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useReportingObligations } from 'views/_components/ReportingObligations/_functions/Hooks/useReportingObligations';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { dataflowsReducer } from './_functions/Reducers/dataflowsReducer';

import { CurrentPage } from 'views/_functions/Utils';
import { DataflowsUtils } from './_functions/Utils/DataflowsUtils';
import { ErrorUtils } from 'views/_functions/Utils';
import { ManageReferenceDataflow } from 'views/_components/ManageReferenceDataflow';
import { ManageBusinessDataflow } from 'views/_components/ManageBusinessDataflow';
import { ReportingObligations } from 'views/_components/ReportingObligations';

import { TextUtils } from 'repositories/_utils/TextUtils';

const Dataflows = withRouter(({ history, match }) => {
  const {
    params: { errorType: dataflowsErrorType }
  } = match;

  const { permissions } = config;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataflowsState, dataflowsDispatch] = useReducer(dataflowsReducer, {
    activeIndex: 0,
    business: [],
    dataflows: [],
    isAddDialogVisible: false,
    isAdmin: null,
    isBusinessDataflowDialogVisible: false,
    isCustodian: null,
    isNationalCoordinator: false,
    isReferencedDataflowDialogVisible: false,
    isRepObDialogVisible: false,
    isReportingObligationsDialogVisible: false,
    isUserListVisible: false,
    loadingStatus: { dataflows: true, reference: true },
    reference: []
  });

  const { obligation, resetObligations, setObligationToPrevious, setCheckedObligation, setToCheckedObligation } =
    useReportingObligations();

  const { activeIndex, loadingStatus } = dataflowsState;

  const tabMenuItems = dataflowsState.isCustodian
    ? [
        { className: styles.flow_tab, id: 'reporting', label: resources.messages['reportingDataflowsListTab'] },
        { className: styles.flow_tab, id: 'business', label: resources.messages['businessDataflowsListTab'] },
        { className: styles.flow_tab, id: 'reference', label: resources.messages['referenceDataflowsListTab'] }
      ]
    : [
        { className: styles.flow_tab, id: 'reporting', label: resources.messages['reportingDataflowsListTab'] },
        { className: styles.flow_tab, id: 'business', label: resources.messages['businessDataflowsListTab'] }
      ];

  const { tabId } = DataflowsUtils.getActiveTab(tabMenuItems, activeIndex);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOWS, history });

  useEffect(() => {
    if (!isNil(dataflowsErrorType)) {
      notificationContext.add({ type: ErrorUtils.parseErrorType(dataflowsErrorType) });
    }
  }, []);

  useEffect(() => {
    leftSideBarContext.removeModels();

    const createReportingDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'reporting' && dataflowsState.isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isAddDialogVisible', true),
      title: 'createNewDataflow'
    };

    const createReferenceDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'reference' && dataflowsState.isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isReferencedDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const createBusinessDataflowBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: tabId === 'business' && dataflowsState.isAdmin,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isBusinessDataflowDialogVisible', true),
      title: 'createNewDataflow'
    };

    const userListBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'users',
      isVisible: dataflowsState.isNationalCoordinator,
      label: 'allDataflowsUserList',
      onClick: () => manageDialogs('isUserListVisible', true),
      title: 'allDataflowsUserList'
    };

    leftSideBarContext.addModels(
      [createBusinessDataflowBtn, createReferenceDataflowBtn, createReportingDataflowBtn, userListBtn].filter(
        button => button.isVisible
      )
    );
  }, [dataflowsState.isAdmin, dataflowsState.isCustodian, dataflowsState.isNationalCoordinator, tabId]);

  useEffect(() => {
    const messageStep0 = dataflowsState.isCustodian ? 'dataflowListRequesterHelp' : 'dataflowListReporterHelp';
    leftSideBarContext.addHelpSteps(
      dataflowsState.isCustodian ? DataflowsRequesterHelpConfig : DataflowsReporterHelpConfig,
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
    if (isEmpty(dataflowsState[tabId]) && !isNil(userContext.contextRoles)) {
      getDataflows();
    }
  }, [tabId]);

  const getDataflows = async () => {
    setLoading(true);

    try {
      if (TextUtils.areEquals(tabId, 'reporting')) {
        const { data } = await DataflowService.all(userContext.contextRoles);
        dataflowsDispatch({ type: 'SET_DATAFLOWS', payload: { data, type: 'reporting' } });
      }

      if (TextUtils.areEquals(tabId, 'reference')) {
        const { data } = await ReferenceDataflowService.all(userContext.contextRoles);
        dataflowsDispatch({ type: 'SET_DATAFLOWS', payload: { data, type: 'reference' } });
      }

      if (TextUtils.areEquals(tabId, 'business')) {
        const { data } = await BusinessDataflowService.getAll(userContext.contextRoles);
        dataflowsDispatch({ type: 'SET_DATAFLOWS', payload: { data, type: 'business' } });
      }
    } catch (error) {
      console.error('Dataflows - getDataflows.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOWS_ERROR' });
    } finally {
      setLoading(false);
    }
  };

  const manageDialogs = (dialog, value) => {
    dataflowsDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value } });
  };

  const onCreateDataflow = dialog => {
    manageDialogs(dialog, false);
    onRefreshToken();
  };

  const onHideObligationDialog = () => {
    manageDialogs('isReportingObligationsDialogVisible', false);
    setObligationToPrevious();
  };

  const onChangeTab = index => dataflowsDispatch({ type: 'ON_CHANGE_TAB', payload: { index } });

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

  const setLoading = status => dataflowsDispatch({ type: 'SET_LOADING', payload: { tab: tabId, status } });

  const renderLayout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  const renderUserListDialogFooter = () => (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isUserListVisible', false)}
    />
  );

  const renderObligationFooter = () => (
    <Fragment>
      <Button
        icon="check"
        label={resources.messages['ok']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setToCheckedObligation();
        }}
      />
      <Button
        className="p-button-secondary button-right-aligned p-button-animated-blink"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          manageDialogs('isReportingObligationsDialogVisible', false);
          setObligationToPrevious();
        }}
      />
    </Fragment>
  );

  return renderLayout(
    <div className="rep-row">
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-12 dataflowList-help-step`}>
        <TabMenu activeIndex={activeIndex} model={tabMenuItems} onTabChange={event => onChangeTab(event.index)} />
        <DataflowsList
          className="dataflowList-accepted-help-step"
          content={{
            reporting: dataflowsState['reporting'],
            business: dataflowsState['business'],
            reference: dataflowsState['reference']
          }}
          isLoading={loadingStatus[tabId]}
          visibleTab={tabId}
        />
      </div>

      {dataflowsState.isUserListVisible && (
        <Dialog
          footer={renderUserListDialogFooter()}
          header={resources.messages['allDataflowsUserListHeader']}
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
          isVisible={dataflowsState.isBusinessDataflowDialogVisible}
          manageDialogs={manageDialogs}
          obligation={obligation}
          onCreateDataflow={onCreateDataflow}
          resetObligations={resetObligations}
        />
      )}

      <DataflowManagement
        isEditForm={false}
        manageDialogs={manageDialogs}
        obligation={obligation}
        onCreateDataflow={onCreateDataflow}
        resetObligations={resetObligations}
        state={dataflowsState}
      />

      {dataflowsState.isReportingObligationsDialogVisible && (
        <Dialog
          footer={renderObligationFooter()}
          header={resources.messages['reportingObligations']}
          onHide={onHideObligationDialog}
          style={{ width: '95%' }}
          visible={dataflowsState.isReportingObligationsDialogVisible}>
          <ReportingObligations obligationChecked={obligation} setCheckedObligation={setCheckedObligation} />
        </Dialog>
      )}
    </div>
  );
});

export { Dataflows };
