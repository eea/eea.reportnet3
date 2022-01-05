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

export const ButtonQCHistory = ({ className, style, ruleId }) => {
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

  const getQcHistoryData = async ruleId => {
    await simulateEndPoint(1000);

    const mockArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17];
    // const fields = tabsValidationsState.history.map(historicEvent => {
    const fields = mockArray.map((_historicEvent, index) => {
      return {
        id: `id-${index + 1}`,
        user: 'qc.user@com.com',
        timestamp: '29/02/2019',
        metadata: true,
        expression: false,
        status: true
      };
    });
    setQcHistoryData(fields);
  };

  const metadataTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.metadata ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const simulateEndPoint = async ms => {
    new Promise(resolve => setTimeout(resolve, ms));
  };
  // const getQcHistory = async () => {
  //   try {
  //     //const { data } = await ValidationService.getQcHistory(tabsValidationsState.viewedQcHistoryId);
  //     // tabsValidationsDispatch({
  //     //   type: 'SET_QC_HISTORY_DATA', // TODO CREATE A REDUCER FOR THIS
  //     //   payload: { qcHistoryData: data }
  //     // });
  //   } catch (error) {
  //     console.error('ValidationsList - getQcHistory.', error);
  //     // notificationContext.add({ type: '________ERROR' }, true); // TODO: add correct error notification
  //   }
  // };

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
          setTimeout(getQcHistoryData, 1000, ruleId);
        }}
        style={style}
        tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />
    </div>
  );
};
