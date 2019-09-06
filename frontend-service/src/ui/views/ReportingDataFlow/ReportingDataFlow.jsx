import React, { useState, useEffect, useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './ReportingDataFlow.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ContributorsList } from './_components/ContributorsList';
import { DataFlowColumn } from 'ui/views/_components/DataFlowColumn';
import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { Dialog } from 'ui/views/_components/Dialog';
import { Icon } from 'ui/views/_components/Icon';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotList } from './_components/SnapshotList';
import { Spinner } from 'ui/views/_components/Spinner';
import { SplitButton } from 'ui/views/_components/SplitButton';

import { DataFlowService } from 'core/services/DataFlow';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/api/getUrl';

import { ScrollPanel } from 'primereact/scrollpanel';

export const ReportingDataFlow = withRouter(({ history, match }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataFlowData, setDataFlowData] = useState(undefined);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isActiveContributorsDialog, setIsActiveContributorsDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [dataSetIdToProps, setDataSetIdToProps] = useState();

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  const onLoadReportingDataFlow = async () => {
    try {
      const dataFlow = await DataFlowService.reporting(match.params.dataFlowId);
      setDataFlowData(dataFlow);
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(config.DATAFLOW_TASKS.url));
      }
    } finally {
      setLoading(false);
    }
  };
  const onLoadSnapshotList = async dataSetId => {
    setSnapshotListData(await SnapshotService.all(dataSetId));
  };

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataFlow();
  }, [match.params.dataFlowId]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages.dataFlowTask,
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages.reportingDataFlow
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

  if (loading || isUndefined(dataFlowData)) {
    return layout(<Spinner />);
  }

  const dropDownItems = [
    {
      label: resources.messages.manageRoles,
      icon: 'users',
      menuItemFunction: () => {
        showContributorsDialog();
      }
    },

    {
      label: resources.messages.delete,
      icon: 'trash',
      menuItemFunction: () => {}
    },

    {
      label: resources.messages.properties,
      icon: 'settings',
      menuItemFunction: () => {}
    }
  ];
  const showContributorsDialog = () => {
    setIsActiveContributorsDialog(true);
  };
  const showReleaseSnapshotDialog = async dataSetId => {
    setDataSetIdToProps(dataSetId);
    onLoadSnapshotList(dataSetId);
    setIsActiveReleaseSnapshotDialog(true);
  };

  return layout(
    <div className="rep-row">
      <DataFlowColumn
        buttonTitle={resources.messages.subscribeThisButton}
        dataFlowTitle={dataFlowData.name}
        navTitle={resources.messages.dataFlow}
        components={['dashboard']}
      />
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
        <div className={styles.titleBar}>
          <div className={styles.title_wrapper}>
            <h2 className={styles.title}>
              <Icon icon="shoppingCart" />
              {dataFlowData.name}
            </h2>
          </div>
          <div className={styles.option_btns_wrapper}>
            <DropdownButton icon="ellipsis" model={dropDownItems} />
          </div>
        </div>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.dataSetItem}`}>
              <Button
                className="p-button-warning"
                label={resources.messages.do}
                onClick={e => {
                  handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/documentation-data-set/`);
                }}
              />
              <p className={styles.caption}>{resources.messages.documents}</p>
            </div>
            {dataFlowData.datasets.map(dataSet => {
              return (
                <div className={`${styles.dataSetItem}`} key={dataSet.id}>
                  <SplitButton
                    label={resources.messages.ds}
                    model={[
                      {
                        label: resources.messages.releaseDataCollection,
                        icon: config.icons.cloudUpload,
                        command: () => {
                          showReleaseSnapshotDialog(dataSet.id);
                        }
                      },
                      {
                        label: resources.messages.importFromFile,
                        icon: config.icons.import
                      },
                      {
                        label: resources.messages.duplicate,
                        icon: config.icons.clone
                      },
                      {
                        label: resources.messages.properties,
                        icon: config.icons.info
                      }
                    ]}
                    onClick={() => {
                      handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/reporter-data-set/${dataSet.id}`);
                    }}
                  />
                  <p className={styles.caption}>{dataSet.dataSetName}</p>
                </div>
              );
            })}
          </div>
        </div>

        <Dialog
          header={`${resources.messages.dataProviderManageContributorsDialogTitle} "${dataFlowData.name}"`}
          visible={isActiveContributorsDialog}
          onHide={() => setIsActiveContributorsDialog(false)}
          style={{ width: '50vw' }}
          maximizable>
          <ContributorsList dataFlowId={dataFlowData.id} />
        </Dialog>
        <Dialog
          header={dataFlowData.name}
          visible={isActiveReleaseSnapshotDialog}
          onHide={() => setIsActiveReleaseSnapshotDialog(false)}
          style={{ width: '30vw' }}>
          <ScrollPanel style={{ width: '100%', height: '50vh' }}>
            <SnapshotList
              snapshotListData={snapshotListData}
              onLoadSnapshotList={onLoadSnapshotList}
              dataFlowId={match.params.dataFlowId}
              dataSetId={dataSetIdToProps}
            />
          </ScrollPanel>
        </Dialog>
      </div>
    </div>
  );
});
