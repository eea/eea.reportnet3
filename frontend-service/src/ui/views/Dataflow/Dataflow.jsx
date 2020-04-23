import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import styles from './Dataflow.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { Button } from 'ui/views/_components/Button';
import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { RepresentativesList } from './_components/RepresentativesList';
import { SnapshotsDialog } from './_components/SnapshotsDialog';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from '../_components/Title/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
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

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  // const [dataProviderId, setDataProviderId] = useState([]);
  const [datasetIdToSnapshotProps, setDatasetIdToSnapshotProps] = useState();
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isDataSchemaCorrect, setIsDataSchemaCorrect] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();

  const dataflowInitialState = {
    data: {},
    deleteInput: '',
    description: '',
    formHasRepresentatives: false,
    hasRepresentativesWithoutDatasets: false,
    hasWritePermissions: false,
    id: dataflowId,
    isApiKeyDialogVisible: false,
    isCustodian: false,
    isDeleteDialogVisible: false,
    isEditDialogVisible: false,
    isManageRolesDialogVisible: false,
    isPropertiesDialogVisible: false,
    isRepresentativeView: false,
    name: '',
    obligations: {},
    status: '',

    dataProviderId: []
  };

  const [dataflowDataState, dataflowDataDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);
  const [receiptState, receiptDispatch] = useReducer(receiptReducer, {});

  useEffect(() => {
    if (!isNil(user.contextRoles)) onLoadPermission();
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
    const apiKeyBtn = {
      className: 'dataflow-properties-provider-help-step',
      icon: 'settings',
      label: 'sidebarApiKeyBtn',
      onClick: () => manageDialogs('isApiKeyDialogVisible', true),
      title: 'sidebarApiKeyBtn'
    };

    const editBtn = {
      className: 'dataflow-edit-help-step',
      icon: 'edit',
      label: 'edit',
      onClick: () => manageDialogs('isEditDialogVisible', true),
      title: 'edit'
    };

    const manageRolesBtn = {
      className: 'dataflow-manage-roles-help-step',
      icon: 'manageRoles',
      label: 'manageRoles',
      onClick: () => manageDialogs('isManageRolesDialogVisible', true),
      title: 'manageRoles'
    };

    const propertiesBtn = {
      className: 'dataflow-properties-provider-help-step',
      icon: 'infoCircle',
      label: 'properties',
      onClick: () => manageDialogs('isPropertiesDialogVisible', true),
      title: 'properties'
    };

    if (dataflowDataState.isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DESIGN']) {
      leftSideBarContext.addModels([propertiesBtn, editBtn, manageRolesBtn]);
    } else if (dataflowDataState.isCustodian && dataflowDataState.status === DataflowConf.dataflowStatus['DRAFT']) {
      leftSideBarContext.addModels([propertiesBtn, manageRolesBtn]);
    } else {
      leftSideBarContext.addModels(
        dataflowDataState.isRepresentativeView ? [propertiesBtn, apiKeyBtn] : [propertiesBtn]
      );
    }
  }, [dataflowDataState.isCustodian, dataflowDataState.status]);

  useEffect(() => {
    const steps = filterHelpSteps();
    leftSideBarContext.addHelpSteps('dataflowHelp', steps);
  }, [
    dataflowDataState.data,
    dataflowDataState.formHasRepresentatives,
    dataflowDataState.status,
    dataflowId,
    designDatasetSchemas,
    dataflowDataState.isCustodian,
    isDataSchemaCorrect
  ]);

  useEffect(() => {
    if (!isEmpty(dataflowDataState.data.representatives)) {
      const representativesNoDatasets = dataflowDataState.data.representatives.filter(
        representative => !representative.hasDatasets
      );
      //set for the first load
      setHasRepresentativesWithoutDatasets(!isEmpty(representativesNoDatasets));
      setFormHasRepresentatives(!isEmpty(representativesNoDatasets));
    }
  }, [dataflowDataState.data.representatives]);

  useEffect(() => {
    setLoading(true);
    onLoadReportingDataflow();
    onLoadSchemasValidations();
  }, [dataflowId, isDataUpdated]);

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
        target: '.dataflow-properties-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep11']}</h2>,
        target: '.dataflow-properties-provider-help-step'
      }
    ];

    const loadedClassesSteps = [...dataflowSteps].filter(
      dataflowStep =>
        !isNil(document.getElementsByClassName(dataflowStep.target.substring(1, dataflowStep.target.length))[0]) ||
        dataflowStep.target === 'body'
    );
    return loadedClassesSteps;
  };

  const handleRedirect = target => history.push(target);

  const manageRoleDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
    />
  );
  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDataDispatch({
      type: 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const onConfirmDelete = event =>
    dataflowDataDispatch({ type: 'ON_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const setFormHasRepresentatives = value =>
    dataflowDataDispatch({ type: 'SET_FORM_HAS_REPRESENTATIVES', payload: { formHasRepresentatives: value } });

  const onEditDataflow = (newName, newDescription) => {
    dataflowDataDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isVisible: false }
    });
    onLoadReportingDataflow();
  };

  const onHideSnapshotDialog = () => setIsActiveReleaseSnapshotDialog(false);

  const onLoadPermission = () => {
    const hasWritePermissions = UserService.hasPermission(
      user,
      [config.permissions.PROVIDER],
      `${config.permissions.DATAFLOW}${dataflowId}`
    );
    const isCustodian = UserService.hasPermission(
      user,
      [config.permissions.CUSTODIAN],
      `${config.permissions.DATAFLOW}${dataflowId}`
    );

    dataflowDataDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian } });
  };

  const setDataProviderId = id => {
    dataflowDataDispatch({ type: 'SET_DATA_PROVIDER_ID', payload: { id } });
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);

      const { datasets } = dataflow;
      const uniqRepresentatives = uniq(datasets.map(dataset => dataset.datasetSchemaName));

      dataflowDataDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: dataflow,
          description: dataflow.description,
          isRepresentativeView: uniqRepresentatives.length === 1,
          name: dataflow.name,
          obligations: dataflow.obligation,
          status: dataflow.status
        }
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
          receiptDispatch({ type: 'INIT_DATA', payload: { isLoading: false, isOutdated: isOutdated[0] } });
        }
      }
    } catch (error) {
      notificationContext.add({ type: 'LOAD_DATAFLOW_DATA_ERROR' });
      if (error.response.status === 401 || error.response.status === 403 || error.response.status === 500) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  useCheckNotifications(['RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'], onLoadReportingDataflow);

  const onLoadSchemasValidations = async () =>
    setIsDataSchemaCorrect(await DataflowService.schemasValidation(dataflowId));

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
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

  const onUpdateData = () => setIsDataUpdated(!isDataUpdated);

  useCheckNotifications(['ADD_DATACOLLECTION_COMPLETED_EVENT'], onUpdateData);

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowDataState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (loading || isNil(dataflowDataState.data)) return layout(<Spinner />);

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
          dataflowData={dataflowDataState.data}
          dataflowDataState={dataflowDataState}
          dataflowId={dataflowId}
          dataProviderId={dataflowDataState.dataProviderId}
          designDatasetSchemas={designDatasetSchemas}
          handleRedirect={handleRedirect}
          formHasRepresentatives={dataflowDataState.formHasRepresentatives}
          hasWritePermissions={dataflowDataState.hasWritePermissions}
          isCustodian={dataflowDataState.isCustodian}
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

        {dataflowDataState.isCustodian && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRoleDialogFooter}
            header={resources.messages['manageRolesDialogTitle']}
            onHide={() => manageDialogs('isManageRolesDialogVisible', false)}
            visible={dataflowDataState.isManageRolesDialogVisible}>
            <div className={styles.dialog}>
              <RepresentativesList
                dataflowRepresentatives={dataflowDataState.data.representatives}
                dataflowId={dataflowId}
                isActiveManageRolesDialog={dataflowDataState.isManageRolesDialogVisible}
                setHasRepresentativesWithoutDatasets={setHasRepresentativesWithoutDatasets}
                setFormHasRepresentatives={setFormHasRepresentatives}
              />
            </div>
          </Dialog>
        )}

        <PropertiesDialog
          dataflowDataState={dataflowDataState}
          dataflowId={dataflowId}
          history={history}
          onConfirmDelete={onConfirmDelete}
          manageDialogs={manageDialogs}
        />

        <DataflowManagement
          dataflowId={dataflowId}
          isEditForm={true}
          onEditDataflow={onEditDataflow}
          manageDialogs={manageDialogs}
          state={dataflowDataState}
        />

        {dataflowDataState.isApiKeyDialogVisible && (
          <ApiKeyDialog
            dataflowId={dataflowId}
            dataProviderId={dataflowDataState.dataProviderId}
            isApiKeyDialogVisible={dataflowDataState.isApiKeyDialogVisible}
            manageDialogs={manageDialogs}
          />
        )}
      </div>
    </div>
  );
});

export { Dataflow };
