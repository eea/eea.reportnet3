import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './Dataflows.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DataflowManagementForm } from 'ui/views/_components/DataflowManagementForm';
import { DataflowsList } from './DataflowsList';
import { Dialog } from 'ui/views/_components/Dialog';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';

import { DataflowService } from 'core/services/Dataflow';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

const Dataflows = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [acceptedContent, setacceptedContent] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  const [isCustodian, setIsCustodian] = useState();
  const [isDataflowDialogVisible, setIsDataflowDialogVisible] = useState(false);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [loading, setLoading] = useState(true);
  const [pendingContent, setpendingContent] = useState([]);
  const [tabMenuItems] = useState([
    {
      label: resources.messages['dataflowAcceptedPendingTab'],
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

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});

  const dataFetch = async () => {
    setLoading(true);
    try {
      const allDataflows = await DataflowService.all();
      setpendingContent(allDataflows.pending);
      setacceptedContent(allDataflows.accepted);
      setcompletedContent(allDataflows.completed);
      const dataflowInitialValues = {};
      allDataflows.accepted.forEach(element => {
        dataflowInitialValues[element.id] = { name: element.name, description: element.description, id: element.id };
      });
      dataflowDispatch({
        type: 'ON_INIT_DATA',
        payload: dataflowInitialValues
      });
    } catch (error) {
      console.error('dataFetch error: ', error);
    }
    setLoading(false);
  };

  useEffect(() => {
    dataFetch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resources.messages, tabMenuActiveItem]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([{ label: resources.messages['dataflowList'], icon: 'home' }]);
  }, [history, match.params.dataflowId, resources.messages]);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(UserService.hasPermission(user, [config.permissions.CUSTODIAN]));
    }
  }, [user]);

  const onCreateDataflow = () => {
    setIsDataflowDialogVisible(false);
    dataFetch();
    onRefreshToken();
  };

  const onHideDialog = () => {
    setIsDataflowDialogVisible(false);
    setIsFormReset(false);
  };

  const onRefreshToken = async () => {
    try {
      const userObject = await UserService.refreshToken();
      user.onTokenRefresh(userObject);
    } catch (error) {
      await UserService.logout();
      user.onLogout();
    }
  };

  const onShowAddForm = () => {
    setIsEditForm(false);
    setIsDataflowDialogVisible(true);
    dataflowDispatch({
      type: 'ON_RESET_DATAFLOW_DATA'
    });
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row" id="dataflows">
      <LeftSideBar
        createDataflowButtonTitle={resources.messages['createNewDataflow']}
        components={['search', 'createDataflow']}
        isCustodian={isCustodian}
        navTitle={resources.messages['dataflowList']}
        onShowAddForm={onShowAddForm}
        subscribeButtonTitle={resources.messages['subscribeButton']}
        style={{ textAlign: 'left' }}
      />
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-10`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <>
            <DataflowsList
              content={pendingContent}
              dataFetch={dataFetch}
              description={resources.messages.pendingDataflowText}
              isCustodian={isCustodian}
              title={resources.messages.pendingDataflowTitle}
              type="pending"
            />
            <DataflowsList
              content={acceptedContent}
              dataFetch={dataFetch}
              dataflowNewValues={dataflowState.selectedDataflow}
              description={resources.messages.acceptedDataflowText}
              selectedDataflowId={dataflowState.selectedDataflowId}
              title={resources.messages.acceptedDataflowTitle}
              type="accepted"
            />
          </>
        ) : (
          <>
            <DataflowsList
              content={completedContent}
              dataFetch={dataFetch}
              description={resources.messages.completedDataflowText}
              isCustodian={isCustodian}
              title={resources.messages.completedDataflowTitle}
              type="completed"
            />
          </>
        )}
      </div>

      <Dialog
        className={styles.dialog}
        dismissableMask={false}
        header={resources.messages['createNewDataflow']}
        onHide={onHideDialog}
        visible={isDataflowDialogVisible}>
        <DataflowManagementForm
          isDialogVisible={isDataflowDialogVisible}
          isFormReset={isFormReset}
          onCreate={onCreateDataflow}
          onCancel={onHideDialog}
        />
      </Dialog>
    </div>
  );
});

export { Dataflows };
