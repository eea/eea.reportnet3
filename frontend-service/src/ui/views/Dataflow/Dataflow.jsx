import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
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
import { useFilterHelpSteps } from './_functions/Hooks/useFilterHelpSteps';
import { useHelpSteps } from 'ui/views/Dataflow/_functions/Hooks/useHelpSteps';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const dataflowInitialState = {
    currentUrl: '',
    data: {},
    dataProviderId: [],
    datasetIdToSnapshotProps: undefined,
    deleteInput: '',
    description: '',
    designDatasetSchemas: [],
    formHasRepresentatives: false,
    hasRepresentativesWithoutDatasets: false,
    hasWritePermissions: false,
    id: dataflowId,
    isApiKeyDialogVisible: false,
    isCustodian: false,
    isDataSchemaCorrect: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isEditDialogVisible: false,
    isManageRolesDialogVisible: false,
    isPageLoading: true,
    isPropertiesDialogVisible: false,
    isReceiptLoading: false,
    isReceiptOutdated: false,
    isRepresentativeView: false,
    isSnapshotDialogVisible: false,
    name: '',
    obligations: {},
    status: '',
    updatedDatasetSchema: undefined
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  useEffect(() => {
    if (!isNil(user.contextRoles)) onLoadPermission();
  }, [user]);

  useHelpSteps(useFilterHelpSteps, leftSideBarContext, dataflowState);

  //Bread Crumbs settings
  useEffect(() => {
    if (!isEmpty(dataflowState.data)) {
      let representatives = dataflowState.data.datasets.map(dataset => {
        return { name: dataset.datasetSchemaName, dataProviderId: dataset.dataProviderId };
      });

      if (representatives.length === 1) {
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
      } else if (representatives.length > 1 && isUndefined(representativeId)) {
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
      } else if (representativeId) {
        const currentRepresentative = representatives
          .filter(representative => representative.dataProviderId === parseInt(representativeId))
          .map(representative => representative.name);

        breadCrumbContext.add([
          {
            label: resources.messages['dataflows'],
            icon: 'home',
            href: getUrl(routes.DATAFLOWS),
            command: () => history.push(getUrl(routes.DATAFLOWS))
          },
          {
            label: resources.messages['dataflow'],
            icon: 'archive',
            href: getUrl(routes.DATAFLOW),
            command: () => history.goBack()
          },
          {
            label: currentRepresentative[0],
            icon: 'archive'
          }
        ]);
      } else if (dataflowState.status === 'DESIGN') {
        breadCrumbContext.add([
          {
            label: resources.messages['dataflows'],
            icon: 'home',
            href: getUrl(routes.DATAFLOWS),
            command: () => history.goBack()
          },
          {
            label: resources.messages['dataflow'],
            icon: 'archive'
          }
        ]);
      }
    }
  }, [match.params, dataflowState.data]);

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

    if (!isEmpty(dataflowState.data)) {
      if (dataflowState.isCustodian && dataflowState.status === DataflowConf.dataflowStatus['DESIGN']) {
        leftSideBarContext.addModels([propertiesBtn, editBtn, manageRolesBtn]);
      } else if (dataflowState.isCustodian && dataflowState.status === DataflowConf.dataflowStatus['DRAFT']) {
        leftSideBarContext.addModels([propertiesBtn, manageRolesBtn]);
      } else {
        if (!dataflowState.isCustodian) {
          dataflowState.data.representatives.length === 1 && isUndefined(representativeId)
            ? leftSideBarContext.addModels([propertiesBtn, apiKeyBtn])
            : dataflowState.data.representatives.length > 1 && isUndefined(representativeId)
            ? leftSideBarContext.addModels([propertiesBtn])
            : leftSideBarContext.addModels([propertiesBtn, apiKeyBtn]);
        } else {
          leftSideBarContext.addModels([propertiesBtn]);
        }
      }
    }
  }, [dataflowState.isCustodian, dataflowState.status, representativeId]);

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

  const handleRedirect = target => history.push(target);

  const manageRoleDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isManageRolesDialogVisible', false)}
    />
  );

  const initialLoad = (dataflow, isRepresentativeView) =>
    dataflowDispatch({
      type: 'INITIAL_LOAD',
      payload: {
        data: dataflow,
        description: dataflow.description,
        isRepresentativeView: isRepresentativeView,
        name: dataflow.name,
        obligations: dataflow.obligation,
        status: dataflow.status
      }
    });

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const onConfirmDeleteDataflow = event =>
    dataflowDispatch({ type: 'ON_CONFIRM_DELETE_DATAFLOW', payload: { deleteInput: event.target.value } });

  const setFormHasRepresentatives = value =>
    dataflowDispatch({ type: 'SET_FORM_HAS_REPRESENTATIVES', payload: { formHasRepresentatives: value } });

  const setHasRepresentativesWithoutDatasets = value =>
    dataflowDispatch({
      type: 'SET_HAS_REPRESENTATIVES_WITHOUT_DATASETS',
      payload: { hasRepresentativesWithoutDatasets: value }
    });

  const setIsDataUpdated = () => dataflowDispatch({ type: 'SET_IS_DATA_UPDATED' });

  const setIsPageLoading = isPageLoading =>
    dataflowDispatch({ type: 'SET_IS_PAGE_LOADING', payload: { isPageLoading } });

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  const setIsReceiptLoading = isReceiptLoading => {
    dataflowDispatch({
      type: 'SET_IS_RECEIPT_LOADING',
      payload: { isReceiptLoading }
    });
  };

  const setIsReceiptOutdated = isReceiptOutdated => {
    dataflowDispatch({
      type: 'SET_IS_RECEIPT_OUTDATED',
      payload: { isReceiptOutdated }
    });
  };

  const onCleanUpReceipt = () => {
    dataflowDispatch({
      type: 'ON_CLEAN_UP_RECEIPT',
      payload: { isReceiptLoading: false, isReceiptOutdated: false }
    });
  };

  const onEditDataflow = (newName, newDescription) => {
    dataflowDispatch({
      type: 'ON_EDIT_DATA',
      payload: { name: newName, description: newDescription, isEditDialogVisible: false }
    });
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

    dataflowDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian } });
  };

  const checkIsRepresentativeView = (datasets, dataflow) => {
    const uniqRepresentatives = uniq(datasets.map(dataset => dataset.dataProviderId));

    return dataflow.representatives.length === 1 && uniqRepresentatives === 1;
  };

  const onInitialLoad = (dataflow, datasets) => {
    const isRepresentativeView = checkIsRepresentativeView(datasets, dataflow);
    initialLoad(dataflow, isRepresentativeView);
  };

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);

      const { datasets } = dataflow;

      onInitialLoad(dataflow, datasets);

      if (!isEmpty(dataflow.designDatasets)) {
        dataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });

        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: dataflow.designDatasets } });

        const datasetSchemaInfo = [];
        dataflow.designDatasets.map(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });

        setUpdatedDatasetSchema(datasetSchemaInfo);
      }

      if (!isEmpty(dataflow.datasets)) {
        const dataProviderIds = dataflow.datasets.map(dataset => dataset.dataProviderId);
        if (uniq(dataProviderIds).length === 1) {
          dataflowDispatch({ type: 'SET_DATA_PROVIDER_ID', payload: { id: dataProviderIds[0] } });
        }
      }

      if (representativeId) {
        if (!isEmpty(dataflow.representatives) && !isEmpty(dataflow.datasets)) {
          const isReceiptOutdated = dataflow.representatives
            .filter(representative => representative.dataProviderId === parseInt(representativeId))
            .map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1) {
            setIsReceiptOutdated(isReceiptOutdated[0]);
          }
        }
      } else {
        if (!isEmpty(dataflow.representatives)) {
          const isReceiptOutdated = dataflow.representatives.map(representative => representative.isReceiptOutdated);

          if (isReceiptOutdated.length === 1) {
            setIsReceiptOutdated(isReceiptOutdated[0]);
          }
        }
      }
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

    dataflowDispatch({ type: 'SET_IS_DATA_SCHEMA_CORRECT', payload: { validationResult } });
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
    dataflowDispatch({ type: 'SET_DATASET_ID_TO_SNAPSHOT_PROPS', payload: { id: datasetId } });
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
          icon="clone"
          iconSize="4rem"
          subtitle={resources.messages['dataflow']}
          title={TextUtils.ellipsis(dataflowState.name)}
        />

        {!dataflowState.isRepresentativeView && isNil(representativeId) ? (
          <BigButtonList
            dataflowState={dataflowState}
            handleRedirect={handleRedirect}
            onCleanUpReceipt={onCleanUpReceipt}
            onSaveName={onSaveName}
            onShowSnapshotDialog={onShowSnapshotDialog}
            onUpdateData={setIsDataUpdated}
            setIsReceiptLoading={setIsReceiptLoading}
            setUpdatedDatasetSchema={setUpdatedDatasetSchema}
          />
        ) : (
          <BigButtonListRepresentative
            dataflowState={dataflowState}
            handleRedirect={handleRedirect}
            match={match}
            onCleanUpReceipt={onCleanUpReceipt}
            onShowSnapshotDialog={onShowSnapshotDialog}
            setIsReceiptLoading={setIsReceiptLoading}
          />
        )}

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

        <PropertiesDialog dataflowState={dataflowState} manageDialogs={manageDialogs} />

        <DataflowManagement
          dataflowId={dataflowId}
          history={history}
          isEditForm={true}
          manageDialogs={manageDialogs}
          onConfirmDeleteDataflow={onConfirmDeleteDataflow}
          onEditDataflow={onEditDataflow}
          state={dataflowState}
        />

        {dataflowState.isApiKeyDialogVisible && (
          <ApiKeyDialog
            dataflowId={dataflowId}
            dataProviderId={dataflowState.dataProviderId}
            isApiKeyDialogVisible={dataflowState.isApiKeyDialogVisible}
            manageDialogs={manageDialogs}
            match={match}
          />
        )}
      </div>
    </div>
  );
});

export { Dataflow };
