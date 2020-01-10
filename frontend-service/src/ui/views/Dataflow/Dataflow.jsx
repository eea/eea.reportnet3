import React, { useContext, useEffect, useReducer, useState } from 'react';

import moment from 'moment';
import { withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEmpty, isUndefined } from 'lodash';

import styles from './Dataflow.module.scss';

import colors from 'conf/colors.json';
import { config } from 'conf';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { routes } from 'ui/routes';

import { BigButton } from './_components/BigButton';
import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowManagementForm } from 'ui/views/_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { InputText } from 'ui/views/_components/InputText';
import { LeftSideBar } from 'ui/views/_components/LeftSideBar';
import { MainLayout } from 'ui/views/_components/Layout';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';
import { RepresentativesList } from './_components/RepresentativesList';
import { SnapshotsList } from './_components/SnapshotsList';
import { Spinner } from 'ui/views/_components/Spinner';

import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';
import { TextUtils } from 'ui/views/_functions/Utils';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserService } from 'core/services/User';
import { SnapshotService } from 'core/services/Snapshot';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const Dataflow = withRouter(({ history, match }) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const notificationContext = useContext(NotificationContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [dataflowTitle, setDataflowTitle] = useState();
  const [datasetIdToProps, setDatasetIdToProps] = useState();
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isActiveManageRolesDialog, setIsActiveManageRolesDialog] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isActiveReleaseSnapshotConfirmDialog, setIsActiveReleaseSnapshotConfirmDialog] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataflowDialogVisible, setIsDataflowDialogVisible] = useState(false);
  const [isDataflowFormReset, setIsDataflowFormReset] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [loading, setLoading] = useState(true);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);
  const [snapshotsListData, setSnapshotsListData] = useState([]);
  const [snapshotDataToRelease, setSnapshotDataToRelease] = useState('');
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();
  const [onConfirmDelete, setOnConfirmDelete] = useState();

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});

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
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive'
      }
    ]);
  }, [history, match.params.dataflowId, resources.messages]);

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
    onLoadDataflowsData();
  }, [match.params.dataflowId, isDataUpdated]);

  const onDeleteDataflow = async () => {
    setIsDeleteDialogVisible(false);
    showLoading();
    try {
      const response = await DataflowService.deleteById(match.params.dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
      } else {
        throw new Error(`Delete dataflow error with this status: ', ${response.status}`);
      }
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DELETE_BY_ID_ERROR',
        content: {
          dataflowId: match.params.dataflowId
        }
      });
    } finally {
      hideLoading();
    }
  };

  if (isDeleteDialogVisible && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

  const onChangeDataflowName = event => {
    setOnConfirmDelete(event.target.value);
    setDataflowTitle(event.target.value);
  };

  const onEditDataflow = (id, newName, newDescription) => {
    setIsDataflowDialogVisible(false);
    dataflowDispatch({
      type: 'ON_EDIT_DATAFLOW',
      payload: { id: id, name: newName, description: newDescription }
    });
  };

  const onLoadDataflowsData = async () => {
    try {
      const allDataflows = await DataflowService.all();
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
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataflowData(dataflow);
      setDataflowStatus(dataflow.status);
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

  const handleRedirect = target => {
    history.push(target);
  };

  const onHideDeleteDataflowDialog = () => {
    setIsDeleteDialogVisible(false);
    setIsActivePropertiesDialog(true);
    setDataflowTitle('');
  };

  const onHideDialog = () => {
    setIsDataflowDialogVisible(false);
    setIsDataflowFormReset(false);
  };

  const onShowEditForm = () => {
    setIsEditForm(true);
    setIsDataflowDialogVisible(true);
  };

  const onShowDeleteDataflowDialog = () => {
    setIsActivePropertiesDialog(false);
    setIsDeleteDialogVisible(true);
  };

  const dropDownItems =
    isCustodian && dataflowStatus === config.dataflowStatus['DESIGN']
      ? [
          {
            label: resources.messages['edit'],
            icon: 'edit',
            disabled: !isCustodian,
            command: () => {
              onShowEditForm();
              dataflowDispatch({ type: 'ON_SELECT_DATAFLOW', payload: match.params.dataflowId });
            }
          },
          {
            label: resources.messages['manageRoles'],
            icon: 'users',
            show: hasWritePermissions,
            command: () => {
              showContributorsDialog();
            }
          },
          {
            label: resources.messages['settings'],
            icon: 'settings',
            show: true,
            command: e => {
              setIsActivePropertiesDialog(true);
            }
          }
        ]
      : [
          {
            label: resources.messages['settings'],
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
    setIsActiveManageRolesDialog(true);
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

  const closeManageRolesDialog = (
    <div>
      <Button
        className="p-button-secondary"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => setIsActiveManageRolesDialog(false)}
      />
    </div>
  );

  const onReleaseSnapshot = async snapshotId => {
    try {
      await SnapshotService.releaseByIdReporter(match.params.dataflowId, datasetIdToProps, snapshotId);
      onLoadSnapshotList(datasetIdToProps);
    } catch (error) {
      notificationContext.add({
        type: 'RELEASED_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
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
        <BreadCrumb model={breadCrumbItems} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading || isUndefined(dataflowData)) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row">
      <LeftSideBar
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
              <FontAwesomeIcon icon={AwesomeIcons('archive')} style={{ fontSize: '1.2rem' }} />
              {!isUndefined(dataflowState[match.params.dataflowId])
                ? dataflowState[match.params.dataflowId].name
                : null}
              {/* <Title title={`${dataflowData.name}`} icon="archive" iconSize="3.5rem" subtitle={dataflowData.name} /> */}
            </h2>
          </div>
          <div>
            <DropdownButton icon="ellipsis" model={dropDownItems} disabled={false} />
          </div>
        </div>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            {isCustodian && (
              <div className={`${styles.datasetItem}`}>
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
              </div>
            )}
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
                      dataflowStatus={dataflowStatus}
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
                          icon: 'pencil',
                          disabled: dataflowStatus !== config.dataflowStatus['DESIGN']
                        },
                        {
                          label: resources.messages['duplicate'],
                          icon: 'clone',
                          disabled: true
                        },
                        {
                          label: resources.messages['delete'],
                          icon: 'trash',
                          disabled: dataflowStatus !== config.dataflowStatus['DESIGN'],
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
          header={resources.messages['manageRolesDialogTitle']}
          footer={closeManageRolesDialog}
          visible={isActiveManageRolesDialog}
          onHide={() => setIsActiveManageRolesDialog(false)}
          style={{ width: '50vw' }}
          maximizable>
          <RepresentativesList dataflowId={dataflowData.id} />
        </Dialog>

        <Dialog
          header={resources.messages['newDatasetSchema']}
          visible={newDatasetDialog}
          className={styles.dialog}
          dismissableMask={false}
          onHide={() => {
            setNewDatasetDialog(false);
            setIsFormReset(false);
          }}>
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
          header={resources.messages['properties']}
          footer={
            <>
              <div className="p-toolbar-group-left">
                {isCustodian && dataflowStatus === config.dataflowStatus['DESIGN'] ? (
                  <Button
                    className="p-button-text-only"
                    label="Delete this dataflow"
                    style={{ backgroundColor: colors.errors, borderColor: colors.errors }}
                    onClick={() => onShowDeleteDataflowDialog()}
                  />
                ) : null}
              </div>
              <Button className="p-button-text-only" label="Generate new API-key" disabled />
              <Button className="p-button-text-only" label="Open Metadata" disabled />
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
          <div className="description">
            {!isUndefined(dataflowState[match.params.dataflowId])
              ? dataflowState[match.params.dataflowId].description
              : null}
          </div>
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
              {moment(snapshotDataToRelease.creationDate).format('YYYY-MM-DD HH:mm:ss')}
            </li>
            <li>
              <strong>{resources.messages['description']}: </strong>
              {snapshotDataToRelease.description}
            </li>
          </ul>
        </Dialog>

        <Dialog
          className={styles.dialog}
          dismissableMask={false}
          header={isEditForm ? resources.messages['updateDataflow'] : resources.messages['createNewDataflow']}
          onHide={onHideDialog}
          visible={isDataflowDialogVisible}>
          <DataflowManagementForm
            dataflowId={match.params.dataflowId}
            dataflowValues={dataflowState}
            isDialogVisible={isDataflowDialogVisible}
            isEditForm={isEditForm}
            isFormReset={isDataflowFormReset}
            onCancel={onHideDialog}
            onEdit={onEditDataflow}
            selectedDataflow={dataflowState.selectedDataflow}
          />
        </Dialog>

        {!isUndefined(dataflowState) ? (
          <ConfirmDialog
            header={resources.messages['delete'].toUpperCase()}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            disabledConfirm={onConfirmDelete !== dataflowState[match.params.dataflowId].name}
            onConfirm={() => onDeleteDataflow()}
            onHide={onHideDeleteDataflowDialog}
            styleConfirm={{ backgroundColor: colors.errors, borderColor: colors.errors }}
            visible={isDeleteDialogVisible}>
            <p>{resources.messages['deleteDataflow']}</p>
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
                  dataflowName: dataflowState[match.params.dataflowId].name
                })
              }}></p>
            <p>
              <InputText
                autoFocus={true}
                className={`${styles.inputText}`}
                onChange={e => onChangeDataflowName(e)}
                value={dataflowTitle}
              />
            </p>
          </ConfirmDialog>
        ) : (
          <></>
        )}
      </div>
    </div>
  );
});

export { Dataflow };
