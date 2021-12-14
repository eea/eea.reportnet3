import { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

import isNil from 'lodash/isNil';

import styles from './SqlSentenceValidation.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ValidationService } from 'services/ValidationService';

export const SqlSentenceValidation = ({
  isVisibleSqlSentenceValidationDialog,
  setIsVisibleSqlSentenceValidationDialog,
  sqlSentence
}) => {
  const [columns, setColumns] = useState();
  const [isLoading, setIsLoading] = useState(true);
  const [sqlResponse, setSqlResponse] = useState();

  const resourcesContext = useContext(ResourcesContext);

  const { datasetId } = useParams();

  useEffect(() => {
    validateSqlSentence();
  }, []);

  useEffect(() => {
    if (sqlResponse) {
      setColumns(generateColumns());
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
    if (sqlResponse) {
      return renderTable();
    } else {
      return (
        <div className={styles.errorMessageWrapper}>
          <p>{resourcesContext.messages['notValidSql']}</p>
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
