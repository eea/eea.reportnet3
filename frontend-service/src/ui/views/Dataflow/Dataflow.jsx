import React, { useContext, useEffect, useReducer } from 'react';
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
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
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

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { dataflowActionCreators } from './_functions/dataflowActionCreators';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

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
    dataProviderId: [],
    datasetIdToSnapshotProps: undefined,
    designDatasetSchemas: [],
    isDataSchemaCorrect: [],
    isDataUpdated: false,
    isPageLoading: true,
    updatedDatasetSchema: undefined,
    isSnapshotDialogVisible: false,
    currentUrl: '',
    isReceiptLoading: false,
    isReceiptOutdated: false,
    urlRepresentativeId: null
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  const {
    initialLoad,
    loadPermissions,
    manageDialogs,
    onDeleteDataflow,
    onEditData,
    setDataProviderId,
    setDatasetIdToSnapshotProps,
    setDesignDatasetSchemas,
    setFormHasRepresentatives,
    setHasRepresentativesWithoutDatasets,
    setIsDataSchemaCorrect,
    setIsDataUpdated,
    setIsPageLoading,
    setUpdatedDatasetSchema,
    setUrlRepresentativeId
  } = dataflowActionCreators(dataflowDispatch);

  useEffect(() => {
    const currentUrl = window.location.pathname;

    if (currentUrl.includes('representativeId')) {
      if (dataflowState.urlRepresentativeId === null) {
        setUrlRepresentativeId(currentUrl.substr(currentUrl.indexOf('Id/') + 3));
      }
    }
  }, [dataflowState.urlRepresentativeId]);

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

    if (dataflowState.isCustodian && dataflowState.status === DataflowConf.dataflowStatus['DESIGN']) {
      leftSideBarContext.addModels([propertiesBtn, editBtn, manageRolesBtn]);
    } else if (dataflowState.isCustodian && dataflowState.status === DataflowConf.dataflowStatus['DRAFT']) {
      leftSideBarContext.addModels([propertiesBtn, manageRolesBtn]);
    } else {
      leftSideBarContext.addModels(dataflowState.isRepresentativeView ? [propertiesBtn, apiKeyBtn] : [propertiesBtn]);
    }
  }, [dataflowState.isCustodian, dataflowState.status]);

  useEffect(() => {
    const steps = filterHelpSteps();
    leftSideBarContext.addHelpSteps('dataflowHelp', steps);
  }, [
    dataflowState.data,
    dataflowState.designDatasetSchemas,
    dataflowState.formHasRepresentatives,
    dataflowState.isCustodian,
    dataflowState.isDataSchemaCorrect,
    dataflowState.status,
    dataflowId
  ]);

  useEffect(() => {
    if (!isEmpty(dataflowState.data.representatives)) {
      const representativesNoDatasets = dataflowState.data.representatives.filter(
        representative => !representative.hasDatasets
      );
      //set for the first load
      setHasRepresentativesWithoutDatasets(!isEmpty(representativesNoDatasets));
      setFormHasRepresentatives(!isEmpty(representativesNoDatasets));
    }
  }, [dataflowState.data.representatives]);

  useEffect(() => {
    setIsPageLoading(true);
    onLoadReportingDataflow();
    onLoadSchemasValidations();
  }, [dataflowId, dataflowState.isDataUpdated]);

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

  const onEditDataflow = (newName, newDescription) => {
    onEditData(newName, newDescription);
    onLoadReportingDataflow();
  };

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

    loadPermissions(hasWritePermissions, isCustodian);
  };

  const checkIsRepresentativeView = datasets => {
    const uniqRepresentatives = uniq(datasets.map(dataset => dataset.datasetSchemaName));

    return uniqRepresentatives.length === 1;
  };

  const onInitialLoad = (dataflow, datasets) => {
    const isRepresentativeView = checkIsRepresentativeView(datasets);
    initialLoad(dataflow, isRepresentativeView);
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);

      const { datasets } = dataflow;

      onInitialLoad(dataflow, datasets);

      //UPDATE NAMES DATASET SCHEMAS IN DESIGN VIEW
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
      ///////////

      if (!isEmpty(dataflow.datasets)) {
        const dataProviderIds = dataflow.datasets.map(dataset => dataset.dataProviderId);
        if (uniq(dataProviderIds).length === 1) {
          setDataProviderId(dataProviderIds[0]);
        }
      } //+

      if (dataflowState.isRepresentativeView) {
        if (!isEmpty(dataflow.representatives) && !isEmpty(dataflow.datasets)) {
          const representativeId = dataflow.datasets.map(id => id.dataProviderId);

          const isReleased = dataflow.datasets
            .filter(representative => representative.dataProviderId === uniq(representativeId)[0])
            .map(releasedStatus => releasedStatus.isReleased);

          const isReceiptOutdated = dataflow.representatives
            .filter(representative => representative.dataProviderId === uniq(representativeId)[0])
            .map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1 && isReleased.length === 1) {
            dataflowDispatch({
              type: 'ON_INIT_RECEIPT_DATA',
              payload: { isReceiptLoading: false, isReceiptOutdated: isReceiptOutdated[0], isReleased }
            });
          }
        }
      } else {
        if (!isEmpty(dataflow.representatives)) {
          const isReceiptOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);
          if (isReceiptOutdated.length === 1) {
            dataflowDispatch({ type: 'ON_INIT_RECEIPT_DATA', payload: { isReceiptOutdated: isReceiptOutdated[0] } });
          }
        }
      }

      /*  if (!isEmpty(dataflow.representatives)) {
        const isReceiptOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);
        if (isReceiptOutdated.length === 1) {
          dataflowDispatch({ type: 'ON_INIT_RECEIPT_DATA', payload: { isReceiptOutdated: isReceiptOutdated[0] } });
        }
      } */
    } catch (error) {
      notificationContext.add({ type: 'LOAD_DATAFLOW_DATA_ERROR' });
      if (error.response.status === 401 || error.response.status === 403 || error.response.status === 500) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsPageLoading(false);
    }
  };

  useCheckNotifications(['RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'], onLoadReportingDataflow);

  const onLoadSchemasValidations = async () => {
    const validationResult = await DataflowService.schemasValidation(dataflowId);
    setIsDataSchemaCorrect(validationResult);
  };

  const onSaveName = async (value, index) => {
    await DatasetService.updateSchemaNameById(
      dataflowState.designDatasetSchemas[index].datasetId,
      encodeURIComponent(value)
    );
    const updatedTitles = [...dataflowState.updatedDatasetSchema];
    updatedTitles[index].schemaName = value;
    setUpdatedDatasetSchema(updatedTitles);
  };

  const onShowSnapshotDialog = async datasetId => {
    setDatasetIdToSnapshotProps(datasetId);
    manageDialogs('isSnapshotDialogVisible', true);
  };

  useCheckNotifications(['ADD_DATACOLLECTION_COMPLETED_EVENT'], setIsDataUpdated);

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowState.isPageLoading || isNil(dataflowState.data)) return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          icon="archive"
          iconSize="4rem"
          subtitle={resources.messages['dataflow']}
          title={TextUtils.ellipsis(dataflowState.name)}
        />

        <BigButtonList
          dataflowDispatch={dataflowDispatch}
          dataflowState={dataflowState}
          handleRedirect={handleRedirect}
          onSaveName={onSaveName}
          onShowSnapshotDialog={onShowSnapshotDialog}
          onUpdateData={setIsDataUpdated}
          setUpdatedDatasetSchema={setUpdatedDatasetSchema}
        />

        <BigButtonListRepresentative
          dataflowDispatch={dataflowDispatch}
          dataflowState={dataflowState}
          handleRedirect={handleRedirect}
          onShowSnapshotDialog={onShowSnapshotDialog}
          representative={'Bulgaria'}
        />

        <SnapshotsDialog
          dataflowId={dataflowId}
          datasetId={dataflowState.datasetIdToSnapshotProps}
          isSnapshotDialogVisible={dataflowState.isSnapshotDialogVisible}
          manageDialogs={manageDialogs}
        />

        {dataflowState.isCustodian && (
          <Dialog
            contentStyle={{ maxHeight: '60vh' }}
            footer={manageRoleDialogFooter}
            header={resources.messages['manageRolesDialogTitle']}
            onHide={() => manageDialogs('isManageRolesDialogVisible', false)}
            visible={dataflowState.isManageRolesDialogVisible}>
            <div className={styles.dialog}>
              <RepresentativesList
                dataflowRepresentatives={dataflowState.data.representatives}
                dataflowId={dataflowId}
                isActiveManageRolesDialog={dataflowState.isManageRolesDialogVisible}
                setHasRepresentativesWithoutDatasets={setHasRepresentativesWithoutDatasets}
                setFormHasRepresentatives={setFormHasRepresentatives}
              />
            </div>
          </Dialog>
        )}

        <PropertiesDialog
          dataflowState={dataflowState}
          dataflowId={dataflowId}
          history={history}
          onDeleteDataflow={onDeleteDataflow}
          manageDialogs={manageDialogs}
        />

        <DataflowManagement
          dataflowId={dataflowId}
          isEditForm={true}
          onEditDataflow={onEditDataflow}
          manageDialogs={manageDialogs}
          state={dataflowState}
        />

        {dataflowState.isApiKeyDialogVisible && (
          <ApiKeyDialog
            dataflowId={dataflowId}
            dataProviderId={dataflowState.dataProviderId}
            isApiKeyDialogVisible={dataflowState.isApiKeyDialogVisible}
            manageDialogs={manageDialogs}
          />
        )}
      </div>
    </div>
  );
});

export { Dataflow };
