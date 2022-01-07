import { useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ButtonQCHistory.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Column } from 'primereact/column';

import { Button } from 'views/_components/Button';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { ValidationService } from 'services/ValidationService';

export const ButtonQCHistory = ({ className, style, ruleId, datasetId }) => {
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [showDialog, setShowDialog] = useState(false);
  const [qcHistoryData, setQcHistoryData] = useState([]);

  const closeDialog = () => {
    setShowDialog(false);
    setQcHistoryData([]);
  };

  const footerQcHistory = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      id="cancelHistoryQc"
      label={resourcesContext.messages['close']}
      onClick={() => closeDialog()}
    />
  );

  const expressionTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.expression ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const generateHistoryDialogContent = () => {
    const columns = getHistoryColumns();

    if (isEmpty(qcHistoryData)) {
      return (
        <div className={styles.loadingSpinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    return (
      <DataTable
        autoLayout
        className={`${styles.sizeContentDialog}`}
        hasDefaultCurrentPage={true}
        paginator={true}
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={qcHistoryData.length}
        value={qcHistoryData}>
        {columns}
      </DataTable>
    );
  };

  const getHistoryColumns = () => {
    const columnData = isEmpty(qcHistoryData)
      ? []
      : Object.keys(qcHistoryData[0]).map(key => ({ field: key, header: key }));

    return columnData.map(col => {
      let template;
      switch (col.field) {
        case 'metadata':
          template = metadataTemplate;
          break;
        case 'expression':
          template = expressionTemplate;
          break;
        case 'status':
          template = statusTemplate;
          break;
        default:
          template = null;
          break;
      }

      return <Column body={template} field={col.field} header={col.header.toUpperCase()} key={col.field} />;
    });
  };

  const getQcHistoryData = async () => {
    try {
      const response = await ValidationService.getHistoricReleases(datasetId, ruleId);
      const data = response.data;
      setQcHistoryData(data);
    } catch (error) {
      console.log('error :>> ', error);
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

  return (
    <div>
      {showDialog && (
        <Dialog
          className={`responsiveDialog ${styles.sizeDialog}`}
          footer={footerQcHistory}
          header={resourcesContext.messages['qcHistoryDialogHeader']}
          onHide={() => closeDialog()}
          visible={showDialog}>
          {generateHistoryDialogContent()}
        </Dialog>
      )}

      <Button
        className={className}
        disabled={validationContext.isFetchingData}
        icon="info"
        onClick={() => {
          setShowDialog(true);
          getQcHistoryData();
        }}
        style={style}
        tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />
    </div>
  );
};
