import { useContext, useState } from 'react';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';

import styles from './ButtonQCHistory.module.scss';

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
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { ValidationService } from 'services/ValidationService';

export const ButtonQCHistory = ({ className, datasetId, ruleId, style }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const validationContext = useContext(ValidationContext);

  const [isDialogVisible, setIsDialogVisible] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [qcHistoryData, setQcHistoryData] = useState([]);

  const onCloseDialog = () => {
    setIsDialogVisible(false);
    setQcHistoryData([]);
  };

  const footerQcHistory = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      id="cancelHistoryQc"
      label={resourcesContext.messages['close']}
      onClick={() => onCloseDialog()}
    />
  );

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
        <div className={styles.errorLoadingHistory}>
          <p>{resourcesContext.messages['loadHistorydataError']}</p>
          <Button label={resourcesContext.messages['refresh']} onClick={getQcHistoryData}></Button>
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
      if (col.field === 'ruleBefore' || col.field === 'ruleInfoId' || col.field === 'ruleId') {
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
      const response = await ValidationService.getHistoricReleases(datasetId, ruleId);
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

  const timestampTemplate = rowData => (
    <div>
      {dayjs(rowData.timestamp).format(
        `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
          userContext.userProps.amPm24h ? '' : ' A'
        }`
      )}
    </div>
  );

  const statusTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.status ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  return (
    <div>
      <Button
        className={className}
        disabled={validationContext.isFetchingData}
        icon="info"
        onClick={() => {
          setIsDialogVisible(true);
          getQcHistoryData();
        }}
        style={style}
        tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />

      {isDialogVisible && (
        <Dialog
          className={`responsiveDialog ${styles.dialogSize}`}
          footer={footerQcHistory}
          header={resourcesContext.messages['qcHistoryDialogHeader']}
          onHide={onCloseDialog}
          visible={isDialogVisible}>
          {generateHistoryDialogContent()}
        </Dialog>
      )}
    </div>
  );
};
