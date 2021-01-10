import React, { Fragment, useContext } from 'react';

import { tables } from './nationalSystems.webform.json';

import { NationalSystemsTable } from './_components/NationalSystemsTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const NationalSystems = ({ dataflowId, datasetId, isReporting, state }) => {
  const { datasetSchema } = state;

  const resources = useContext(ResourcesContext);

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

    if (!TextUtils.areEquals(data?.records[0]?.fields[0].type, 'ATTACHMENT')) {
      errorMessages.push('goodf');
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

  return datasetSchema.tables
    .filter(tab => tab.tableSchemaId)
    .map((table, index) => {
      const configTables = tables.filter(tab => TextUtils.areEquals(tab['name'], table['tableSchemaName']))[0];

      return (
        <Fragment key={index}>
          <NationalSystemsTable
            datasetId={datasetId}
            errorMessages={getErrorMessages}
            schemaTables={table}
            tables={configTables}
            tableSchemaId={table.tableSchemaId}
          />
        </Fragment>
      );
    });
};
