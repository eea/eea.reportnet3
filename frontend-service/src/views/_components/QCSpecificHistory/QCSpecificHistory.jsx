import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import orderBy from 'lodash/orderBy';

import styles from './QCSpecificHistory.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ValidationService } from 'services/ValidationService';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

export const QCSpecificHistory = ({ datasetId, isDialogVisible, onCloseDialog, validationId }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [qcHistoryData, setQcHistoryData] = useState([]);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  useEffect(() => {
    getQcHistoryData();
  }, []);

  const renderHistoryDialogContent = () => {
    if (loadingStatus === 'pending') {
      return (
        <div className={styles.loadingSpinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (loadingStatus === 'failed') {
      return (
        <div className={styles.noDataContent}>
          <p>{resourcesContext.messages['loadHistoryDataError']}</p>
          <Button label={resourcesContext.messages['refresh']} onClick={getQcHistoryData} />
        </div>
      );
    }

    if (isEmpty(qcHistoryData)) {
      return (
        <div className={styles.noDataContent}>
          <h3>{resourcesContext.messages['noHistoryData']}</h3>
        </div>
      );
    }

    return (
      <DataTable
        autoLayout
        className={styles.dialogContent}
        hasDefaultCurrentPage
        paginator
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={qcHistoryData.length}
        value={qcHistoryData}>
        {getHistoryColumns()}
      </DataTable>
    );
  };

  const getHistoryColumns = () => {
    const columns = [
      {
        key: 'user',
        header: resourcesContext.messages['user']
      },
      {
        key: 'timestamp',
        header: resourcesContext.messages['timestamp'],
        template: timestampTemplate
      },
      {
        key: 'expression',
        header: resourcesContext.messages['expressionText'],
        template: checkTemplate
      },
      {
        key: 'metadata',
        header: resourcesContext.messages['metadata'],
        template: checkTemplate
      },
      {
        key: 'status',
        header: resourcesContext.messages['status'],
        template: checkTemplate
      }
    ];

    return columns.map(column => {
      return <Column body={column.template} field={column.key} header={column.header} key={column.key} sortable />;
    });
  };

  const getQcHistoryData = async () => {
    setLoadingStatus('pending');

    try {
      const response = await ValidationService.getQcHistoricInfo(datasetId, validationId);
      const data = response.data;
      setQcHistoryData(orderBy(data, 'timestamp', 'desc'));
      setLoadingStatus('success');
    } catch (error) {
      console.error('HistoryData - getQcHistoryData.', error);
      notificationContext.add({ type: 'LOAD_HISTORY_DATA_ERROR' }, true);
      setLoadingStatus('failed');
    }
  };

  const checkTemplate = (rowData, column) => (
    <div className={styles.checkedValueColumn}>
      {rowData[column.field] ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const timestampTemplate = rowData => <div>{getDateTimeFormatByUserPreferences(rowData.timestamp)}</div>;

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      id="cancelHistoryQc"
      label={resourcesContext.messages['close']}
      onClick={onCloseDialog}
    />
  );

  return (
    <Dialog
      className={`responsiveDialog ${styles.dialogWidth}`}
      footer={dialogFooter}
      header={resourcesContext.messages['qcHistoryDialogHeader']}
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {renderHistoryDialogContent()}
    </Dialog>
  );
};
