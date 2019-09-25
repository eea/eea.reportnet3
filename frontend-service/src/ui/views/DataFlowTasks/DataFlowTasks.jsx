import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './DataFlowTasks.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { DataFlowColumn } from 'ui/views/_components/DataFlowColumn';
import { DataFlowList } from './DataFlowList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabMenu } from 'primereact/tabmenu';

import { DataFlowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataFlowTasks = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [tabMenuItems] = useState([
    {
      label: resources.messages['dataFlowAcceptedPendingTab'],
      className: styles.flow_tab,
      tabKey: 'pending'
    },
    {
      label: resources.messages['dataFlowCompletedTab'],
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
    command: () => history.push(getUrl(config.DATAFLOWS.url))
  };

  const dataFetch = async () => {
    setLoading(true);
    try {
      const allDataFlows = await DataFlowService.all();
      setpendingContent(allDataFlows.pending);
      setacceptedContent(allDataFlows.accepted);
      setcompletedContent(allDataFlows.completed);
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
    setBreadCrumbItems([{ label: resources.messages['dataFlowList'] }]);
  }, [history, match.params.dataFlowId, resources.messages]);

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
      <DataFlowColumn
        navTitle={resources.messages['dataFlow']}
        components={['search']}
        buttonTitle={resources.messages['subscribeButton']}
      />
      <div className={`${styles.container} rep-col-xs-12 rep-col-md-9`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabMenuActiveItem.tabKey === 'pending' ? (
          <>
            <DataFlowList
              listTitle={resources.messages.pendingDataFlowTitle}
              listDescription={resources.messages.pendingDataFlowText}
              listContent={pendingContent}
              dataFetch={dataFetch}
              listType="pending"
            />
            <DataFlowList
              listTitle={resources.messages.acceptedDataFlowTitle}
              listDescription={resources.messages.acceptedDataFlowText}
              listContent={acceptedContent}
              dataFetch={dataFetch}
              listType="accepted"
            />
          </>
        ) : (
          <>
            <DataFlowList
              listTitle={resources.messages.completedDataFlowTitle}
              listDescription={resources.messages.completedDataFlowText}
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
