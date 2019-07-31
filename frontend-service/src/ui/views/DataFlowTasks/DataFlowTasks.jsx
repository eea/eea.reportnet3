import React, { useEffect, useContext, useState } from 'react';

import styles from './DataFlowTasks.module.scss';

import { config } from 'assets/conf';

import { BreadCrumb } from 'primereact/breadcrumb';
import { DataFlowColumn } from 'ui/views/_components/DataFlowColumn';
import { DataFlowList } from './DataFlowList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ProgressSpinner } from 'primereact/progressspinner';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { TabMenu } from 'primereact/tabmenu';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const i18nKey = 'app.components.pages.dataFlowTasks';

export const DataFlowTasks = ({ match, history }) => {
  const resources = useContext(ResourcesContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [tabMenuItems, setTabMenuItems] = useState([
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
  const [tabData, setTabData] = useState([]);
  const [loading, setLoading] = useState(true);
  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

  const dataFetch = () => {
    setLoading(true);
    const c = {
      listKeys: [],
      apiUrl: '',
      userId: 2,
      queryString: {}
    };
    if (tabMenuActiveItem.tabKey === 'pending') {
      c.listKeys.push('pending');
      c.listKeys.push('accepted');
      // c.apiUrl = `${config.loadDataFlowTaskPendingAcceptedAPI.url}${c.userId}`;
      c.apiUrl = '/jsons/DataFlaws2.json';
      c.queryString = {};
    } else {
      c.listKeys.push('completed');
      c.apiUrl = '';
    }

    HTTPRequester.get({
      url: window.env.REACT_APP_JSON ? c.apiUrl : c.apiUrl,
      queryString: c.queryString
    })
      .then(response => {
        setTabData(
          c.listKeys.map(key => {
            return {
              listContent: response.data.filter(data => data.userRequestStatus.toLowerCase() === key),
              listType: key,
              listTitle: resources.messages[`${key}DataFlowTitle`],
              listDescription: resources.messages[`${key}DataFlowText`]
            };
          })
        );
        setLoading(false);
      })
      .catch(error => {
        console.log('error', error);
        setLoading(false);
        return error;
      });
  };

  useEffect(() => {
    dataFetch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resources.messages, tabMenuActiveItem]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([{ label: resources.messages['dataFlowTask'] }]);
  }, [history, match.params.dataFlowId, resources.messages]);

  const layout = children => {
    return (
      <MainLayout>
        <div className="titleDiv">
          <BreadCrumb model={breadCrumbItems} home={home} />
        </div>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<ProgressSpinner />);
  }

  return layout(
    <div className="rep-row">
      <DataFlowColumn navTitle={resources.messages['dataFlow']} search={false} />
      <div className={`${styles.container} rep-col-xs-12 rep-col-md-9`}>
        <TabMenu model={tabMenuItems} activeItem={tabMenuActiveItem} onTabChange={e => setTabMenuActiveItem(e.value)} />
        {tabData.map((data, i) => (
          <DataFlowList {...data} key={i} dataFetch={dataFetch} />
        ))}
      </div>
    </div>
  );
};
