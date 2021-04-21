import { Fragment, useContext } from 'react';

import styles from './NationalSystems.module.scss';

import { tables } from './nationalSystems.webform.json';

import { NationalSystemsTable } from './_components/NationalSystemsTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const NationalSystems = ({ datasetId, state }) => {
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

    // if (!areEquals(data?.records[0]?.fields[0].type, 'ATTACHMENT')) {
    //   errorMessages.push(resources.messages['webformShouldBeAttachment']);
    // }

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
      {tables.map((table, index) => {
        const schemaTable = schemaTables.filter(tab => areEquals(tab['tableSchemaName'], table['name']))[0];

        return (
          <Fragment key={index}>
            <NationalSystemsTable
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
