import React, { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableView = ({ checkedRow, data, onSelectObl }) => {
  const resources = useContext(ResourcesContext);

  const onLoadCheckButton = row => (
    <Checkbox
      id={`${row.id}_checkbox`}
      isChecked={checkedRow.title === row.title}
      onChange={() => onSelectObl(row)}
      role="checkbox"
    />
  );

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = data => {
    const repOblCols = [];
    const repOblKeys = !isEmpty(data) ? Object.keys(data[0]) : [];
    repOblCols.push(
      repOblKeys
        .filter(key => key !== 'id')
        .map(obligation => (
          <Column
            columnResizeMode="expand"
            field={obligation}
            header={resources.messages[obligation]}
            key={obligation}
          />
        ))
    );
    return [renderCheckColum, ...repOblCols];
  };

  return (
    <DataTable
      autoLayout={true}
      onRowClick={event => onSelectObl(event.data)}
      paginator={true}
      rows={10}
      rowsPerPageOptions={[5, 10, 15]}
      totalRecords={data.length}
      value={data}>
      {renderColumns(data)}
    </DataTable>
  );
};
