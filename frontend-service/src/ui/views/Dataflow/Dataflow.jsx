import React, { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import styles from './Dataflow.module.scss';

import { config } from 'conf';
import { DataflowHelpConfig } from 'conf/help/dataflow';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { BigButtonListRepresentative } from './_components/BigButtonListRepresentative';
import { Button } from 'ui/views/_components/Button';
import { DataflowManagement } from 'ui/views/_components/DataflowManagement';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
import { ManageRights } from './_components/ManageRights';
import { PropertiesDialog } from './_components/PropertiesDialog';
import { RepresentativesList } from './_components/RepresentativesList';
import { ShareRights } from './_components/ShareRights';
import { SnapshotsDialog } from './_components/SnapshotsDialog';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from '../_components/Title/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { dataflowDataReducer } from './_functions/Reducers/dataflowDataReducer';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const Dataflow = withRouter(({ history, match }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

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
    isManageRightsDialogVisible: false,
    isManageRolesDialogVisible: false,
    isPageLoading: true,
    isPropertiesDialogVisible: false,
    isReceiptLoading: false,
    isReceiptOutdated: false,
    isRepresentativeView: false,
    isShareRightsDialogVisible: false,
    isSnapshotDialogVisible: false,
    name: '',
    obligations: {},
    status: '',
    updatedDatasetSchema: undefined,
    userRoles: []
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowDataReducer, dataflowInitialState);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) onLoadPermission();
  }, [userContext, dataflowState.data]);

  useEffect(() => {
    leftSideBarContext.addHelpSteps(DataflowHelpConfig, 'dataflowHelp');
  }, [
    dataflowState.data,
    dataflowState.designDatasetSchemas,
    dataflowState.formHasRepresentatives,
    dataflowState.isCustodian,
    dataflowState.isDataSchemaCorrect,
    dataflowState.status,
    dataflowState.id
  ]);

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
            icon: 'clone'
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
            icon: 'clone'
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
            icon: 'clone',
            href: getUrl(routes.DATAFLOW),
            command: () => history.goBack()
          },
          {
            label: currentRepresentative[0],
            icon: 'clone'
          }
        ]);
      } else if (dataflowState.status === 'DESIGN') {
        breadCrumbContext.add([
          {
            label: resources.messages['dataflows'],
            icon: 'home',
            href: getUrl(routes.DATAFLOWS),
            command: () => history.push(getUrl(routes.DATAFLOWS))
          },
          {
            label: resources.messages['dataflow'],
            icon: 'clone'
          }
        ]);
      }
    }
  }, [match.params, dataflowState.data]);

  useEffect(() => {
    if (!isEmpty(dataflowState.userRoles)) {
      const buttonsVisibility = getLeftSidebarButtonsVisibility();

      const apiKeyBtn = {
        className: 'dataflow-properties-provider-help-step',
        icon: 'settings',
        isVisible: buttonsVisibility.apiKeyBtn,
        label: 'sidebarApiKeyBtn',
        onClick: () => manageDialogs('isApiKeyDialogVisible', true),
        title: 'sidebarApiKeyBtn'
      };

      const editBtn = {
        className: 'dataflow-edit-help-step',
        icon: 'edit',
        isVisible: buttonsVisibility.editBtn,
        label: 'edit',
        onClick: () => manageDialogs('isEditDialogVisible', true),
        title: 'edit'
      };

      const manageRightsBtn = {
        className: 'dataflow-properties-provider-help-step',
        icon: 'userConfig',
        isVisible: buttonsVisibility.manageRightsBtn,
        label: dataflowState.isCustodian ? 'manageEditorsRights' : 'manageReportersRights',
        onClick: () => manageDialogs('isShareRightsDialogVisible', true),
        title: dataflowState.isCustodian ? 'manageEditorsRights' : 'manageReportersRights'
      };

      const propertiesBtn = {
        className: 'dataflow-properties-provider-help-step',
        icon: 'infoCircle',
        isVisible: buttonsVisibility.propertiesBtn,
        label: 'properties',
        onClick: () => manageDialogs('isPropertiesDialogVisible', true),
        title: 'properties'
      };

      const allButtons = [propertiesBtn, editBtn, apiKeyBtn, manageRightsBtn];

      leftSideBarContext.addModels(allButtons.filter(button => button.isVisible));
    }
  }, [dataflowState.userRoles, dataflowState.status, representativeId, dataflowState.datasetId]);

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
  }, [dataflowId, dataflowState.isDataUpdated, representativeId]);

  const getLeftSidebarButtonsVisibility = () => {
    const { userRoles } = dataflowState;

    const isDesign = dataflowState.status === DataflowConf.dataflowStatus['DESIGN'];
    const isDraft = dataflowState.status === DataflowConf.dataflowStatus['DRAFT'];

    if (isEmpty(dataflowState.data)) {
      return { apiKeyBtn: false, editBtn: false, manageRightsBtn: false, propertiesBtn: false };
    }

    const isRepresentative =
      dataflowState.data.representatives.length === 1 && isUndefined(representativeId)
        ? true
        : dataflowState.data.representatives.length > 1 && isUndefined(representativeId)
        ? false
        : true;

    return {
      apiKeyBtn: isRepresentative,

      editBtn:
        userRoles.includes(config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD']) && isDesign,

      manageRightsBtn:
        (isDesign && userRoles.includes(config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD'])) ||
        (isDraft && isRepresentative && userRoles.includes(config.permissions['LEAD_REPORTER'])),

      propertiesBtn: true
    };
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
  const manageRightsDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon={'cancel'}
      label={resources.messages['close']}
      onClick={() => manageDialogs('isShareRightsDialogVisible', false)}
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

  const getCurrentDatasetId = () => {
    if (isEmpty(dataflowState.data)) return null;

    const { datasets } = dataflowState.data;

    return first(
      datasets
        .filter(dataset => dataset.dataProviderId === parseInt(representativeId))
        .map(dataset => dataset.datasetId)
    );
  };

  const onLoadPermission = () => {
    const currentDatasetId = getCurrentDatasetId();

    const hasWritePermissions = userContext.hasPermission(
      [config.permissions.LEAD_REPORTER],
      `${config.permissions.DATAFLOW}${dataflowId}`
    );

    const entity = isNil(representativeId)
      ? `${config.permissions['DATAFLOW']}${dataflowId}`
      : `${config.permissions['DATASET']}${currentDatasetId}`;

    const userRoles = userContext.getUserRole(entity);

    const isCustodian = userRoles.includes(config.permissions['DATA_CUSTODIAN'] || config.permissions['DATA_STEWARD']);

    dataflowDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian, userRoles } });
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

  const onShowManageReportersDialog = () => manageDialogs('isManageRolesDialogVisible', true);

  const onShowSnapshotDialog = async datasetId => {
    dataflowDispatch({ type: 'SET_DATASET_ID_TO_SNAPSHOT_PROPS', payload: { id: datasetId } });
    manageDialogs('isSnapshotDialogVisible', true);
  };

  useCheckNotifications(
    ['ADD_DATACOLLECTION_COMPLETED_EVENT', 'COPY_DATASET_SCHEMA_COMPLETED_EVENT'],
    setIsDataUpdated
  );

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
            onShowManageReportersDialog={onShowManageReportersDialog}
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

        <Dialog
          contentStyle={{ maxHeight: '60vh' }}
          footer={manageRightsDialogFooter}
          header={
            dataflowState.isCustodian
              ? resources.messages['manageEditorsRights']
              : resources.messages['manageReportersRights']
          }
          onHide={() => manageDialogs('isManageRightsDialogVisible', false)}
          visible={dataflowState.isManageRightsDialogVisible}>
          <div className={styles.dialog}>
            <ManageRights
              dataflowId={dataflowId}
              dataflowState={dataflowState}
              dataProviderId={dataflowState.dataProviderId}
              isActiveManageRightsDialog={dataflowState.isManageRightsDialogVisible}
            />
          </div>
        </Dialog>

        <Dialog
          header={
            dataflowState.isCustodian
              ? resources.messages['manageEditorsRights']
              : resources.messages['manageReportersRights']
          }
          footer={manageRightsDialogFooter}
          onHide={() => manageDialogs('isShareRightsDialogVisible', false)}
          visible={dataflowState.isShareRightsDialogVisible}>
          <ShareRights dataflowId={dataflowId} dataflowState={dataflowState} representativeId={representativeId} />
        </Dialog>

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
            isCustodian={dataflowState.isCustodian}
            manageDialogs={manageDialogs}
            match={match}
          />
        )}
      </div>
    </div>
  );
});

export { Dataflow };
