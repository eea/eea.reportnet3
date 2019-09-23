import React, { useState, useEffect, useContext, useRef } from 'react';
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
import { ListItem } from './_components/ListItem';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';
import { SnapshotList } from './_components/SnapshotList';
import { Spinner } from 'ui/views/_components/Spinner';
import { SplitButton } from 'ui/views/_components/SplitButton';

import { DataFlowService } from 'core/services/DataFlow';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/api/getUrl';

import { ScrollPanel } from 'primereact/scrollpanel';

export const ReportingDataFlow = withRouter(({ history, match }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataFlowData, setDataFlowData] = useState(undefined);
  const [snapshotListData, setSnapshotListData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isActiveContributorsDialog, setIsActiveContributorsDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [dataSetIdToProps, setDataSetIdToProps] = useState();
  const [hasWritePermissions, setHasWritePermissions] = useState(false);

  useEffect(() => {
    if (!isUndefined(user.roles)) {
      setHasWritePermissions(
        UserService.hasPermission(
          user,
          [config.permissions.PROVIDER],
          `${config.permissions.DATA_FLOW}${match.params.dataFlowId}`
        )
      );
    }
  }, [user]);

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
        label: resources.messages['dataFlowList'],
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

  if (loading || isUndefined(dataFlowData)) {
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
      label: resources.messages.delete,
      icon: 'trash',
      show: hasWritePermissions,
      menuItemFunction: () => {}
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
        components={[]}
        entity={`${config.permissions.DATA_FLOW}${dataFlowData.id}`}
      />
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
        <div className={styles.titleBar}>
          <div className={styles.title_wrapper}>
            <h2 className={styles.title}>
              <Icon icon="shoppingCart" />
              {dataFlowData.name}
            </h2>
          </div>
          <div className={styles.option_btns_wrapper}>{<DropdownButton icon="ellipsis" model={dropDownItems} />}</div>
        </div>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.dataSetItem}`}>
              <ListItem
                layout="documents"
                label="DO"
                handleRedirect={() =>
                  handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/documentation-data-set/`)
                }
              />
              <p className={styles.caption}>{resources.messages.documents}</p>
            </div>
            {dataFlowData.datasets.map(dataSet => {
              return (
                <>
                  <div className={`${styles.dataSetItem}`} key={dataSet.id}>
                    <ListItem
                      layout="dataSet"
                      label="DS"
                      handleRedirect={() => {
                        handleRedirect(
                          `/reporting-data-flow/${match.params.dataFlowId}/reporter-data-set/${dataSet.id}`
                        );
                      }}
                      model={
                        hasWritePermissions
                          ? [
                              {
                                label: resources.messages.releaseDataCollection,
                                icon: 'cloudUpload',
                                command: () => showReleaseSnapshotDialog(dataSet.id),
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
                    <p className={styles.caption}>{dataSet.dataSetName}</p>
                  </div>
                </>
              );
            })}
            {console.log(
              'DATA_CUSTODIAN PERMISSION: ',
              UserService.hasPermission(
                user,
                [config.permissions.DATA_CUSTODIAN],
                `${config.permissions.DATA_FLOW}${match.params.dataFlowId}`
              )
            )}
            {UserService.hasPermission(
              user,
              [config.permissions.CUSTODIAN],
              `${config.permissions.DATA_FLOW}${match.params.dataFlowId}`
            ) && (
              <div className={`${styles.dataSetItem}`}>
                <ListItem
                  layout="dashboard"
                  handleRedirect={() =>
                    handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/data-custodian-dashboards/`)
                  }
                />
                <p className={styles.caption}>{resources.messages.dashboards}</p>
              </div>
            )}
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
        <Dialog
          header={dataFlowData.name}
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
          <div className="description">{dataFlowData.description}</div>
          <div className="features">
            <ul>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATA_FLOW}${match.params.dataFlowId}`)}{' '}
                  functionality:
                </strong>{' '}
                {hasWritePermissions ? 'read / write' : 'read'}
              </li>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATA_FLOW}${match.params.dataFlowId}`)} type:
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
