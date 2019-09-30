import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './ReportingDataFlow.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ContributorsList } from './_components/ContributorsList';
import { DataflowColumn } from 'ui/views/_components/DataFlowColumn';
import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { Dialog } from 'ui/views/_components/Dialog';
import { Icon } from 'ui/views/_components/Icon';
import { BigButton } from './_components/BigButton';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
import { SnapshotList } from './_components/SnapshotList';
import { Spinner } from 'ui/views/_components/Spinner';
import { SplitButton } from 'ui/views/_components/SplitButton';

import { DataflowService } from 'core/services/DataFlow';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/api/getUrl';

import { ScrollPanel } from 'primereact/scrollpanel';

export const ReportingDataflow = withRouter(({ history, match }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowData, setDataflowData] = useState(undefined);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isActiveContributorsDialog, setIsActiveContributorsDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [datasetIdToProps, setDatasetIdToProps] = useState();
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  useEffect(() => {
    if (!isUndefined(user.roles)) {
      setHasWritePermissions(
        UserService.hasPermission(
          user,
          [config.permissions.PROVIDER],
          `${config.permissions.DATA_FLOW}${match.params.dataflowId}`
        )
      );
      setIsCustodian(
        UserService.hasPermission(
          user,
          [config.permissions.CUSTODIAN],
          `${config.permissions.DATA_FLOW}${match.params.dataflowId}`
        )
      );
    }
  }, [user]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(config.DATAFLOWS.url))
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataflowData(dataflow);
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(config.DATAFLOWS.url));
      }
    } finally {
      setLoading(false);
    }
  };
  const onLoadSnapshotList = async datasetId => {
    setSnapshotListData(await SnapshotService.all(datasetId));
  };

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
  }, [match.params.dataflowId]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(config.DATAFLOWS.url))
      },
      {
        label: resources.messages.dataflow
      }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  const handleRedirect = target => {
    history.push(target);
  };

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  let growlRef = useRef();

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading || isUndefined(dataflowData)) {
    return layout(<Spinner />);
  }

  const dropDownItems = [
    {
      label: resources.messages.manageRoles,
      icon: 'users',
      show: hasWritePermissions,
      menuItemFunction: () => {
        showContributorsDialog();
      }
    },

    {
      label: resources.messages.properties,
      icon: 'settings',
      show: true,
      menuItemFunction: e => {
        setIsActivePropertiesDialog(true);
      }
    }
  ];
  const showContributorsDialog = () => {
    setIsActiveContributorsDialog(true);
  };
  const showReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToProps(datasetId);
    onLoadSnapshotList(datasetId);
    setIsActiveReleaseSnapshotDialog(true);
  };

  return layout(
    <div className="rep-row">
      <DataflowColumn
        subscribeButtonTitle={resources.messages.subscribeThisButton}
        dataflowTitle={dataflowData.name}
        navTitle={resources.messages.dataflow}
        components={[]}
        entity={`${config.permissions.DATA_FLOW}${dataflowData.id}`}
      />
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
        <div className={styles.titleBar}>
          <div className={styles.title_wrapper}>
            <h2 className={styles.title}>
              <Icon icon="shoppingCart" />
              {dataflowData.name}
            </h2>
          </div>
          <div>
            <DropdownButton icon="ellipsis" model={dropDownItems} />
          </div>
        </div>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.datasetItem}`}>
              <BigButton
                layout="documents"
                label="DO"
                caption={resources.messages.documents}
                handleRedirect={() => handleRedirect(`/dataflow/${match.params.dataflowId}/documentation-data-set/`)}
              />
            </div>
            {dataflowData.datasets.map(dataset => {
              return (
                <div className={`${styles.datasetItem}`} key={dataset.id}>
                  <BigButton
                    layout="dataset"
                    label="DS"
                    caption={dataset.dataSetName}
                    isReleased={dataset.isReleased}
                    handleRedirect={() => {
                      handleRedirect(`/dataflow/${match.params.dataflowId}/dataset/${dataset.id}`);
                    }}
                    model={
                      hasWritePermissions
                        ? [
                            {
                              label: resources.messages.releaseDataCollection,
                              icon: 'cloudUpload',
                              command: () => showReleaseSnapshotDialog(dataset.id),
                              disabled: false
                            },
                            {
                              label: resources.messages['importFromFile'],
                              icon: 'export',
                              disabled: true
                            },
                            {
                              label: resources.messages['duplicate'],
                              icon: 'clone',
                              disabled: true
                            },
                            {
                              label: resources.messages['properties'],
                              icon: 'info',
                              disabled: true
                            }
                          ]
                        : [
                            {
                              label: resources.messages['properties'],
                              icon: 'info',
                              disabled: true
                            }
                          ]
                    }
                  />
                </div>
              );
            })}
            {isCustodian && (
              <div className={`${styles.datasetItem}`}>
                <BigButton
                  layout="dashboard"
                  caption={resources.messages.dashboards}
                  handleRedirect={() =>
                    handleRedirect(`/dataflow/${match.params.dataflowId}/data-custodian-dashboards/`)
                  }
                />
              </div>
            )}
          </div>
        </div>

        <Dialog
          header={`${resources.messages.dataProviderManageContributorsDialogTitle} "${dataflowData.name}"`}
          visible={isActiveContributorsDialog}
          onHide={() => setIsActiveContributorsDialog(false)}
          style={{ width: '50vw' }}
          maximizable>
          <ContributorsList dataflowId={dataflowData.id} />
        </Dialog>
        <Dialog
          header={dataflowData.name}
          visible={isActiveReleaseSnapshotDialog}
          onHide={() => setIsActiveReleaseSnapshotDialog(false)}
          style={{ width: '30vw' }}>
          <ScrollPanel style={{ width: '100%', height: '50vh' }}>
            <SnapshotList
              snapshotListData={snapshotListData}
              onLoadSnapshotList={onLoadSnapshotList}
              dataflowId={match.params.dataflowId}
              datasetId={datasetIdToProps}
            />
          </ScrollPanel>
        </Dialog>
        <Dialog
          header={dataflowData.name}
          footer={
            <>
              <Button className="p-button-text-only" label="Generate new API-key" />
              <Button className="p-button-text-only" label="Open Metadata" />
              <Button
                className="p-button-secondary"
                icon="cancel"
                label={resources.messages.close}
                onClick={() => setIsActivePropertiesDialog(false)}
              />
            </>
          }
          visible={isActivePropertiesDialog}
          onHide={() => setIsActivePropertiesDialog(false)}
          style={{ width: '50vw' }}>
          <div className="description">{dataflowData.description}</div>
          <div className="features">
            <ul>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATA_FLOW}${match.params.dataflowId}`)}{' '}
                  functionality:
                </strong>{' '}
                {hasWritePermissions ? 'read / write' : 'read'}
              </li>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATA_FLOW}${match.params.dataflowId}`)} type:
                </strong>
              </li>
              <li>
                <strong>REST API key:</strong> <a>Copy API-key</a> (API-key access for developers)
              </li>
            </ul>
          </div>
          <div className="actions"></div>
        </Dialog>
      </div>
    </div>
  );
});
