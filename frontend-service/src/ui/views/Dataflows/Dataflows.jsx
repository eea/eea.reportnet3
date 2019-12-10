import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './Dataflows.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DataflowCrudForm } from 'ui/views/_components/DataflowCrudForm';
import { DataflowsList } from './DataflowsList';
import { Dialog } from 'ui/views/_components/Dialog';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataflowService } from 'core/services/Dataflow';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

export const Dataflows = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [acceptedContent, setacceptedContent] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  const [createDataflowDialogVisible, setCreateDataflowDialogVisible] = useState(false);
  const [isCustodian, setIsCustodian] = useState();
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

  const dataFetch = async () => {
    setLoading(true);
    try {
      const allDataflows = await DataflowService.all();
      setpendingContent(allDataflows.pending);
      setacceptedContent(allDataflows.accepted);
      setcompletedContent(allDataflows.completed);
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
    setCreateDataflowDialogVisible(false);
    dataFetch();
    onRefreshToken();
  };

  const onHideDialog = () => {
    setCreateDataflowDialogVisible(false);
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
    setCreateDataflowDialogVisible(true);
  };

  const onShowEditForm = () => {
    setIsEditForm(true);
    setCreateDataflowDialogVisible(true);
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
    <div className="rep-row">
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
              dataFetch={dataFetch}
              isCustodian={isCustodian}
              listContent={pendingContent}
              listDescription={resources.messages.pendingDataflowText}
              listTitle={resources.messages.pendingDataflowTitle}
              listType="pending"
            />
            <DataflowsList
              dataFetch={dataFetch}
              isCustodian={isCustodian}
              listContent={acceptedContent}
              listDescription={resources.messages.acceptedDataflowText}
              listTitle={resources.messages.acceptedDataflowTitle}
              listType="accepted"
              showEditForm={onShowEditForm}
            />
          </>
        ) : (
          <>
            <DataflowsList
              dataFetch={dataFetch}
              isCustodian={isCustodian}
              listContent={completedContent}
              listDescription={resources.messages.completedDataflowText}
              listTitle={resources.messages.completedDataflowTitle}
              listType="completed"
            />
          </>
        )}
      </div>

      <Dialog
        className={styles.dialog}
        dismissableMask={false}
        header={resources.messages['createNewDataflow']}
        onHide={onHideDialog}
        visible={createDataflowDialogVisible}>
        <DataflowCrudForm
          isEditForm={isEditForm}
          isFormReset={isFormReset}
          onCreate={onCreateDataflow}
          onCancel={onHideDialog}
        />
      </Dialog>
    </div>
  );
});
