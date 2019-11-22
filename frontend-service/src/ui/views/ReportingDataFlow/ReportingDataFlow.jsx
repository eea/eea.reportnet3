import React, { useContext, useEffect, useRef, useState } from 'react';

import moment from 'moment';
import { withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEmpty, isUndefined } from 'lodash';

import styles from './ReportingDataFlow.module.scss';

import { config } from 'conf';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { routes } from 'ui/routes';

import { BigButton } from './_components/BigButton';
import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ContributorsList } from './_components/ContributorsList';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';
import { DataflowColumn } from 'ui/views/_components/DataFlowColumn';
import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { Dialog } from 'ui/views/_components/Dialog';
import { LoadingContext } from 'ui/views/_components/_context/LoadingContext';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { ScrollPanel } from 'primereact/scrollpanel';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/DataFlow';
import { DatasetService } from 'core/services/DataSet';
import { UserService } from 'core/services/User';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const ReportingDataflow = withRouter(({ history, match }) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowData, setDataflowData] = useState();
  const [datasetIdToProps, setDatasetIdToProps] = useState();
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isActiveContributorsDialog, setIsActiveContributorsDialog] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isActiveReleaseSnapshotConfirmDialog, setIsActiveReleaseSnapshotConfirmDialog] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [loading, setLoading] = useState(true);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);
  const [snapshotsListData, setSnapshotsListData] = useState([]);
  const [snapshotDataToRelease, setSnapshotDataToRelease] = useState('');
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();

  let growlRef = useRef();

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(
          user,
          [config.permissions.PROVIDER],
          `${config.permissions.DATA_FLOW}${match.params.dataflowId}`
        )
      );
    }

    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(
        UserService.hasPermission(
          user,
          [config.permissions.CUSTODIAN],
          `${config.permissions.DATA_FLOW}${match.params.dataflowId}`
        )
      );
    }
  }, [user]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow']
      }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataflowData(dataflow);
      if (!isEmpty(dataflow.designDatasets)) {
        dataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });
        setDesignDatasetSchemas(dataflow.designDatasets);
        const datasetSchemaInfo = [];
        dataflow.designDatasets.map(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });
        setUpdatedDatasetSchema(datasetSchemaInfo);
      }
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadSnapshotList = async datasetId => {
    setSnapshotsListData(await SnapshotService.allReporter(datasetId));
  };

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
  }, [match.params.dataflowId, isDataUpdated]);

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  const handleRedirect = target => {
    history.push(target);
  };

  const dropDownItems = [
    {
      label: resources.messages['manageRoles'],
      icon: 'users',
      show: hasWritePermissions,
      command: () => {
        showContributorsDialog();
      }
    },
    {
      label: resources.messages['properties'],
      icon: 'settings',
      show: true,
      command: e => {
        setIsActivePropertiesDialog(true);
      }
    }
  ];

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setErrorDialogVisible(false);
          setIsDuplicated(false);
        }}
      />
    </div>
  );

  const getDeleteSchemaIndex = index => {
    setDeleteSchemaIndex(index);
    setDeleteDialogVisible(true);
  };

  const onCreateDatasetSchema = () => {
    setNewDatasetDialog(false);
  };

  const onDatasetSchemaNameError = () => {
    setErrorDialogVisible(true);
  };

  const onDeleteDatasetSchema = async index => {
    setDeleteDialogVisible(false);
    showLoading();
    try {
      const response = await DatasetService.deleteSchemaById(designDatasetSchemas[index].datasetId);
      if (response >= 200 && response <= 299) {
        onUpdateData();
      }
    } catch (error) {
      console.error(error.response);
    } finally {
      hideLoading();
    }
  };

  const onDuplicateName = () => {
    setIsDuplicated(true);
  };

  const onHideErrorDialog = () => {
    setErrorDialogVisible(false);
    setIsDuplicated(false);
  };

  const onUpdateData = () => {
    setIsDataUpdated(!isDataUpdated);
  };

  const onSaveName = async (value, index) => {
    await DatasetService.updateSchemaNameById(designDatasetSchemas[index].datasetId, encodeURIComponent(value));
    const titles = [...updatedDatasetSchema];
    titles[index].schemaName = value;
    setUpdatedDatasetSchema(titles);
  };

  const showContributorsDialog = () => {
    setIsActiveContributorsDialog(true);
  };

  const showNewDatasetDialog = () => {
    setNewDatasetDialog(true);
    setIsFormReset(true);
  };

  const showReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToProps(datasetId);
    onLoadSnapshotList(datasetId);
    setIsActiveReleaseSnapshotDialog(true);
  };
  const onReleaseSnapshot = async snapshotId => {
    const snapshotToRelease = await SnapshotService.releaseByIdReporter(
      match.params.dataflowId,
      datasetIdToProps,
      snapshotId
    );

    if (snapshotToRelease.isReleased) {
      onLoadSnapshotList(datasetIdToProps);
      setIsActiveReleaseSnapshotConfirmDialog(false);
    }
  };

  const releseModalFooter = (
    <div>
      <Button
        icon="cloudUpload"
        label={resources.messages['yes']}
        onClick={() => onReleaseSnapshot(snapshotDataToRelease.id)}
      />
      <Button
        icon="cancel"
        className="p-button-secondary"
        label={resources.messages['no']}
        onClick={() => setIsActiveReleaseSnapshotConfirmDialog(false)}
      />
    </div>
  );
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

  return layout(
    <div className="rep-row">
      <DataflowColumn
        subscribeButtonTitle={resources.messages['subscribeThisButton']}
        dataflowTitle={dataflowData.name}
        navTitle={resources.messages['dataflow']}
        components={[]}
        entity={`${config.permissions.DATA_FLOW}${dataflowData.id}`}
        style={{ textAlign: 'left' }}
      />

      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-10`}>
        <div className={styles.titleBar}>
          <div className={styles.title_wrapper}>
            <h2 className={styles.title}>
              <FontAwesomeIcon icon={AwesomeIcons('archive')} style={{ fontSize: '1.2rem' }} /> {dataflowData.name}
              {/* <Title title={`${dataflowData.name}`} icon="archive" iconSize="3.5rem" subtitle={dataflowData.name} /> */}
            </h2>
          </div>
          <div>
            <DropdownButton icon="ellipsis" model={dropDownItems} disabled />
          </div>
        </div>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.datasetItem}`}>
              {isCustodian && (
                <BigButton
                  layout="newItem"
                  caption={resources.messages['newItem']}
                  model={[
                    {
                      label: resources.messages['createNewEmptyDatasetSchema'],
                      icon: 'add',
                      command: () => showNewDatasetDialog()
                    },
                    {
                      label: resources.messages['createNewDatasetFromTemplate'],
                      icon: 'add',
                      disabled: true
                    }
                  ]}
                />
              )}
            </div>
            <div className={`${styles.datasetItem}`}>
              <BigButton
                layout="documents"
                caption={resources.messages['dataflowHelp']}
                handleRedirect={() =>
                  handleRedirect(
                    getUrl(
                      routes.DOCUMENTS,
                      {
                        dataflowId: match.params.dataflowId
                      },
                      true
                    )
                  )
                }
                onWheel={getUrl(
                  routes.DOCUMENTS,
                  {
                    dataflowId: match.params.dataflowId
                  },
                  true
                )}
              />
            </div>
            {!isUndefined(dataflowData.designDatasets) ? (
              dataflowData.designDatasets.map(newDatasetSchema => {
                return (
                  <div className={`${styles.datasetItem}`} key={newDatasetSchema.datasetId}>
                    <BigButton
                      layout="designDatasetSchema"
                      caption={newDatasetSchema.datasetSchemaName}
                      datasetSchemaInfo={updatedDatasetSchema}
                      handleRedirect={() => {
                        handleRedirect(
                          getUrl(
                            routes.DATASET_SCHEMA,
                            {
                              dataflowId: match.params.dataflowId,
                              datasetId: newDatasetSchema.datasetId
                            },
                            true
                          )
                        );
                      }}
                      index={newDatasetSchema.index}
                      onDuplicateName={onDuplicateName}
                      onSaveError={onDatasetSchemaNameError}
                      onSaveName={onSaveName}
                      onWheel={getUrl(
                        routes.DATASET_SCHEMA,
                        {
                          dataflowId: match.params.dataflowId,
                          datasetId: newDatasetSchema.datasetId
                        },
                        true
                      )}
                      placeholder={resources.messages['datasetSchemaNamePlaceholder']}
                      model={[
                        {
                          label: resources.messages['openDataset'],
                          icon: 'openFolder',
                          command: () => {
                            handleRedirect(
                              getUrl(
                                routes.DATASET_SCHEMA,
                                {
                                  dataflowId: match.params.dataflowId,
                                  datasetId: newDatasetSchema.datasetId
                                },
                                true
                              )
                            );
                          }
                        },
                        {
                          label: resources.messages['rename'],
                          icon: 'pencil'
                        },
                        {
                          label: resources.messages['duplicate'],
                          icon: 'clone',
                          disabled: true
                        },
                        {
                          label: resources.messages['delete'],
                          icon: 'trash',
                          disabled: true,
                          command: () => getDeleteSchemaIndex(newDatasetSchema.index)
                        },
                        {
                          label: resources.messages['properties'],
                          icon: 'info',
                          disabled: true
                        }
                      ]}
                    />
                  </div>
                );
              })
            ) : (
              <></>
            )}
            {dataflowData.datasets.map(dataset => {
              return (
                <div className={`${styles.datasetItem}`} key={dataset.datasetId}>
                  <BigButton
                    layout="dataset"
                    caption={dataset.datasetSchemaName}
                    isReleased={dataset.isReleased}
                    handleRedirect={() => {
                      handleRedirect(
                        getUrl(
                          routes.DATASET,
                          {
                            dataflowId: match.params.dataflowId,
                            datasetId: dataset.datasetId
                          },
                          true
                        )
                      );
                    }}
                    onWheel={getUrl(
                      routes.DATASET,
                      {
                        dataflowId: match.params.dataflowId,
                        datasetId: dataset.datasetId
                      },
                      true
                    )}
                    model={
                      hasWritePermissions
                        ? [
                            {
                              label: resources.messages['releaseDataCollection'],
                              icon: 'cloudUpload',
                              command: () => showReleaseSnapshotDialog(dataset.datasetId),
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
            {isCustodian && !isEmpty(dataflowData.datasets) && (
              <div className={`${styles.datasetItem}`}>
                <BigButton
                  layout="dashboard"
                  caption={resources.messages['dashboards']}
                  handleRedirect={() =>
                    handleRedirect(
                      getUrl(
                        routes.DASHBOARDS,
                        {
                          dataflowId: match.params.dataflowId
                        },
                        true
                      )
                    )
                  }
                  onWheel={getUrl(
                    routes.DASHBOARDS,
                    {
                      dataflowId: match.params.dataflowId
                    },
                    true
                  )}
                />
              </div>
            )}
          </div>
        </div>

        <Dialog
          header={`${resources.messages['dataProviderManageContributorsDialogTitle']} "${dataflowData.name}"`}
          visible={isActiveContributorsDialog}
          onHide={() => setIsActiveContributorsDialog(false)}
          style={{ width: '50vw' }}
          maximizable>
          <ContributorsList dataflowId={dataflowData.id} />
        </Dialog>

        <Dialog
          header={resources.messages['newDatasetSchema']}
          visible={newDatasetDialog}
          className={styles.dialog}
          dismissableMask={false}
          onHide={() => (setNewDatasetDialog(false), setIsFormReset(false))}>
          <NewDatasetSchemaForm
            dataflowId={match.params.dataflowId}
            datasetSchemaInfo={updatedDatasetSchema}
            isFormReset={isFormReset}
            onCreate={onCreateDatasetSchema}
            onUpdateData={onUpdateData}
            setNewDatasetDialog={setNewDatasetDialog}
          />
        </Dialog>
        <Dialog
          footer={errorDialogFooter}
          header={resources.messages['error'].toUpperCase()}
          onHide={onHideErrorDialog}
          visible={isDuplicated}>
          <div className="p-grid p-fluid">{resources.messages['duplicateSchemaError']}</div>
        </Dialog>
        <Dialog
          footer={errorDialogFooter}
          header={resources.messages['error'].toUpperCase()}
          onHide={onHideErrorDialog}
          visible={errorDialogVisible}>
          <div className="p-grid p-fluid">{resources.messages['emptyDatasetSchema']}</div>
        </Dialog>
        <ConfirmDialog
          header={resources.messages['delete'].toUpperCase()}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteDatasetSchema(deleteSchemaIndex)}
          onHide={() => setDeleteDialogVisible(false)}
          visible={deleteDialogVisible}>
          {resources.messages['deleteDatasetSchema']}
        </ConfirmDialog>
        <Dialog
          header={`${resources.messages['snapshots'].toUpperCase()} ${dataflowData.name.toUpperCase()}`}
          className={styles.releaseSnapshotsDialog}
          visible={isActiveReleaseSnapshotDialog}
          onHide={() => setIsActiveReleaseSnapshotDialog(false)}
          style={{ width: '30vw' }}>
          {/* <ScrollPanel style={{ width: '100%', height: '50vh' }}> */}
          {!isEmpty(snapshotsListData) ? (
            <SnapshotsList
              className={styles.releaseList}
              snapshotsListData={snapshotsListData}
              onLoadSnapshotList={onLoadSnapshotList}
              setSnapshotDataToRelease={setSnapshotDataToRelease}
              setIsActiveReleaseSnapshotConfirmDialog={setIsActiveReleaseSnapshotConfirmDialog}
            />
          ) : (
            <h3>{resources.messages['emptySnapshotList']}</h3>
          )}
          {/* </ScrollPanel> */}
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
                label={resources.messages['close']}
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
                </strong>
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

        <Dialog
          header={`${resources.messages['releaseSnapshotMessage']}`}
          footer={releseModalFooter}
          visible={isActiveReleaseSnapshotConfirmDialog}
          onHide={() => setIsActiveReleaseSnapshotConfirmDialog(false)}>
          <ul>
            <li>
              <strong>{resources.messages['creationDate']}: </strong>
              {moment(snapshotDataToRelease.creationDate).format('DD/MM/YYYY HH:mm:ss')}
            </li>
            <li>
              <strong>{resources.messages['description']}: </strong>
              {snapshotDataToRelease.description}
            </li>
          </ul>
        </Dialog>
      </div>
    </div>
  );
});
