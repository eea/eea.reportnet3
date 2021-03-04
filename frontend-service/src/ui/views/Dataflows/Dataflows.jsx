import React, { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Dataflows.module.scss';

import { config } from 'conf';
import { DataflowsRequesterHelpConfig } from 'conf/help/dataflows/requester';
import { DataflowsReporterHelpConfig } from 'conf/help/dataflows/reporter';

import { Button } from 'ui/views/_components/Button';
import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { DataflowsList } from './_components/DataflowsList';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';
import { UserList } from 'ui/views/_components/UserList';

import { DataflowService } from 'core/services/Dataflow';
import { UserService } from 'core/services/User';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowsReducer } from './_functions/Reducers/dataflowsReducer';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { ErrorUtils } from 'ui/views/_functions/Utils';

const Dataflows = withRouter(({ match, history }) => {
  const {
    params: { errorType: dataflowsErrorType }
  } = match;
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [tabMenuItems] = useState([
    {
      // label: resources.messages['dataflowAcceptedPendingTab'],
      label: resources.messages['dataflowsListTab'],
      className: styles.flow_tab,
      tabKey: 'pending'
    },
    {
      label: resources.messages['dataflowCompletedTab'],
      className: styles.flow_tab,
      disabled: true,
      tabKey: 'completed'
    }
  ]);
  const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);

  const [dataflowsState, dataflowsDispatch] = useReducer(dataflowsReducer, {
    accepted: [],
    allDataflows: {},
    completed: [],
    isAddDialogVisible: false,
    isCustodian: null,
    isLoading: true,
    isNationalCoordinator: false,
    isRepObDialogVisible: false,
    isUserListVisible: false,
    pending: []
  });

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOWS, history });

  useEffect(() => {
    if (!isNil(dataflowsErrorType)) {
      notificationContext.add({ type: ErrorUtils.parseErrorType(dataflowsErrorType) });
    }
  }, []);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      dataFetch();
      onLoadPermissions();
    }
  }, [userContext.contextRoles]);

  useEffect(() => {
    leftSideBarContext.removeModels();

    const createBtn = {
      className: 'dataflowList-left-side-bar-create-dataflow-help-step',
      icon: 'plus',
      isVisible: dataflowsState.isCustodian,
      label: 'createNewDataflow',
      onClick: () => manageDialogs('isAddDialogVisible', true),
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

    const allButtons = [createBtn, userListBtn];

    leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
  }, [dataflowsState.isCustodian, dataflowsState.isNationalCoordinator]);

  useEffect(() => {
    const messageStep0 = dataflowsState.isCustodian ? 'dataflowListRequesterHelp' : 'dataflowListReporterHelp';
    leftSideBarContext.addHelpSteps(
      dataflowsState.isCustodian ? DataflowsRequesterHelpConfig : DataflowsReporterHelpConfig,
      messageStep0
    );
  }, [dataflowsState]);

  const dataFetch = async () => {
    isLoading(true);
    try {
      const allDataflows = await DataflowService.all(userContext.contextRoles);
      dataflowsDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          accepted: allDataflows.accepted,
          allDataflows,
          completed: allDataflows.completed,
          pending: allDataflows.pending
        }
      });
    } catch (error) {
      console.error('dataFetch error: ', error);
    }
    isLoading(false);
  };

  const isLoading = value => dataflowsDispatch({ type: 'IS_LOADING', payload: { value } });

  const onCreateDataflow = () => {
    manageDialogs('isAddDialogVisible', false);
    onRefreshToken();
  };

  const onLoadPermissions = () => {
    const isCustodian =
      userContext.hasPermission([config.permissions.DATA_CUSTODIAN]) ||
      userContext.hasPermission([config.permissions.DATA_STEWARD]);

    const isNationalCoordinator = userContext.hasContextAccessPermission(
      config.permissions.NATIONAL_COORDINATOR_PREFIX,
      null,
      [config.permissions.NATIONAL_COORDINATOR]
    );

    dataflowsDispatch({ type: 'HAS_PERMISSION', payload: { isCustodian, isNationalCoordinator } });
  };

  const manageDialogs = (dialog, value, secondDialog, secondValue, data = {}) =>
    dataflowsDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value, secondDialog, secondValue, data } });

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      userContext.onTokenRefresh(userObject);
    } catch (error) {
      await UserService.logout();
      userContext.onLogout();
    }
  };

  const renderDataflowUsersListFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isUserListVisible', false)}
    />
  );

  const layout = children => (
    <MainLayout>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowsState.isLoading) return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-12 dataflowList-help-step`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <Fragment>
            {/* <DataflowsList
              className="dataflowList-pending-help-step"
              content={dataflowsState.pending}
              dataFetch={dataFetch}
              description={resources.messages['pendingDataflowText']}
              title={resources.messages['pendingDataflowTitle']}
              type="pending"
            /> */}
            <DataflowsList
              className="dataflowList-accepted-help-step"
              content={dataflowsState.accepted}
              dataFetch={dataFetch}
              isCustodian={dataflowsState.isCustodian}
              type="accepted"
            />
          </Fragment>
        ) : (
          <Fragment>
            <DataflowsList
              content={dataflowsState.completed}
              dataFetch={dataFetch}
              description={resources.messages.completedDataflowText}
              isCustodian={dataflowsState.isCustodian}
              title={resources.messages.completedDataflowTitle}
              type="completed"
            />
          </Fragment>
        )}
      </div>

      {dataflowsState.isUserListVisible && (
        <Dialog
          footer={renderDataflowUsersListFooter}
          header={resources.messages['allDataflowsUserListHeader']}
          onHide={() => manageDialogs('isUserListVisible', false)}
          visible={dataflowsState.isUserListVisible}>
          <UserList />
        </Dialog>
      )}

      <DataflowManagement
        isEditForm={false}
        onCreateDataflow={onCreateDataflow}
        manageDialogs={manageDialogs}
        state={dataflowsState}
      />
    </div>
  );
});

export { Dataflows };
