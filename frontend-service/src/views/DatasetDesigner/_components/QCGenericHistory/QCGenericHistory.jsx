import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import orderBy from 'lodash/orderBy';

import styles from './QCGenericHistory.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { QCSpecificHistory } from 'views/_components/QCSpecificHistory';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { ValidationService } from 'services/ValidationService';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

export const QCGenericHistory = ({ datasetId, isDialogVisible, onCloseDialog }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [qcHistoryData, setQcHistoryData] = useState([]);

  const [isSpecificHistoryDialogOpened, setIsSpecificHistoryDialogOpened] = useState(false);
  const [selectedValidationId, setSelectedValidationId] = useState(null);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  useEffect(() => {
    getQcHistoryData();
  }, []);

  const onOpenHistoryDialog = validationId => {
    setSelectedValidationId(validationId);
    setIsSpecificHistoryDialogOpened(true);
  };

  const onCloseHistoryDialog = () => {
    setSelectedValidationId('');
    setIsSpecificHistoryDialogOpened(false);
  };

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
    const headers = [
      {
        id: 'ruleCode',
        label: resourcesContext.messages['ruleCode'],
        template: ruleCodeTemplate
      },
      {
        id: 'name',
        label: resourcesContext.messages['name'],
        template: nameTemplate
      },
      {
        id: 'user',
        label: resourcesContext.messages['user']
      },
      {
        id: 'timestamp',
        label: resourcesContext.messages['timestamp'],
        template: timestampTemplate
      },
      {
        id: 'expression',
        label: resourcesContext.messages['expressionText'],
        template: checkTemplate
      },
      {
        id: 'metadata',
        label: resourcesContext.messages['metadata'],
        template: checkTemplate
      },
      {
        id: 'status',
        label: resourcesContext.messages['status'],
        template: checkTemplate
      },
      {
        id: 'actions',
        label: resourcesContext.messages['actions'],
        template: actionsTemplate
      }
    ];

    return headers.map(header => {
      return <Column body={header.template} field={header.id} header={header.label} key={header.id} sortable />;
    });
  };

  const actionsTemplate = rowData => {
    return (
      <div className={styles.editButtonWrapper}>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.editRowButton}`}
          disabled={validationContext.isFetchingData}
          icon="info"
          onClick={() => {
            onOpenHistoryDialog(rowData.ruleId);
          }}
          tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      </div>
    );
  };

  const getQcHistoryData = async () => {
    setLoadingStatus('pending');

    try {
      const response = await ValidationService.getAllQCsHistoricInfo(datasetId);
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

  const getRuleSchema = ruleId =>
    validationContext.rulesDescription.find(ruleDescription => ruleDescription.id === ruleId);

  const nameTemplate = rowData => {
    const currentRule = getRuleSchema(rowData.ruleId);
    return <div>{currentRule?.name}</div>;
  };

  const ruleCodeTemplate = rowData => {
    const currentRule = getRuleSchema(rowData.ruleId);
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

  return (
    <Fragment>
      <Dialog
        className={`responsiveDialog ${styles.dialogWidth}`}
        footer={dialogFooter}
        header={resourcesContext.messages['allQCsHistoryHeader']}
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderHistoryDialogContent()}
      </Dialog>
      {isSpecificHistoryDialogOpened && (
        <QCSpecificHistory
          datasetId={datasetId}
          isDialogVisible={isSpecificHistoryDialogOpened}
          onCloseDialog={onCloseHistoryDialog}
          validationId={selectedValidationId}
        />
      )}
    </Fragment>
  );
};
