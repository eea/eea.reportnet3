import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import styles from './Representative.module.scss';

import colors from 'conf/colors.json';
import { config } from 'conf';
import DataflowConf from 'conf/dataflow.config.json';
import { routes } from 'ui/routes';

import { BigButtonList } from './_components/BigButtonList';
import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { MainLayout } from 'ui/views/_components/Layout';
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

import { dataflowReducer } from 'ui/views/_components/DataflowManagementForm/_functions/Reducers';
import { receiptReducer } from 'ui/views/_functions/Reducers/receiptReducer';

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

  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [dataProviderId, setDataProviderId] = useState([]);
  const [datasetIdToSnapshotProps, setDatasetIdToSnapshotProps] = useState();
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isActivePropertiesDialog, setIsActivePropertiesDialog] = useState(false);
  const [isActiveReleaseSnapshotDialog, setIsActiveReleaseSnapshotDialog] = useState(false);
  const [isCustodian, setIsCustodian] = useState(false);
  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [loading, setLoading] = useState(true);

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, {});
  const [receiptState, receiptDispatch] = useReducer(receiptReducer, {});

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
        icon: 'archive',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true))
      },
      {
        label: representative || resources.messages['representative'],
        icon: 'representative'
      }
    ]);
  }, []);

  useEffect(() => {
    if (isCustodian && dataflowStatus === DataflowConf.dataflowStatus['DESIGN']) {
      leftSideBarContext.addModels([
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
  }, [dataflowId, isDataUpdated]);

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

  const handleRedirect = target => {
    history.push(target);
  };

  if (isDeleteDialogVisible && document.getElementsByClassName('p-inputtext p-component').length > 0) {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }

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
      const dataflow = await DataflowService.reporting(dataflowId);
      setDataflowData(dataflow);
      setDataflowStatus(dataflow.status);

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
      setLoading(false);
    }
  };

  const onShowDeleteDataflowDialog = () => {
    setIsActivePropertiesDialog(false);
    setIsDeleteDialogVisible(true);
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
          title={!isUndefined(dataflowState[dataflowId]) ? `${representative}` : null}
          subtitle={` ${TextUtils.ellipsis(dataflowState[dataflowId].name)}`}
          icon="representative"
          iconSize="4rem"
        />

        <BigButtonList
          dataflowData={dataflowData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          handleRedirect={handleRedirect}
          hasWritePermissions={hasWritePermissions}
          isCustodian={isCustodian}
          receiptDispatch={receiptDispatch}
          receiptState={receiptState}
          representative={representative}
          showReleaseSnapshotDialog={onShowReleaseSnapshotDialog}
        />

        <SnapshotsDialog
          dataflowData={dataflowData}
          dataflowId={dataflowId}
          datasetId={datasetIdToSnapshotProps}
          hideSnapshotDialog={onHideSnapshotDialog}
          isSnapshotDialogVisible={isActiveReleaseSnapshotDialog}
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
            {!isUndefined(dataflowState[dataflowId]) && dataflowState[dataflowId].description}
          </div>
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
      </div>
    </div>
  );
});

export { Representative };
