import React, { useContext, useEffect, useReducer, useState } from 'react';

import { withRouter } from 'react-router-dom';
import { isEmpty, isUndefined, uniq } from 'lodash';

import styles from './Representative.module.scss';

import colors from 'conf/colors.json';
import { config } from 'conf';
import DataflowConf from 'conf/dataflow.config.json';
import { routes } from 'ui/routes';

import { BigButtonList } from './_components/BigButtonList';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowManagementForm } from 'ui/views/_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { MainLayout } from 'ui/views/_components/Layout';
import { SnapshotsDialog } from './_components/SnapshotsDialog';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from '../_components/Title/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';
import { receiptReducer } from 'ui/views/_functions/Reducers/receiptReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const Representative = withRouter(({ history, match }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [dataflowTitle, setDataflowTitle] = useState();
  const [dataProviderId, setDataProviderId] = useState([]);
  const [datasetIdToSnapshotProps, setDatasetIdToSnapshotProps] = useState();
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataflowDialogVisible, setIsDataflowDialogVisible] = useState(false);
  const [isDataflowFormReset, setIsDataflowFormReset] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isEditForm, setIsEditForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [onConfirmDelete, setOnConfirmDelete] = useState();
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});
  const [receiptState, receiptDispatch] = useReducer(receiptReducer, {});

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(
          user,
          [config.permissions.PROVIDER],
          `${config.permissions.DATAFLOW}${match.params.dataflowId}`
        )
      );
    }

    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(
        UserService.hasPermission(
          user,
          [config.permissions.CUSTODIAN],
          `${config.permissions.DATAFLOW}${match.params.dataflowId}`
        )
      );
    }
  }, [user]);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['dataflowList'],
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId: match.params.dataflowId }, true))
      },
      {
        label: match.params.representative || resources.messages['representative'],
        icon: 'representative'
      }
    ]);
  }, []);

  useEffect(() => {
    if (isCustodian && dataflowStatus === DataflowConf.dataflowStatus['DESIGN']) {
      leftSideBarContext.addModels([
        {
          label: 'edit',
          icon: 'edit',
          onClick: e => {
            onShowEditForm();
            dataflowDispatch({ type: 'ON_SELECT_DATAFLOW', payload: match.params.dataflowId });
          },
          title: 'edit'
        },
        {
          label: 'settings',
          icon: 'settings',
          onClick: e => {
            setIsActivePropertiesDialog(true);
          },
          show: true,
          title: 'settings'
        }
      ]);
    } else {
      leftSideBarContext.addModels([
        {
          label: 'settings',
          icon: 'settings',
          onClick: e => {
            setIsActivePropertiesDialog(true);
          },
          title: 'settings'
        }
      ]);
    }
  }, [isCustodian, dataflowStatus]);

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
    onLoadDataflowsData();
  }, [match.params.dataflowId, isDataUpdated]);

  useEffect(() => {
    const refresh = notificationContext.toShow.find(
      notification => notification.key === 'ADD_DATACOLLECTION_COMPLETED_EVENT'
    );
    if (refresh) {
      onUpdateData();
    }
  }, [notificationContext]);

  const handleRedirect = target => {
    history.push(target);
  };

  const onChangeDataflowName = event => {
    setOnConfirmDelete(event.target.value.toLowerCase());
    setDataflowTitle(event.target.value);
  };

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

  const onEditDataflow = (id, newName, newDescription) => {
    setIsDataflowDialogVisible(false);
    dataflowDispatch({
      type: 'ON_EDIT_DATAFLOW',
      payload: { id: id, name: newName, description: newDescription }
    });
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

  const onHideSnapshotDialog = () => {
    setIsActiveReleaseSnapshotDialog(false);
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

      if (!isEmpty(dataflow.datasets)) {
        const representativeId = dataflow.datasets
          .filter(dataset => dataset.datasetSchemaName === match.params.representative)
          .map(id => id.dataProviderId);
        if (representativeId.length === 1) {
          setDataProviderId(uniq(representativeId)[0]);
        }
      }

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

      if (!isEmpty(dataflow.representatives) && !isEmpty(dataflow.datasets)) {
        const representativeId = dataflow.datasets
          .filter(dataset => dataset.datasetSchemaName === match.params.representative)
          .map(id => id.dataProviderId);

        const isOutdated = dataflow.representatives
          .filter(representative => representative.dataProviderId === uniq(representativeId))
          .map(representative => representative.isReceiptOutdated);

        if (isOutdated.length === 1) {
          receiptDispatch({
            type: 'INIT_DATA',
            payload: { isLoading: false, isOutdated: isOutdated[0], receiptData: {} }
          });
        }
      }
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onSaveName = async (value, index) => {
    await DatasetService.updateSchemaNameById(designDatasetSchemas[index].datasetId, encodeURIComponent(value));
    const titles = [...updatedDatasetSchema];
    titles[index].schemaName = value;
    setUpdatedDatasetSchema(titles);
  };

  const onShowDeleteDataflowDialog = () => {
    setIsActivePropertiesDialog(false);
    setIsDeleteDialogVisible(true);
  };

  const onShowEditForm = () => {
    setIsEditForm(true);
    setIsDataflowDialogVisible(true);
  };

  const onShowReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToSnapshotProps(datasetId);
    setIsActiveReleaseSnapshotDialog(true);
  };

  const onUpdateData = () => {
    setIsDataUpdated(!isDataUpdated);
  };

  const layout = children => (
    <MainLayout
      leftSideBarConfig={{
        isCustodian,
        buttons: []
      }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );
  if (loading || isUndefined(dataflowData)) {
    return layout(<Spinner />);
  }
  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          title={!isUndefined(dataflowState[match.params.dataflowId]) ? `${match.params.representative}` : null}
          subtitle={` ${TextUtils.ellipsis(dataflowState[match.params.dataflowId].name)}`}
          icon="representative"
          iconSize="4rem"
        />

        <BigButtonList
          dataflowData={dataflowData}
          dataflowId={match.params.dataflowId}
          dataProviderId={dataProviderId}
          designDatasetSchemas={designDatasetSchemas}
          handleRedirect={handleRedirect}
          hasWritePermissions={hasWritePermissions}
          isCustodian={isCustodian}
          onUpdateData={onUpdateData}
          receiptDispatch={receiptDispatch}
          receiptState={receiptState}
          representative={match.params.representative}
          showReleaseSnapshotDialog={onShowReleaseSnapshotDialog}
          updatedDatasetSchema={updatedDatasetSchema}
        />

        <SnapshotsDialog
          dataflowData={dataflowData}
          dataflowId={match.params.dataflowId}
          datasetId={datasetIdToSnapshotProps}
          hideSnapshotDialog={onHideSnapshotDialog}
          isSnapshotDialogVisible={isActiveReleaseSnapshotDialog}
          receiptDispatch={receiptDispatch}
          setSnapshotDialog={setIsActiveReleaseSnapshotDialog}
        />

        <Dialog
          header={resources.messages['properties']}
          footer={
            <>
              <div className="p-toolbar-group-left">
                {isCustodian && dataflowStatus === DataflowConf.dataflowStatus['DESIGN'] && (
                  <Button
                    className="p-button-text-only"
                    label="Delete this dataflow"
                    style={{ backgroundColor: colors.errors, borderColor: colors.errors }}
                    onClick={() => onShowDeleteDataflowDialog()}
                  />
                )}
              </div>
              <Button className="p-button-text-only" label="Generate new API-key" disabled />
              <Button className="p-button-text-only" label="Open Metadata" disabled />
              <Button
                className="p-button-secondary p-button-animated-blink"
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
            {!isUndefined(dataflowState[match.params.dataflowId]) && dataflowState[match.params.dataflowId].description}
          </div>
          <div className="features">
            <ul>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATAFLOW}${match.params.dataflowId}`)}{' '}
                  functionality:
                </strong>
                {hasWritePermissions ? 'read / write' : 'read'}
              </li>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATAFLOW}${match.params.dataflowId}`)} type:
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

        {!isUndefined(dataflowState[match.params.dataflowId]) && (
          <ConfirmDialog
            header={resources.messages['delete'].toUpperCase()}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            disabledConfirm={onConfirmDelete !== dataflowState[match.params.dataflowId].name.toLowerCase()}
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
        )}
      </div>
    </div>
  );
});

export { Representative };
