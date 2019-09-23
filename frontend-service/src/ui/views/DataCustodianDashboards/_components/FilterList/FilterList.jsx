import React, { useState, useContext } from 'react';

import { uniqBy } from 'lodash';

import styles from './FilterList.module.scss';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { CountriesListItem } from '../_components/CountriesListItem';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { TableListItem } from '../_components/TableListItem';

function FilterList({ originalData: { datasets, labels }, filterDispatch }) {
  const resources = useContext(ResourcesContext);
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
      <Accordion>
        <AccordionTab header={resources.messages['filterByTables']}>
          <ul className={styles.list}>
            {tableNamesIdsArray.map(item => (
              <TableListItem key={item.tableId} item={item} filterDispatch={filterDispatch} />
            ))}
          </ul>
        </AccordionTab>
        <AccordionTab header={resources.messages['filterByCountries']}>
          <ul className={styles.list}>
            {labels.map(item => (
              <CountriesListItem key={item} item={item} filterDispatch={filterDispatch} />
            ))}
          </ul>
        </AccordionTab>
      </Accordion>
    </>
  );
}

export { FilterList };
