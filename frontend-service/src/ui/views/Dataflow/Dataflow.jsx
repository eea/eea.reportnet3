import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import styles from './Dataflow.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';
import colors from 'conf/colors.json';
import DataflowConf from 'conf/dataflow.config.json';

import { BigButtonList } from './_components/BigButtonList';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowManagementForm } from 'ui/views/_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { MainLayout } from 'ui/views/_components/Layout';
import { RepresentativesList } from './_components/RepresentativesList';
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

import { dataflowDataReducer } from './_functions/dataflowDataReducer';
import { receiptReducer } from 'ui/views/_functions/Reducers/receiptReducer';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId }
  } = match;

  const { showLoading, hideLoading } = useContext(LoadingContext);
  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataProviderId, setDataProviderId] = useState([]);
  const [datasetIdToSnapshotProps, setDatasetIdToSnapshotProps] = useState();
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataSchemaCorrect, setIsDataSchemaCorrect] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();

  const dataflowInitialState = {
    data: {},
    deleteInput: '',
    description: '',
    hasRepresentativesWithoutDatasets: false,
    id: dataflowId,
    isDeleteDialogVisible: false,
    isEditDialogVisible: false,
    isManageRolesDialogVisible: false,
    isPropertiesDialogVisible: false,
    name: '',
    status: ''
  };

  const [dataflowDataState, dataflowDataDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);
  const [receiptState, receiptDispatch] = useReducer(receiptReducer, {});

  const deleteInputRef = useRef(null);

  useEffect(() => {
    if (!isEmpty(dataflowDataState.data.representatives)) {
      const representativesNoDatasets = dataflowDataState.data.representatives.filter(
        representative => !representative.hasDatasets
      );
      setHasRepresentativesWithoutDatasets(!isEmpty(representativesNoDatasets));
    }
  }, [dataflowDataState.data.representatives]);

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATAFLOW}${dataflowId}`)
      );
    }

    if (!isUndefined(user.contextRoles)) {
      setIsCustodian(
        UserService.hasPermission(user, [config.permissions.CUSTODIAN], `${config.permissions.DATAFLOW}${dataflowId}`)
      );
    }
  }, [user]);

  //Bread Crumbs settings
  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['dataflows'],
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive'
      }
    ]);
  }, []);

  useEffect(() => {
    if (isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DESIGN']) {
      leftSideBarContext.addModels([
        {
          className: 'dataflow-edit-help-step',
          icon: 'edit',
          label: 'edit',
          onClick: () => onManageDialogs('isEditDialogVisible', true),
          title: 'edit'
        },
        {
          className: 'dataflow-manage-roles-help-step',
          icon: 'manageRoles',
          label: 'manageRoles',
          onClick: () => onManageDialogs('isManageRolesDialogVisible', true),
          title: 'manageRoles'
        },
        {
          className: 'dataflow-settings-help-step',
          icon: 'settings',
          label: 'settings',
          onClick: () => onManageDialogs('isPropertiesDialogVisible', true),
          show: true,
          title: 'properties'
        }
      ]);
    } else if (isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DRAFT']) {
      leftSideBarContext.addModels([
        {
          className: 'dataflow-manage-roles-help-step',
          icon: 'manageRoles',
          label: 'manageRoles',
          onClick: () => onManageDialogs('isManageRolesDialogVisible', true),
          title: 'manageRoles'
        },
        {
          className: 'dataflow-settings-help-step',
          icon: 'settings',
          label: 'settings',
          onClick: () => onManageDialogs('isPropertiesDialogVisible', true),
          show: true,
          title: 'properties'
        }
      ]);
    } else {
      leftSideBarContext.addModels([
        {
          className: 'dataflow-settings-provider-help-step',
          icon: 'settings',
          label: 'settings',
          onClick: () => onManageDialogs('isPropertiesDialogVisible', true),
          title: 'settings'
        }
      ]);
    }
  }, [isCustodian, dataflowDataState.status]);

  useEffect(() => {
    const steps = filterHelpSteps();
    leftSideBarContext.addHelpSteps('dataflowHelp', steps);
  }, [
    dataflowDataState.data,
    dataflowDataState.hasRepresentativesWithoutDatasets,
    dataflowDataState.status,
    dataflowId,
    designDatasetSchemas,
    isCustodian,
    isDataSchemaCorrect
  ]);

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
    onLoadSchemasValidations();
  }, [dataflowId, isDataUpdated]);

  useEffect(() => {
    if (dataflowDataState.isDeleteDialogVisible && !isNil(deleteInputRef.current)) {
      deleteInputRef.current.element.focus();
    }
  }, [dataflowDataState.isDeleteDialogVisible]);

  const filterHelpSteps = () => {
    const dataflowSteps = [
      {
        content: <h2>{resources.messages['dataflowHelp']}</h2>,
        locale: { skip: <strong aria-label="skip">{resources.messages['skipHelp']}</strong> },
        placement: 'center',
        target: 'body'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep1']}</h2>,
        target: '.dataflow-new-item-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep2']}</h2>,
        target: '.dataflow-documents-weblinks-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep3']}</h2>,
        target: '.dataflow-schema-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep4']}</h2>,
        target: '.dataflow-dataset-container-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep5']}</h2>,
        target: '.dataflow-dataset-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep6']}</h2>,
        target: '.dataflow-datacollection-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep7']}</h2>,
        target: '.dataflow-dashboards-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep8']}</h2>,
        target: '.dataflow-edit-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep9']}</h2>,
        target: '.dataflow-manage-roles-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep10']}</h2>,
        target: '.dataflow-settings-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep11']}</h2>,
        target: '.dataflow-settings-provider-help-step'
      }
    ];

    const loadedClassesSteps = [...dataflowSteps].filter(
      dataflowStep =>
        !isUndefined(
          document.getElementsByClassName(dataflowStep.target.substring(1, dataflowStep.target.length))[0]
        ) || dataflowStep.target === 'body'
    );
    return loadedClassesSteps;
  };

  const handleRedirect = target => {
    history.push(target);
  };

  const manageRoleDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => onManageDialogs('isManageRolesDialogVisible', false)}
    />
  );
  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDataDispatch({
      type: 'HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const onConfirmDelete = event =>
    dataflowDataDispatch({ type: 'ON_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const onDeleteDataflow = async () => {
    onManageDialogs('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true);
    showLoading();
    try {
      const response = await DataflowService.deleteById(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
      } else {
        throw new Error(`Delete dataflow error with this status: ', ${response.status}`);
      }
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DELETE_BY_ID_ERROR',
        content: {
          dataflowId
        }
      });
    } finally {
      hideLoading();
    }
  };

  const onEditDataflow = (newName, newDescription) => {
    dataflowDataDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isVisible: false }
    });
  };

  const onHideSnapshotDialog = () => setIsActiveReleaseSnapshotDialog(false);

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);
      dataflowDataDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: dataflow, name: dataflow.name, description: dataflow.description, status: dataflow.status }
      });

      if (!isEmpty(dataflow.datasets)) {
        const dataProviderIds = dataflow.datasets.map(dataset => dataset.dataProviderId);
        if (uniq(dataProviderIds).length === 1) {
          setDataProviderId(dataProviderIds[0]);
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
      if (!isEmpty(dataflow.representatives)) {
        const isOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);
        if (isOutdated.length === 1) {
          receiptDispatch({
            type: 'INIT_DATA',
            payload: { isLoading: false, isOutdated: isOutdated[0] }
          });
        }
      }
    } catch (error) {
      notificationContext.add({
        type: 'RELEASED_BY_ID_REPORTER_ERROR',
        content: {}
      });
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  useCheckNotifications(['RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'], onLoadReportingDataflow);

  const onLoadSchemasValidations = async () =>
    setIsDataSchemaCorrect(await DataflowService.schemasValidation(dataflowId));

  const onManageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDataDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const onSaveName = async (value, index) => {
    await DatasetService.updateSchemaNameById(designDatasetSchemas[index].datasetId, encodeURIComponent(value));
    const titles = [...updatedDatasetSchema];
    titles[index].schemaName = value;
    setUpdatedDatasetSchema(titles);
  };

  const onShowReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToSnapshotProps(datasetId);
    setIsActiveReleaseSnapshotDialog(true);
  };

  const onUpdateData = () => {
    setIsDataUpdated(!isDataUpdated);
  };

  useCheckNotifications(['ADD_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);

  const layout = children => {
    return (
      <MainLayout leftSideBarConfig={{ isCustodian, buttons: [] }}>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading || isNil(dataflowDataState.data)) {
    return layout(<Spinner />);
  }

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          icon="archive"
          iconSize="4rem"
          subtitle={resources.messages['dataflow']}
          title={TextUtils.ellipsis(dataflowDataState.name)}
        />

        <BigButtonList
          dataflowDataState={dataflowDataState}
          dataflowData={dataflowDataState.data}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          designDatasetSchemas={designDatasetSchemas}
          handleRedirect={handleRedirect}
          hasWritePermissions={hasWritePermissions}
          isCustodian={isCustodian}
          isDataSchemaCorrect={isDataSchemaCorrect}
          onSaveName={onSaveName}
          onUpdateData={onUpdateData}
          receiptDispatch={receiptDispatch}
          receiptState={receiptState}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
          showReleaseSnapshotDialog={onShowReleaseSnapshotDialog}
          updatedDatasetSchema={updatedDatasetSchema}
        />

        <SnapshotsDialog
          dataflowData={dataflowDataState.data}
          dataflowId={dataflowId}
          datasetId={datasetIdToSnapshotProps}
          hideSnapshotDialog={onHideSnapshotDialog}
          isSnapshotDialogVisible={isActiveReleaseSnapshotDialog}
          setSnapshotDialog={setIsActiveReleaseSnapshotDialog}
        />

        {isCustodian && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRoleDialogFooter}
            header={resources.messages['manageRolesDialogTitle']}
            onHide={() => onManageDialogs('isManageRolesDialogVisible', false)}
            visible={dataflowDataState.isManageRolesDialogVisible}>
            <div className={styles.dialog}>
              <RepresentativesList
                dataflowRepresentatives={dataflowDataState.data.representatives}
                dataflowId={dataflowId}
                isActiveManageRolesDialog={dataflowDataState.isManageRolesDialogVisible}
                setHasRepresentativesWithoutDatasets={setHasRepresentativesWithoutDatasets}
              />
            </div>
          </Dialog>
        )}

        <Dialog
          header={resources.messages['properties']}
          footer={
            <>
              <div className="p-toolbar-group-left">
                {isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DESIGN'] && (
                  <Button
                    className="p-button-text-only"
                    label="Delete this dataflow"
                    onClick={() => onManageDialogs('isDeleteDialogVisible', true, 'isPropertiesDialogVisible', false)}
                    style={{ backgroundColor: colors.errors, borderColor: colors.errors }}
                  />
                )}
              </div>
              <Button className="p-button-text-only" label="Generate new API-key" disabled />
              <Button className="p-button-text-only" label="Open Metadata" disabled />
              <Button
                className="p-button-secondary p-button-animated-blink"
                icon="cancel"
                label={resources.messages['close']}
                onClick={() => onManageDialogs('isPropertiesDialogVisible', false)}
              />
            </>
          }
          visible={dataflowDataState.isPropertiesDialogVisible}
          onHide={() => onManageDialogs('isPropertiesDialogVisible', false)}
          style={{ width: '50vw' }}>
          <div className="description">{dataflowDataState.description}</div>
          <div className="features">
            <ul>
              <li>
                <strong>
                  {UserService.userRole(user, `${config.permissions.DATAFLOW}${dataflowId}`)} functionality:
                </strong>
                {hasWritePermissions ? 'read / write' : 'read'}
              </li>
              <li>
                <strong>{UserService.userRole(user, `${config.permissions.DATAFLOW}${dataflowId}`)} type:</strong>
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
          header={resources.messages['updateDataflow']}
          onHide={() => onManageDialogs('isEditDialogVisible', false)}
          visible={dataflowDataState.isEditDialogVisible}>
          <DataflowManagementForm
            dataflowData={dataflowDataState}
            isEditForm={true}
            onCancel={() => onManageDialogs('isEditDialogVisible', false)}
            onEdit={onEditDataflow}
            refresh={dataflowDataState.isEditDialogVisible}
          />
        </Dialog>

        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['delete'].toUpperCase()}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          disabledConfirm={dataflowDataState.deleteInput.toLowerCase() !== dataflowDataState.name.toLowerCase()}
          onConfirm={() => onDeleteDataflow()}
          onHide={() => onManageDialogs('isDeleteDialogVisible', false, 'isPropertiesDialogVisible', true)}
          visible={dataflowDataState.isDeleteDialogVisible}>
          <p>{resources.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
                dataflowName: dataflowDataState.name
              })
            }}></p>
          <p>
            <InputText
              autoFocus={true}
              className={`${styles.inputText}`}
              onChange={event => onConfirmDelete(event)}
              ref={deleteInputRef}
              value={dataflowDataState.deleteInput}
            />
          </p>
        </ConfirmDialog>
      </div>
    </div>
  );
});

export { Dataflow };
