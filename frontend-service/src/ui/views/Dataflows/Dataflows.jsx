import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './Dataflows.module.scss';

import { config } from 'conf';

import { DataflowManagementForm } from 'ui/views/_components/DataflowManagementForm';
import { DataflowsList } from './DataflowsList';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataflowService } from 'core/services/Dataflow';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const Dataflows = withRouter(({ match, history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [acceptedContent, setacceptedContent] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  const [dataflowHasErrors, setDataflowHasErrors] = useState(false);
  const [isCustodian, setIsCustodian] = useState();
  const [isDataflowDialogVisible, setIsDataflowDialogVisible] = useState(false);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [isNameDuplicated, setIsNameDuplicated] = useState(false);
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
    breadCrumbContext.add([{ label: resources.messages['dataflowList'], icon: 'home' }]);
  }, []);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(UserService.hasPermission(user, [config.permissions.CUSTODIAN]));
    }
  }, [user]);

  useEffect(() => {
    const steps = [
      {
        content: <h2>{resources.messages['dataflowListHelp']}</h2>,
        locale: { skip: <strong aria-label="skip">{resources.messages['skipHelp']}</strong> },
        placement: 'center',
        target: 'body'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep1']}</h2>,
        target: '.dataflowList-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep2']}</h2>,
        target: '.dataflowList-pending-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep3']}</h2>,
        target: '.dataflowList-accepted-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep4']}</h2>,
        target: '.dataflowList-delivery-date-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep5']}</h2>,
        target: '.dataflowList-name-description-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowListHelpStep6']}</h2>,
        target: '.dataflowList-status-help-step'
      }
    ];

    if (isCustodian) {
      leftSideBarContext.addModels([
        {
          className: 'dataflowList-create-dataflow-help-step',
          icon: 'plus',
          label: 'createNewDataflow',
          onClick: () => onShowAddForm(),
          title: 'createNewDataflow'
        },
        {
          className: 'dataflowList-manage-codelists-help-step',
          href: getUrl(routes['CODELISTS']),
          icon: 'clipboard',
          label: 'manageCodelists',
          onClick: e => {
            e.preventDefault();
            history.push(getUrl(routes['CODELISTS']));
          },
          title: 'manageCodelists'
        }
      ]);
      steps.push(
        {
          content: <h2>{resources.messages['dataflowListHelpStep7']}</h2>,
          target: '.dataflowList-create-dataflow-help-step'
        },
        {
          content: <h2>{resources.messages['dataflowListHelpStep8']}</h2>,
          target: '.dataflowList-manage-codelists-help-step'
        }
      );
    } else {
      leftSideBarContext.removeModels();
    }
    leftSideBarContext.addHelpSteps('dataflowListHelp', steps);
  }, [isCustodian]);

  const onCreateDataflow = () => {
    setIsDataflowDialogVisible(false);
    dataFetch();
    onRefreshToken();
  };

  const onHideDialog = () => {
    setIsDataflowDialogVisible(false);
    setIsFormReset(false);
    setDataflowHasErrors(false);
    setIsNameDuplicated(false);
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
    setIsFormReset(true);
    dataflowDispatch({
      type: 'ON_RESET_DATAFLOW_DATA'
    });
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row">
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-12 dataflowList-help-step`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <>
            <DataflowsList
              className="dataflowList-pending-help-step"
              content={pendingContent}
              dataFetch={dataFetch}
              description={resources.messages.pendingDataflowText}
              isCustodian={isCustodian}
              title={resources.messages.pendingDataflowTitle}
              type="pending"
              user={user}
            />
            <DataflowsList
              className="dataflowList-accepted-help-step"
              content={acceptedContent}
              dataFetch={dataFetch}
              dataflowNewValues={dataflowState.selectedDataflow}
              description={resources.messages.acceptedDataflowText}
              selectedDataflowId={dataflowState.selectedDataflowId}
              title={resources.messages.acceptedDataflowTitle}
              type="accepted"
              user={user}
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
              user={user}
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
          hasErrors={dataflowHasErrors}
          isDialogVisible={isDataflowDialogVisible}
          isFormReset={isFormReset}
          isNameDuplicated={isNameDuplicated}
          onCancel={onHideDialog}
          onCreate={onCreateDataflow}
          setHasErrors={setDataflowHasErrors}
          setIsNameDuplicated={setIsNameDuplicated}
        />
      </Dialog>
    </div>
  );
});

export { Dataflows };
