import React from 'react';

import isEmpty from 'lodash/isEmpty';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

export const TableView = ({ data, onSelectObl }) => {
  const onLoadCheckButton = row => <Checkbox checked={true} onChange={() => onSelectObl(row)} role="checkbox" />;

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = data => {
    const repOblCols = [];
    const repOblKeys = !isEmpty(data) ? Object.keys(data[0]) : [];
    repOblCols.push(
      repOblKeys.map(obligation => (
        <Column
          key={obligation}
          field={obligation}
          // body={template}
          // key={field}
          columnResizeMode="expand"
          // field={field}
          header={obligation}
          // sortable={true}
          // style={columnStyles(field)}
        />
      ))
    );
    return [renderCheckColum, ...repOblCols];
  };

  return (
    <DataTable
      autoLayout={true}
      // onRowClick={event => setValidationId(event.data.id)}
      paginator={true}
      rows={10}
      rowsPerPageOptions={[5, 10, 15]}
      totalRecords={data.length}
      value={data}>
      {renderColumns(data)}
    </DataTable>
  );
};
