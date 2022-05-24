import { Fragment, useContext } from 'react';

import uniqueId from 'lodash/uniqueId';

import styles from './QuestionAnswerWebform.module.scss';

import { QuestionAnswerWebformTable } from './_components/QuestionAnswerWebformTable';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const QuestionAnswerWebform = ({ dataflowId, dataProviderId, datasetId, state, tables = [] }) => {
  const resourcesContext = useContext(ResourcesContext);

  const { areEquals } = TextUtils;
  const { datasetSchema } = state;

  const schemaTables = datasetSchema.tables.filter(tab => tab.tableSchemaId);

  const getErrorMessages = (data, tableName) => {
    switch (tableName) {
      case 'Table_1':
        return renderTableErrors(data);
      case 'Table_2':
        return renderFileErrors(data);
      default:
        break;
    }
  };

  const renderFileErrors = data => {
    const errorMessages = [];

    if (data?.totalRecords === 0) {
      errorMessages.push(resourcesContext.messages['webformTableWithLessRecords']);
    }

    if (data?.totalRecords > 1) {
      errorMessages.push(resourcesContext.messages['webformTableWithMoreRecords']);
    }

    return errorMessages;
  };

  const renderTableErrors = data => {
    const errorMessages = [];

    if (data?.totalRecords === 0) {
      errorMessages.push(resourcesContext.messages['webformTableWithLessRecords']);
    }

    return errorMessages;
  };

  return (
    <div className={styles.questionAnswer}>
      {tables.map(table => {
        const schemaTable = schemaTables.filter(tab => areEquals(tab['tableSchemaName'], table['name']))[0];

        return (
          <Fragment key={uniqueId()}>
            <QuestionAnswerWebformTable
              dataflowId={dataflowId}
              dataProviderId={dataProviderId}
              datasetId={datasetId}
              errorMessages={getErrorMessages}
              schemaTables={schemaTable}
              tables={table}
            />
          </Fragment>
        );
      })}
    </div>
  );
};
