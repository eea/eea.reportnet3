import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './DataFlowTasks.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DataflowColumn } from 'ui/views/_components/DataFlowColumn';
import { DataflowList } from './DataFlowList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataflowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';

export const DataflowTasks = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
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
  const [loading, setLoading] = useState(true);
  const [pendingContent, setpendingContent] = useState([]);
  const [acceptedContent, setacceptedContent] = useState([]);
  const [completedContent, setcompletedContent] = useState([]);
  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

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
    setBreadCrumbItems([{ label: resources.messages['dataflowList'] }]);
  }, [history, match.params.dataflowId, resources.messages]);

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row">
      <DataflowColumn
        navTitle={resources.messages['dataflow']}
        components={['search', 'createDataflow']}
        createDataflowButtonTitle={resources.messages['createNewDataflow']}
        subscribeButtonTitle={resources.messages['subscribeButton']}
        style={{ textAlign: 'left' }}
      />
      <div className={`${styles.container} rep-col-xs-12 rep-col-md-10`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <>
            <DataflowList
              listTitle={resources.messages.pendingDataflowTitle}
              listDescription={resources.messages.pendingDataflowText}
              listContent={pendingContent}
              dataFetch={dataFetch}
              listType="pending"
            />
            <DataflowList
              listTitle={resources.messages.acceptedDataflowTitle}
              listDescription={resources.messages.acceptedDataflowText}
              listContent={acceptedContent}
              dataFetch={dataFetch}
              listType="accepted"
            />
          </>
        ) : (
          <>
            <DataflowList
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
