import { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

import isNil from 'lodash/isNil';

import styles from './SqlSentenceValidation.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ValidationService } from 'services/ValidationService';

export const SqlSentenceValidation = ({
  isVisibleSqlSentenceValidationDialog,
  setIsVisibleSqlSentenceValidationDialog,
  sqlSentence
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [columns, setColumns] = useState();
  const [errorMessage, setErrorMessage] = useState(resourcesContext.messages['notValidSql']);
  const [isLoading, setIsLoading] = useState(true);
  const [sqlResponse, setSqlResponse] = useState(null);

  const { datasetId } = useParams();

  useEffect(() => {
    validateSqlSentence();
  }, []);

  useEffect(() => {
    if (!isNil(sqlResponse) && sqlResponse.length > 0) {
      setColumns(generateColumns());
    } else if (!isNil(sqlResponse) && sqlResponse.length === 0) {
      setColumns(sqlResponse);
    }
  }, [sqlResponse]);

  const generateColumns = () => {
    const [firstRow] = sqlResponse;

    const columnData = Object.keys(firstRow).map(key => ({ field: key, header: key.replace('*', '.') }));

    return columnData.map(col => <Column field={col.field} header={col.header} key={col.field} />);
  };

  const validateSqlSentence = async () => {
    setIsLoading(true);

    try {
      const showInternalFields = true;
      const response = await ValidationService.runSqlRule(datasetId, sqlSentence, showInternalFields);

      setSqlResponse(response);
    } catch (error) {
      console.error('SqlSentenceValidation - validateSqlSentence.', error);
      if (error.response.status === 400 || error.response.status === 422) {
        setErrorMessage(error.response.data.message);
      } else {
        notificationContext.add({ type: 'VALIDATE_SQL_ERROR' }, true);
        setIsVisibleSqlSentenceValidationDialog(false);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const footer = (
    <Button
      className="p-button-secondary p-button-right-aligned"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
      onClick={() => setIsVisibleSqlSentenceValidationDialog(false)}
    />
  );
  const renderTable = () => {
    return (
      <DataTable paginator={false} value={sqlResponse}>
        {columns}
      </DataTable>
    );
  };

  const renderDialogContent = () => {
    if (!isNil(sqlResponse) && sqlResponse.length === 0) {
      return (
        <div className={styles.messageWrapper}>
          <p>{resourcesContext.messages['noData']}</p>
        </div>
      );
    } else if (!isNil(sqlResponse) && sqlResponse.length > 0) {
      return renderTable();
    } else {
      return (
        <div className={styles.messageWrapper}>
          <p>{errorMessage}</p>
        </div>
      );
    }
  };

  return (
    <Dialog
      contentStyle={{ height: '78vh' }}
      footer={footer}
      header={resourcesContext.messages['sqlSentenceValidationDialogTitle']}
      onHide={() => setIsVisibleSqlSentenceValidationDialog(false)}
      style={{ width: '90%' }}
      visible={isVisibleSqlSentenceValidationDialog}>
      {isLoading ? <Spinner /> : renderDialogContent()}
    </Dialog>
  );
};
