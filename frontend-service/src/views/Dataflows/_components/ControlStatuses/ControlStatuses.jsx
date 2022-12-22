import { Fragment, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Column } from 'primereact/column';

import styles from './ControlStatuses.module.scss';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DatasetsForm } from 'views/_components/DatasetsForm';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { ControlStatusesService } from 'services/ControlStatusesService';

import { getUrl } from 'repositories/_utils/UrlUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from '../../../_functions/Contexts/UserContext';

const { permissions } = config;

export const ControlStatuses = ({ onCloseDialog, isDialogVisible }) => {
  const navigate = useNavigate();

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const isAdmin = userContext.hasPermission([permissions.roles.ADMIN.key]);

  const [controlStatus, setControlStatus] = useState(null);
  const [dataflowId, setDataflowId] = useState(null);
  const [dataProviderId, setDataProviderId] = useState(null);
  const [datasetForDataDeletion, setDatasetForDataDeletion] = useState([]);
  const [datasetId, setDatasetId] = useState(null);
  const [datasetName, setDatasetName] = useState('');
  const [isDatasetDataDeleteSuccessfull, setIsDatasetDataDeleteSuccessfull] = useState(false);
  const [isDeleteDatasetDataResultDialogVisible, setIsDeleteDatasetDataResultDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isDeletingDatasetData, setIsDeletingDatasetData] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isParametersWrongDialogVisible, setIsParametersWrongDialogVisible] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');

  const getDatasetData = async (forwardedDatasetId, forwardedProviderId) => {
    setLoadingStatus('pending');

    try {
      const data = await ControlStatusesService.getDatasetData(forwardedDatasetId, forwardedProviderId);

      setDatasetForDataDeletion([...[], data]);
      setDatasetId(data.datasetId);
      setDatasetName(data.datasetName);
      setDataProviderId(data.dataProviderId);
      setDataflowId(data.dataflowId);

      console.log('data.datasetId: ' + data.datasetId);
      console.log('data.datasetName: ' + data.datasetName);
      console.log('data.dataProviderId: ' + data.dataProviderId);
      console.log('data.dataflowId: ' + data.dataflowId);

      if (
        data.datasetId === null ||
        data.dataProviderId === null ||
        data.datasetId === undefined ||
        data.dataProviderId === undefined
      ) {
        setIsParametersWrongDialogVisible(true);
      }

      setLoadingStatus('success');
    } catch (error) {
      console.error('ControlStatus - getDatasetData.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_CONTROL_STATUSES_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getTableColumns = () => {
    const columns = [
      {
        key: 'dataProviderId',
        header: resourcesContext.messages['providerId'],
        template: getProviderIdTemplate,
        className: styles.middleColumn
      },
      {
        key: 'dataflowName',
        header: resourcesContext.messages['dataflowName'],
        template: getDataflowNameTemplate,
        className: styles.middleColumn
      },
      {
        key: 'datasetId',
        header: resourcesContext.messages['datasetId'],
        template: getDatasetIdTemplate,
        className: styles.middleColumn
      },
      {
        key: 'datasetName',
        header: resourcesContext.messages['datasetName'],
        template: getDatasetNameTemplate,
        className: styles.middleColumn
      }
    ];

    if (isAdmin) {
      columns.push({
        key: 'buttonsUniqueId',
        header: resourcesContext.messages['actions'],
        template: getDeleteButton,
        className: styles.smallColumn
      });
    }

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
      />
    ));
  };

  const getDeleteButton = rowData => (
    <ActionsColumn
      isDeletingDatasetData={isDeletingDatasetData}
      onDeleteClick={() => {
        setIsDeleteDialogVisible(true);
        setControlStatus(rowData);
      }}
      rowDataId={rowData.datasetId}
    />
  );

  const getProviderIdTemplate = datasetData => <p>{datasetData.dataProviderId}</p>;

  const getDataflowNameTemplate = datasetData => <p>{datasetData.dataflowName}</p>;

  const getDatasetIdTemplate = datasetData => <p>{datasetData.datasetId}</p>;

  const getDatasetNameTemplate = datasetData => <p>{datasetData.datasetName}</p>;

  const onConfirmDeleteDialog = async () => {
    setLoadingStatus('pending');
    setIsDeleteDialogVisible(false);

    try {
      setIsDeletingDatasetData(true);
      const isDeleteSuccessfull = await ControlStatusesService.deleteDatasetData(datasetId);

      if (isDeleteSuccessfull) {
        setIsDatasetDataDeleteSuccessfull(true);
        setIsDeleteDatasetDataResultDialogVisible(true);
      } else {
        setIsDatasetDataDeleteSuccessfull(false);
        setIsDeleteDatasetDataResultDialogVisible(true);
      }

      setLoadingStatus('success');
      console.log('Is delete successfull: ' + isDeleteSuccessfull);
    } catch (error) {
      console.error('ControlStatus - onConfirmDeleteDialog.', error);
      setLoadingStatus('failed');

      notificationContext.add({ status: 'DELETE_PROVIDER_DATASET_DATA_ERROR' }, true);
    } finally {
      setIsDeletingDatasetData(false);
      setControlStatus(null);
    }
  };

  const onHideDeleteDialog = () => {
    setIsDeleteDialogVisible(false);
    setControlStatus(null);
  };

  const onHideIsParametersWrongDialog = () => {
    setIsParametersWrongDialogVisible(false);
    setControlStatus(null);
    setDatasetForDataDeletion(null);
  };

  const onSearchAgain = () => {
    setIsDeleteDatasetDataResultDialogVisible(false);
    setControlStatus(null);
    setDatasetForDataDeletion(null);
  };

  const dialogFooter = (
    <div className={styles.footer}>
      {!(
        isEmpty(datasetForDataDeletion) ||
        datasetId === null ||
        dataProviderId === null ||
        datasetId === undefined ||
        dataProviderId === undefined
      ) && (
        <Button
          className={`p-button-secondary ${styles.buttonPushLeft}`}
          icon="arrowRight"
          label={resourcesContext.messages['back']}
          onClick={onSearchAgain}
        />
      )}
      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  const renderSearchAgainButton = () => (
    <Button
      className="p-button-secondary"
      icon="search"
      label={resourcesContext.messages['searchAgain']}
      onClick={onSearchAgain}
    />
  );

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (
      isEmpty(datasetForDataDeletion) ||
      datasetId === null ||
      dataProviderId === null ||
      datasetId === undefined ||
      dataProviderId === undefined
    ) {
      return <DatasetsForm getDatasetData={getDatasetData} />;
    }

    return (
      <div className={styles.dialogContent}>
        <div>{renderSearchAgainButton()}</div>
        <DataTable
          autoLayout={true}
          className={styles.controlStatusesTable}
          hasDefaultCurrentPage={true}
          lazy={true}
          loading={loadingStatus === 'pending' && isNil(controlStatus)}
          value={datasetForDataDeletion}>
          {getTableColumns()}
        </DataTable>
      </div>
    );
  };

  return (
    <Fragment>
      <Dialog
        blockScroll={false}
        className="responsiveBigDialog"
        footer={dialogFooter}
        header={resourcesContext.messages['controlStatus']}
        modal={true}
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDatasetDataResultDialogVisible && (
        <ConfirmDialog
          header={
            isDatasetDataDeleteSuccessfull
              ? resourcesContext.messages['datasetDataDeletedSuccessfullyHeader']
              : resourcesContext.messages['datasetDataFailedToDeleteHeader']
          }
          iconConfirm="search"
          labelCancel={resourcesContext.messages['close']}
          labelConfirm={resourcesContext.messages['searchAgain']}
          onConfirm={onSearchAgain}
          onHide={onCloseDialog}
          visible={isDeleteDatasetDataResultDialogVisible}>
          {isDatasetDataDeleteSuccessfull ? (
            <p>
              <a
                href=""
                onClick={() => {
                  navigate(getUrl(routes.DATASET, { dataflowId, datasetId }, true));
                }}>
                {datasetName} - {datasetId}
              </a>
              {resourcesContext.messages['datasetDataDeletedSuccessfullyContent']}
            </p>
          ) : (
            <p>
              <a
                href=""
                onClick={() => {
                  navigate(getUrl(routes.DATASET, { dataflowId, datasetId }, true));
                }}>
                {datasetName} - {datasetId}
              </a>
              {resourcesContext.messages['datasetDataFailedToDeleteContent']}
            </p>
          )}
        </ConfirmDialog>
      )}

      {isParametersWrongDialogVisible && (
        <Dialog
          className="p-button-danger"
          header={resourcesContext.messages['falseParameters']}
          onHide={onHideIsParametersWrongDialog}
          visible={isParametersWrongDialogVisible}>
          {resourcesContext.messages['noDatasetsWithSelectedParameters']}
        </Dialog>
      )}

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          header={resourcesContext.messages['providerDatasetDataRemoveDialogHeader']}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteDialog}
          onHide={onHideDeleteDialog}
          visible={isDeleteDialogVisible}>
          {
            <p>
              {resourcesContext.messages['providerDatasetDataRemoveDialogContent']}
              <a
                href=""
                onClick={() => {
                  navigate(getUrl(routes.DATASET, { dataflowId, datasetId }, true));
                }}>
                {datasetName} - {datasetId}
              </a>
              {resourcesContext.messages['?']}
            </p>
          }
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
