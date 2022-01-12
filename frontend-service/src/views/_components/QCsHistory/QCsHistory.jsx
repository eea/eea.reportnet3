import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';
import capitalize from 'lodash/capitalize';

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

import { ValidationService } from 'services/ValidationService';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

export const QCsHistory = ({ datasetId, isDialogVisible, onCloseDialog, validationId, validations }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [clickedValidationId, setClickedValidationId] = useState(null);
  const [isRecursivelyOpenedDialog, setIsRecursivelyOpenedDialog] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [qcHistoryData, setQcHistoryData] = useState([]);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  const isAllQCsHistoryDialog = isNil(validationId);

  useEffect(() => {
    getQcHistoryData();
  }, []);

  const onRowClick = id => {
    setClickedValidationId(id);
    setIsRecursivelyOpenedDialog(true);
  };

  const onCloseRecursivelyOpenedDialog = () => {
    setIsRecursivelyOpenedDialog(false);
    setClickedValidationId(null);
  };

  const renderHistoryDialogContent = () => {
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
          <p>{resourcesContext.messages['loadHistoryDataError']}</p>
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
    }

    return (
      <DataTable
        autoLayout
        className={styles.dialogContent}
        hasDefaultCurrentPage
        onRowClick={event => {
          if (isAllQCsHistoryDialog) {
            onRowClick(event.data.ruleId);
          }
        }}
        paginator
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        selectionMode={isAllQCsHistoryDialog ? 'single' : null}
        totalRecords={qcHistoryData.length}
        value={qcHistoryData}>
        {columns}
      </DataTable>
    );
  };

  const getHistoryColumns = () => {
    if (isEmpty(qcHistoryData)) {
      return [];
    }

    const columnData = getColumnsInOrder(Object.keys(qcHistoryData[0])).map(key => ({ field: key }));

    return columnData.map(col => {
      if (
        col.field === 'ruleBefore' ||
        (!isAllQCsHistoryDialog && col.field === 'ruleInfoId') ||
        (!isAllQCsHistoryDialog && col.field === 'ruleId')
      ) {
        return null;
      }

      let template;
      let header = capitalize(col.field);

      switch (col.field) {
        case 'expression':
        case 'metadata':
        case 'status':
          template = checkTemplate;
          break;
        case 'ruleId':
          template = nameTemplate;
          header = 'Name';
          break;
        case 'ruleInfoId':
          template = codeTemplate;
          header = 'Code';
          break;
        case 'timestamp':
          template = timestampTemplate;
          break;
        default:
          template = null;
          break;
      }

      return <Column body={template} field={col.field} header={header} key={col.field} sortable />;
    });
  };

  const getColumnsInOrder = historyDataKeys => {
    const historyDataKeysInOrder = [
      { id: 'ruleInfoId', index: 0 },
      { id: 'ruleId', index: 1 },
      { id: 'user', index: 2 },
      { id: 'timestamp', index: 3 },
      { id: 'metadata', index: 4 },
      { id: 'expression', index: 5 },
      { id: 'status', index: 6 }
    ];

    return historyDataKeys
      .map(key => historyDataKeysInOrder.filter(orderedKey => key === orderedKey.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedKey => orderedKey.id);
  };

  const getQcHistoryData = async () => {
    setLoadingStatus('pending');

    try {
      const response = isAllQCsHistoryDialog
        ? await ValidationService.getAllQCsHistoricInfo(datasetId)
        : await ValidationService.getQcHistoricInfo(datasetId, validationId);
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

  const nameTemplate = rowData => {
    const currentRule = validations.find(validation => rowData.ruleId === validation.id);
    return <div>{currentRule?.name}</div>;
  };

  const codeTemplate = rowData => {
    const currentRule = validations.find(validation => rowData.ruleId === validation.id);
    return <div>{currentRule?.shortCode}</div>;
  };

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

  if (isRecursivelyOpenedDialog) {
    return (
      <QCsHistory
        datasetId={datasetId}
        isDialogVisible={isRecursivelyOpenedDialog}
        onCloseDialog={onCloseRecursivelyOpenedDialog}
        validationId={clickedValidationId}
      />
    );
  }

  return (
    <Dialog
      className={`responsiveDialog ${styles.dialogWidth}`}
      footer={dialogFooter}
      header={
        isAllQCsHistoryDialog
          ? resourcesContext.messages['allQCsHistoryHeader']
          : resourcesContext.messages['qcHistoryDialogHeader']
      }
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {renderHistoryDialogContent()}
    </Dialog>
  );
};
