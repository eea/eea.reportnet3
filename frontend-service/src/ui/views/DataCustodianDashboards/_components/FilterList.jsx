import React, { useState } from 'react';

import { uniqBy } from 'lodash';

import { TableListItem } from './_components/TableListItem';

function FilterList({ filteredDataState: { datasets }, filterDispatch }) {
  const createTableCheckBoxObject = dataset => {
    return { tableName: dataset.tableName, tableId: dataset.tableId };
  };

  const tableNamesIdsArray = [];

  const uniqDatasets = uniqBy(datasets, 'tableId');

  uniqDatasets.map(dataset => {
    const datasetObject = createTableCheckBoxObject(dataset);
    tableNamesIdsArray.push(datasetObject);
  });

  return (
    <ul>
      {tableNamesIdsArray.map(item => (
        <TableListItem key={item.tableId} item={item} filterDispatch={filterDispatch}></TableListItem>
      ))}
    </ul>
  );
}

export { FilterList };
