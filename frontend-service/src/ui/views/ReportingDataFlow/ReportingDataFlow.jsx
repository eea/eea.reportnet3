import React, { useState, useEffect, useContext } from 'react';

import styles from './ReportingDataFlow.module.css';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { DataFlowColumn } from 'ui/views/_components/DataFlowColumn';
import { Icon } from 'ui/views/_components/Icon';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { SplitButton } from 'primereact/splitbutton';

import { DataFlowService } from 'core/services/DataFlow';

export const ReportingDataFlow = ({ history, match }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataFlowData, setDataFlowData] = useState(undefined);
  const [loading, setLoading] = useState(true);

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  const onLoadReportingDataFlow = async () => {
    const dataFlow = await DataFlowService.reporting(match.params.dataFlowId);
    setDataFlowData(dataFlow);
    setLoading(false);
  };

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataFlow();
  }, [match.params.dataFlowId]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages['reportingDataFlow']
      }
    ]);
  }, [history, match.params.dataFlowId, resources.messages]);

  const handleRedirect = target => {
    history.push(target);
  };

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
        buttonTitle={resources.messages['subscribeThisButton']}
        dataFlowTitle={dataFlowData.name}
        navTitle={resources.messages['dataFlow']}
      />
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
        <h2 className={styles.title}>
          <Icon icon="shoppingCart" />
          {dataFlowData.name}
        </h2>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.dataSetItem}`}>
              <Button
                className="p-button-warning"
                label={resources.messages['do']}
                onClick={e => {
                  handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/documentation-data-set/`);
                }}
              />
              <p className={styles.caption}>{resources.messages['documents']}</p>
            </div>
            {dataFlowData.datasets.map(item => {
              return (
                <div className={`${styles.dataSetItem}`} key={item.id}>
                  <SplitButton
                    label={resources.messages['ds']}
                    model={[
                      {
                        label: resources.messages['releaseDataCollection'],
                        icon: config.icons.archive
                      },
                      {
                        label: resources.messages['importFromFile'],
                        icon: config.icons.import
                      },
                      {
                        label: resources.messages['duplicate'],
                        icon: config.icons.clone
                      },
                      {
                        label: resources.messages['properties'],
                        icon: config.icons.info
                      }
                    ]}
                    onClick={e => {
                      handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/reporter-data-set/${item.id}`);
                    }}
                  />
                  <p className={styles.caption}>{item.dataSetName}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
