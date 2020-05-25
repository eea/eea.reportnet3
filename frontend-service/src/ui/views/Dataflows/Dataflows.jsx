import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isNil from 'lodash/isNil';

import styles from './Dataflows.module.scss';

import { config } from 'conf';
import { DataflowsHelpConfig } from 'conf/domain/model/Dataflow';

import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { DataflowsList } from './_components/DataflowsList';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataflowService } from 'core/services/Dataflow';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowsReducer } from './_functions/Reducers/dataflowsReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const Dataflows = withRouter(({ match, history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

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
    isRepObDialogVisible: false,
    isLoading: true,
    pending: []
  });

  useEffect(() => {
    if (!isNil(user.contextRoles)) {
      dataFetch();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resources.messages, tabMenuActiveItem, user.contextRoles]);

  //Bread Crumbs settings
  useEffect(() => {
    breadCrumbContext.add([{ label: resources.messages['dataflows'], icon: 'home' }]);
  }, []);

  useEffect(() => {
    if (!isNil(user.contextRoles)) onLoadPermissions();
  }, [user]);

  useEffect(() => {
    if (dataflowsState.isCustodian) {
      leftSideBarContext.addModels([
        {
          className: 'dataflowList-create-dataflow-help-step',
          icon: 'plus',
          label: 'createNewDataflow',
          onClick: () => manageDialogs('isAddDialogVisible', true),
          title: 'createNewDataflow'
        }
      ]);
    } else {
      leftSideBarContext.removeModels();
    }
  }, [dataflowsState.isCustodian]);

  useEffect(() => {
    leftSideBarContext.addHelpSteps(DataflowsHelpConfig, 'dataflowListHelp');
  }, [dataflowsState]);

  const dataFetch = async () => {
    isLoading(true);
    try {
      const allDataflows = await DataflowService.all(user.contextRoles);
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
    dataFetch();
    manageDialogs('isAddDialogVisible', false);
    onRefreshToken();
  };

  const onLoadPermissions = () => {
    const isCustodian = UserService.hasPermission(user, [config.permissions.CUSTODIAN]);
    dataflowsDispatch({ type: 'HAS_PERMISSION', payload: { isCustodian } });
  };

  const manageDialogs = (dialog, value, secondDialog, secondValue, data = {}) =>
    dataflowsDispatch({ type: 'MANAGE_DIALOGS', payload: { dialog, value, secondDialog, secondValue, data } });

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      user.onTokenRefresh(userObject);
    } catch (error) {
      await UserService.logout();
      user.onLogout();
    }
  };

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
          <>
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
              // description={resources.messages['acceptedDataflowText']}
              // title={resources.messages['acceptedDataflowTitle']}
              type="accepted"
            />
          </>
        ) : (
          <>
            <DataflowsList
              content={dataflowsState.completed}
              dataFetch={dataFetch}
              description={resources.messages.completedDataflowText}
              isCustodian={dataflowsState.isCustodian}
              title={resources.messages.completedDataflowTitle}
              type="completed"
            />
          </>
        )}
      </div>

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
