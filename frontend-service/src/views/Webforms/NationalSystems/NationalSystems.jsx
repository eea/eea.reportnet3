import { Fragment, useContext } from 'react';

import uniqueId from 'lodash/uniqueId';

import styles from './NationalSystems.module.scss';

import { tables } from './nationalSystems.webform.json';

import { NationalSystemsTable } from './_components/NationalSystemsTable';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const NationalSystems = ({ dataProviderId, dataflowId, datasetId, state }) => {
  const resources = useContext(ResourcesContext);

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
      errorMessages.push(resources.messages['webformTableWithLessRecords']);
    }

    if (data?.totalRecords > 1) {
      errorMessages.push(resources.messages['webformTableWithMoreRecords']);
    }

    return errorMessages;
  };

  const renderTableErrors = data => {
    const errorMessages = [];

    if (data?.totalRecords === 0) {
      errorMessages.push(resources.messages['webformTableWithLessRecords']);
    }

    return errorMessages;
  };

  return (
    <div className={styles.nationalSystems}>
      {tables.map(table => {
        const schemaTable = schemaTables.filter(tab => areEquals(tab['tableSchemaName'], table['name']))[0];

        return (
          <Fragment key={uniqueId()}>
            <NationalSystemsTable
              dataProviderId={dataProviderId}
              dataflowId={dataflowId}
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
