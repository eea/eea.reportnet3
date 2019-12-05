import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './Dataflows.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { DataflowsList } from './DataflowsList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataflowService } from 'core/services/Dataflow';
import { UserContext } from '../_components/_context/UserContext';
import { UserService } from 'core/services/User';

export const Dataflows = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [acceptedContent, setacceptedContent] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  const [isCustodian, setIsCustodian] = useState();
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
        navTitle={resources.messages['dataflowList']}
        components={['search', 'createDataflow']}
        createDataflowButtonTitle={resources.messages['createNewDataflow']}
        isCustodian={isCustodian}
        onFetchData={dataFetch}
        subscribeButtonTitle={resources.messages['subscribeButton']}
        style={{ textAlign: 'left' }}
      />
      <div className={`${styles.container} rep-col-xs-12 rep-col-xl-10`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <>
            <DataflowsList
              listTitle={resources.messages.pendingDataflowTitle}
              listDescription={resources.messages.pendingDataflowText}
              listContent={pendingContent}
              dataFetch={dataFetch}
              listType="pending"
            />
            <DataflowsList
              listTitle={resources.messages.acceptedDataflowTitle}
              listDescription={resources.messages.acceptedDataflowText}
              listContent={acceptedContent}
              dataFetch={dataFetch}
              listType="accepted"
            />
          </>
        ) : (
          <>
            <DataflowsList
              listTitle={resources.messages.completedDataflowTitle}
              listDescription={resources.messages.completedDataflowText}
              listContent={completedContent}
              dataFetch={dataFetch}
              listType="completed"
            />
          </>
        )}
      </div>
    </div>
  );
});
