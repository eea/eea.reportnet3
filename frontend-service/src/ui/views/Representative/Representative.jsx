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

import { receiptReducer } from 'ui/views/_functions/Reducers/receiptReducer';
import { representativeReducer } from './_functions/representativeReducer';

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
    isSnapshotDialogVisible: false
  };

  const [receiptState, receiptDispatch] = useReducer(receiptReducer, {});
  const [representativeState, representativeDispatch] = useReducer(representativeReducer, representativeInitialState);

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

    leftSideBarContext.addModels([representativeState.isCustodian ? propertiesBtn : propertiesBtn, apiKeyBtn]);
  }, []);

  useEffect(() => {
    setIsPageLoading(true);
    onLoadReportingDataflow();
  }, [dataflowId, representativeState.isDataUpdated]);

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

  const onHideSnapshotDialog = () => manageDialogs('isSnapshotDialogVisible', false);

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

    representativeDispatch({ type: 'LOAD_PERMISSIONS', payload: { hasWritePermissions, isCustodian } });
  };

  const setIsPageLoading = isPageLoading =>
    representativeDispatch({ type: 'SET_IS_PAGE_LOADING', payload: { isPageLoading } });

  const setDataProviderId = id => representativeDispatch({ type: 'SET_DATA_PROVIDER_ID', payload: { id } });

  const setIsDataUpdated = () => representativeDispatch({ type: 'SET_IS_DATA_UPDATED' });

  const setDatasetIdToSnapshotProps = id =>
    representativeDispatch({ type: 'SET_DATASET_ID_TO_SNAPSHOT_PROPS', payload: { id } });

  const onLoadReportingDataflow = async () => {
    try {
      const dataflow = await DataflowService.reporting(dataflowId);
      representativeDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: dataflow,
          description: dataflow.description,
          name: dataflow.name,
          obligations: dataflow.obligation,
          status: dataflow.status
        }
      });

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

        const isOutdated = dataflow.representatives
          .filter(representative => representative.dataProviderId === uniq(representativeId)[0])
          .map(representative => representative.isReceiptOutdated);

        if (isOutdated.length === 1 && isReleased.length === 1) {
          receiptDispatch({
            type: 'INIT_DATA',
            payload: { isLoading: false, isOutdated: isOutdated[0], receiptPdf: {}, isReleased }
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

  const manageDialogs = (dialog, value, secondDialog, secondValue) =>
    representativeDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });

  const onShowReleaseSnapshotDialog = async datasetId => {
    setDatasetIdToSnapshotProps(datasetId);
    manageDialogs('isSnapshotDialogVisible', true);
  };

  const onUpdateData = () => setIsDataUpdated(!representativeState.isDataUpdated);

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: representativeState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (representativeState.isPageLoading || isNil(representativeState.data)) return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          title={!isNil(representativeState.data) ? `${representative}` : null}
          subtitle={` ${TextUtils.ellipsis(representativeState.name)}`}
          icon="representative"
          iconSize="4rem"
        />

        <BigButtonList
          dataflowData={representativeState.data}
          dataflowId={dataflowId}
          dataProviderId={representativeState.dataProviderId}
          handleRedirect={handleRedirect}
          hasWritePermissions={representativeState.hasWritePermissions}
          isCustodian={representativeState.isCustodian}
          receiptDispatch={receiptDispatch}
          receiptState={receiptState}
          representative={representative}
          onShowSnapshotDialog={onShowReleaseSnapshotDialog}
        />

        <SnapshotsDialog
          dataflowId={dataflowId}
          datasetId={representativeState.datasetIdToSnapshotProps}
          isSnapshotDialogVisible={representativeState.isSnapshotDialogVisible}
          manageDialogs={manageDialogs}
        />

        <PropertiesDialog
          dataflowState={representativeState}
          dataflowId={dataflowId}
          history={history}
          manageDialogs={manageDialogs}
        />

        {representativeState.isApiKeyDialogVisible && (
          <ApiKeyDialog
            dataflowId={dataflowId}
            dataProviderId={representativeState.dataProviderId}
            isApiKeyDialogVisible={representativeState.isApiKeyDialogVisible}
            manageDialogs={manageDialogs}
          />
        )}
      </div>
    </div>
  );
});

export { Representative };
