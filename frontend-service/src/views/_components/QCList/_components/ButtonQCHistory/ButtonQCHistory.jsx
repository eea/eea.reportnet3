import { useState, useContext } from 'react';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { DataTable } from 'views/_components/DataTable';

import { Column } from 'primereact/column';
import { Dialog } from 'views/_components/Dialog';
import { Button } from 'views/_components/Button';

import styles from './ButtonQCHistory.module.scss';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const ButtonQCHistory = ({ className, style, rowId }) => {
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [showDialog, setShowDialog] = useState(false);

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

  const mockArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
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

  const metadataTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.metadata ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const expressionTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.expression ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const statusTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.status ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const getHistoryColumns = () => {
    const columnData = Object.keys(fields[0]).map(key => ({ field: key, header: key }));

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

  const generateHistoryDialogContent = () => {
    const columns = getHistoryColumns();

    // todo add loading indicator if no data
    // if (!tabsValidationsState.qcHistoryData) {
    //   return (
    //     <div className={styles.loadingSpinner}>
    //       <Spinner className={styles.spinnerPosition} />
    //     </div>
    //   );
    // }

    return (
      <DataTable
        autoLayout
        hasDefaultCurrentPage={true}
        paginator={true}
        //paginatorDisabled={fields.length > 15}
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={fields.length}
        value={fields}>
        {columns}
      </DataTable>
    );
  };

  const footerQcHistory = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      id="cancelHistoryQc"
      label={resourcesContext.messages['close']}
      onClick={() => setShowDialog(false)}
    />
  );

  const DialogQcHistory = () => (
    <Dialog
      className="responsiveDialog"
      footer={footerQcHistory}
      header={resourcesContext.messages['qcHistoryDialogHeader']}
      onHide={() => setShowDialog(false)}
      visible={showDialog}>
      {generateHistoryDialogContent()}
    </Dialog>
  );

  return (
    <div>
      {showDialog && <DialogQcHistory />}

      <Button
        className={className}
        disabled={validationContext.isFetchingData}
        icon="info"
        onClick={() => {
          setShowDialog(true);
          //setViewedQcHistoryId(rowId);
        }}
        style={style}
        tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />
    </div>
  );
};
