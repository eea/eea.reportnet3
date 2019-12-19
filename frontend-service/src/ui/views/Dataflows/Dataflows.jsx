import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './Dataflows.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowCrudForm } from 'ui/views/_components/DataflowCrudForm';
import { DataflowsList } from './DataflowsList';
import { Dialog } from 'ui/views/_components/Dialog';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { dataflowReducer } from './_functions/Reducers';

import { DataflowService } from 'core/services/Dataflow';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

export const Dataflows = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [acceptedContent, setacceptedContent] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  // const [dataflowInitialValues, setDataflowInitialValues] = useState({});
  const [isCustodian, setIsCustodian] = useState();
  const [isDataflowDialogVisible, setIsDataflowDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
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

  const onDeleteDataflow = async () => {
    setIsDeleteDialogVisible(false);
    try {
      const response = await DataflowService.deleteById(dataflowState.selectedDataflowId);
      if (response.status >= 200 && response.status <= 299) {
        dataFetch();
      } else {
        console.log('Delete dataflow error with this status: ', response);
      }
    } catch (error) {
      console.log('Error: ', error);
    }
  };

  const onEditDataflow = (id, newName, newDescription) => {
    setIsDataflowDialogVisible(false);
    dataflowDispatch({
      type: 'ON_EDIT_DATAFLOW',
      payload: { id: id, name: newName, description: newDescription }
    });
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

  const onShowDeleteDialog = () => {
    setIsDeleteDialogVisible(true);
  };

  const onShowEditForm = () => {
    setIsEditForm(true);
    setIsDataflowDialogVisible(true);
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
              dataflowDispatch={dataflowDispatch}
              dataflowNewValues={dataflowState.selectedDataflow}
              isCustodian={isCustodian}
              listContent={acceptedContent}
              listDescription={resources.messages.acceptedDataflowText}
              listTitle={resources.messages.acceptedDataflowTitle}
              listType="accepted"
              selectedDataflowId={dataflowState.selectedDataflowId}
              showDeleteDialog={onShowDeleteDialog}
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
        header={isEditForm ? 'edit' : resources.messages['createNewDataflow']}
        onHide={onHideDialog}
        visible={isDataflowDialogVisible}>
        <DataflowCrudForm
          dataflowId={dataflowState.selectedDataflowId}
          dataflowValues={dataflowState}
          isDialogVisible={isDataflowDialogVisible}
          isEditForm={isEditForm}
          isFormReset={isFormReset}
          onCreate={onCreateDataflow}
          onCancel={onHideDialog}
          onEdit={onEditDataflow}
          selectedDataflow={dataflowState.selectedDataflow}
        />
      </Dialog>

      <ConfirmDialog
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteDataflow()}
        onHide={e => setIsDeleteDialogVisible(false)}
        visible={isDeleteDialogVisible}>
        {resources.messages['deleteDataflowConfirm']}
      </ConfirmDialog>
    </div>
  );
});
