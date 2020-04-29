import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import styles from './Representative.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { ApiKeyDialog } from 'ui/views/_components/ApiKeyDialog';
import { BigButtonList } from './_components/BigButtonList';
import { MainLayout } from 'ui/views/_components/Layout';
import { PropertiesDialog } from 'ui/views/Dataflow/_components/PropertiesDialog';
import { SnapshotsDialog } from './_components/SnapshotsDialog';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from '../_components/Title/Title';

import { DataflowService } from 'core/services/Dataflow';
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { representativeReducer } from './_functions/representativeReducer';
import { dataflowActionCreators } from './_functions/dataflowActionCreators';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const Representative = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, representative }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  // const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);

  const representativeInitialState = {
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
    isReceiptLoading: false,
    isReceiptOutdated: false
  };

  const [dataflowState, dataflowDispatch] = useReducer(representativeReducer, representativeInitialState);

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
    setUpdatedDatasetSchema
  } = dataflowActionCreators(dataflowDispatch);

  useEffect(() => {
    if (!isNil(user.contextRoles)) onLoadPermission();
  }, [user]);

  useEffect(() => {
    breadCrumbContext.add([
      {
        command: () => history.push(getUrl(routes.DATAFLOWS)),
        href: getUrl(routes.DATAFLOWS),
        icon: 'home',
        label: resources.messages['dataflows']
      },
      {
        command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
        href: getUrl(routes.DATAFLOWS),
        icon: 'archive',
        label: resources.messages['dataflow']
      },
      {
        icon: 'representative',
        label: representative || resources.messages['representative']
      }
    ]);

    const propertiesBtn = {
      icon: 'infoCircle',
      label: 'properties',
      onClick: () => manageDialogs('isPropertiesDialogVisible', true),
      title: 'properties'
    };

    const apiKeyBtn = {
      className: 'dataflow-properties-provider-help-step',
      icon: 'settings',
      label: 'sidebarApiKeyBtn',
      onClick: () => manageDialogs('isApiKeyDialogVisible', true),
      title: 'sidebarApiKeyBtn'
    };

    leftSideBarContext.addModels([dataflowState.isCustodian ? propertiesBtn : propertiesBtn, apiKeyBtn]);
  }, []);

  useEffect(() => {
    setIsPageLoading(true);
    onLoadReportingDataflow();
  }, [dataflowId, dataflowState.isDataUpdated]);

  useEffect(() => {
    const refresh = notificationContext.toShow.find(
      notification => notification.key === 'ADD_DATACOLLECTION_COMPLETED_EVENT'
    );
    if (refresh) {
      onUpdateData();
    }
  }, [notificationContext]);

  useEffect(() => {
    const response = notificationContext.toShow.find(
      notification => notification.key === 'RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'
    );
    if (response) {
      onLoadReportingDataflow();
    }
  }, [notificationContext]);

  const handleRedirect = target => history.push(target);

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

      if (!isEmpty(dataflow.datasets)) {
        const representativeId = dataflow.datasets
          .filter(dataset => dataset.datasetSchemaName === representative)
          .map(id => id.dataProviderId);
        if (representativeId.length === 1) {
          setDataProviderId(uniq(representativeId)[0]);
        }
      }

      if (!isEmpty(dataflow.representatives) && !isEmpty(dataflow.datasets)) {
        const representativeId = dataflow.datasets
          .filter(dataset => dataset.datasetSchemaName === representative)
          .map(id => id.dataProviderId);

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
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setIsPageLoading(false);
    }
  };

  const onShowReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToSnapshotProps(datasetId);
    manageDialogs('isSnapshotDialogVisible', true);
  };

  const onUpdateData = () => setIsDataUpdated(!dataflowState.isDataUpdated);

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
          title={!isNil(dataflowState.data) ? `${representative}` : null}
          subtitle={` ${TextUtils.ellipsis(dataflowState.name)}`}
          icon="representative"
          iconSize="4rem"
        />

        <BigButtonList
          dataflowState={dataflowState}
          dataflowDispatch={dataflowDispatch}
          dataflowData={dataflowState.data}
          dataflowId={dataflowId}
          dataProviderId={dataflowState.dataProviderId}
          handleRedirect={handleRedirect}
          hasWritePermissions={dataflowState.hasWritePermissions}
          isCustodian={dataflowState.isCustodian}
          representative={representative}
          onShowSnapshotDialog={onShowReleaseSnapshotDialog}
        />

        <SnapshotsDialog
          dataflowId={dataflowId}
          datasetId={dataflowState.datasetIdToSnapshotProps}
          isSnapshotDialogVisible={dataflowState.isSnapshotDialogVisible}
          manageDialogs={manageDialogs}
        />

        <PropertiesDialog
          dataflowState={dataflowState}
          dataflowId={dataflowId}
          history={history}
          manageDialogs={manageDialogs}
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

export { Representative };
