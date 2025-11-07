import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import dayjs from 'dayjs';

import styles from './ImportedFilesDialog.module.scss';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { DatasetService } from 'services/DatasetService';
import { DownloadFile } from 'views/_components/DownloadFile';

export const ImportedFilesDialog = ({ datasetId, dataflowId, onCloseDialog, isDialogVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [filterValue, setFilterValue] = useState('');
  const [importedFilesList, setImportedFilesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [downloadingFileName, setDownloadingFileName] = useState(null);

  useEffect(() => {
    if (isDialogVisible) {
      getImportedFilesList();
    }
  }, [isDialogVisible]);

  const getImportedFilesList = async () => {
    setLoadingStatus('pending');
    setIsLoading(true);

    try {
      const response = await DatasetService.getImportedFiles(datasetId);

      const data = response?.data;

      setImportedFilesList(data);
      setLoadingStatus('success');
    } catch (error) {
      console.error('ImportedFiles - getImportedFilesList.', error);
      if (error.response?.status === 500 && error.response?.data?.message?.includes('No import directory found')) {
        setImportedFilesList([]);
        setLoadingStatus('success');
      } else {
        setLoadingStatus('error');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const onDownloadFile = async fileName => {
    setDownloadingFileName(fileName);

    try {
      const { data } = await DatasetService.downloadImportedFile(fileName, datasetId, dataflowId);

      if (data.size !== 0) {
        DownloadFile(data, fileName);
      }
    } catch (error) {
      console.error('ImportedFilesDialog - onDownloadFile.', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add(
          {
            type: 'DOWNLOAD_IMPORTED_FILE_ERROR_EVENT',
            content: { fileName, dataflowId, datasetId }
          },
          true
        );
      }
    } finally {
      setDownloadingFileName(null);
    }
  };

  const filterData = () => {
    if (!filterValue.trim()) {
      return;
    }

    const filteredList = importedFilesList.filter(file => {
      const searchValue = filterValue.toLowerCase();
      return file.fileName.toLowerCase().includes(searchValue);
    });

    setImportedFilesList(filteredList);
  };

  const resetFilter = () => {
    setFilterValue('');
    getImportedFilesList();
  };

  const getTableColumns = () => {
    const columns = [
      {
        key: 'fileName',
        header: resourcesContext.messages['fileName'],
        sortable: true
      },
      {
        key: 'fileSize',
        header: resourcesContext.messages['size'],
        sortable: true
      },
      {
        key: 'creationDateTimestamp',
        header: resourcesContext.messages['creationDate'],
        sortable: true,
        template: getCreationDateTemplate
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate,
        className: styles.actionsColumn
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className || ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.sortable}
      />
    ));
  };

  const getCreationDateTemplate = ({ creationDateTimestamp }) => (
    <span>
      {dayjs(creationDateTimestamp).format(
        `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH:mm' : 'hh:mm A'}`
      )}
    </span>
  );

  const getActionsTemplate = file => {
    const isDownloading = downloadingFileName === file.fileName;

    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
        disabled={loadingStatus === 'pending'}
        icon={isDownloading ? 'spinnerAnimate' : 'export'}
        onClick={() => onDownloadFile(file.fileName)}
        tooltip={resourcesContext.messages['downloadFile']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />
    );
  };

  const renderFilterControls = () => (
    <div className={styles.filterControls}>
      <InputText
        className={styles.filterInput}
        onChange={e => {
          setFilterValue(e.target.value);
          if (e.target.value === '') {
            getImportedFilesList();
          }
        }}
        onKeyPress={e => {
          if (e.key === 'Enter') {
            filterData();
          }
        }}
        placeholder={resourcesContext.messages['search']}
        value={filterValue}
      />
      <Button icon="search" label={resourcesContext.messages['search']} onClick={filterData} />
      <Button
        className="p-button-secondary"
        disabled={!filterValue}
        icon="refresh"
        label={resourcesContext.messages['reset']}
        onClick={resetFilter}
      />
    </div>
  );

  const renderInfoMessage = () => {
    return (
      <span className={styles.infoMessage}>{resourcesContext.messages['importedFilesInfoMessage']}</span>
    );
  };

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={onCloseDialog}
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

    if (loadingStatus === 'error') {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['errorLoadingImportedFiles']}</span>
        </div>
      );
    }

    if (isEmpty(importedFilesList)) {
      return (
        <div>
          {renderFilterControls()}
          <div className={styles.noDataContent}>
            <span>{resourcesContext.messages['noImportedFiles']}</span>
          </div>
        </div>
      );
    }

    return (
      <div className={styles.dialogContent}>
        {renderFilterControls()}
        <DataTable
          autoLayout
          hasDefaultCurrentPage
          loading={loadingStatus === 'pending' && downloadingFileName === null}
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={importedFilesList.length}
          value={importedFilesList}>
          {getTableColumns()}
        </DataTable>
      </div>
    );
  };

  return (
    <Dialog
      blockScroll={false}
      className="responsiveDialog"
      footer={dialogFooter}
      header={resourcesContext.messages['importedFiles']}
      modal
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {renderInfoMessage()}
      {renderDialogContent()}
    </Dialog>
  );
};
