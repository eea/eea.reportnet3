import { useContext, useEffect, useState } from 'react';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './QCsHistory.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { ValidationService } from 'services/ValidationService';

export const QCsHistory = ({ datasetId, isDialogVisible, onCloseDialog, validationId }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [qcHistoryData, setQcHistoryData] = useState([]);

  useEffect(() => {
    getQcHistoryData();
  }, []);

  const expressionTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.expression ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const generateHistoryDialogContent = () => {
    const columns = getHistoryColumns();

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
          <p>{resourcesContext.messages['loadHistorydataError']}</p>
          <Button label={resourcesContext.messages['refresh']} onClick={getQcHistoryData} />
        </div>
      );
    }

    if (isEmpty(columns)) {
      return (
        <div className={styles.noDataContent}>
          <h3>{resourcesContext.messages['noHistoryData']}</h3>
        </div>
      );
    } else {
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
          {columns}
        </DataTable>
      );
    }
  };

  const getHistoryColumns = () => {
    const columnData = isEmpty(qcHistoryData)
      ? []
      : Object.keys(qcHistoryData[0]).map(key => ({ field: key, header: key }));

    return columnData.map(col => {
      if (col.field === 'ruleBefore' || col.field === 'ruleInfoId') {
        return null;
      } else if (!isNil(validationId) && col.field === 'ruleId') {
        return null;
      }
      let template;

      switch (col.field) {
        case 'expression':
          template = expressionTemplate;
          break;
        case 'metadata':
          template = metadataTemplate;
          break;
        case 'status':
          template = statusTemplate;
          break;
        case 'ruleId':
          template = ruleIdTemplate;
          break;
        case 'timestamp':
          template = timestampTemplate;
          break;
        default:
          template = null;
          break;
      }

      return <Column body={template} field={col.field} header={col.header.toUpperCase()} key={col.field} />;
    });
  };

  const getQcHistoryData = async () => {
    setLoadingStatus('pending');

    try {
      const response = isNil(validationId)
        ? await ValidationService.getAllQcHistoricInfo(datasetId)
        : await ValidationService.getQcHistoricInfo(datasetId, validationId);
      const data = response.data;

      setQcHistoryData(data);
      setLoadingStatus('success');
    } catch (error) {
      console.error('HistoryData - getQcHistoryData.', error);
      notificationContext.add({ type: 'LOAD_HISTORYDATA_ERROR' }, true);
      setLoadingStatus('failed');
    }
  };

  const metadataTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.metadata ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const statusTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.status ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const ruleIdTemplate = rowData => {
    return <div>{rowData.ruleId}</div>;
  };

  const timestampTemplate = rowData => (
    <div>
      {dayjs(rowData.timestamp).format(
        `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
          userContext.userProps.amPm24h ? '' : ' A'
        }`
      )}
    </div>
  );

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
      className={`responsiveDialog ${styles.dialogSize}`}
      footer={dialogFooter}
      header={resourcesContext.messages['qcHistoryDialogHeader']}
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {generateHistoryDialogContent()}
    </Dialog>
  );
};
