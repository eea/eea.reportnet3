import React, { useState } from 'react';

import { uniqBy } from 'lodash';

import { CountriesListItem } from '../_components/CountriesListItem';
import { TableListItem } from '../_components/TableListItem';

function FilterList({ originalData: { datasets, labels }, filterDispatch }) {
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
    <>
      <div>
        <h3>Filter by Tables/Datasets</h3>
        <ul>
          {tableNamesIdsArray.map(item => (
            <TableListItem key={item.tableId} item={item} filterDispatch={filterDispatch}></TableListItem>
          ))}
        </ul>
      </div>

      <div>
        <h3>Filter by Countrie</h3>
        <ul>
          {labels.map(item => (
            <CountriesListItem key={item} item={item} filterDispatch={filterDispatch}></CountriesListItem>
          ))}
        </ul>
      </div>
    </>
  );
}

export { FilterList };
