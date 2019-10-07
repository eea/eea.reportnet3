import React, { useContext } from 'react';

import { isUndefined, uniqBy } from 'lodash';

import styles from './FilterList.module.scss';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { ReportersListItem } from '../_components/ReportersListItem';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { StatusList } from '../_components/StatusList';
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

  const filterByReporters = () => {
    if (labels.length > 0) {
      return (
        <AccordionTab header={resources.messages['filterByReporters']}>
          <ul className={styles.list}>
            {labels.map(item => (
              <ReportersListItem key={item} item={item} filterDispatch={filterDispatch} />
            ))}
          </ul>
        </AccordionTab>
      );
    }
  };

  const filterByTables = () => {
    if (tableNamesIdsArray.length > 0) {
      return (
        <AccordionTab header={resources.messages['filterByTables']}>
          <ul className={styles.list}>
            {tableNamesIdsArray.map(item => (
              <TableListItem key={item.tableId} item={item} filterDispatch={filterDispatch} />
            ))}
          </ul>
        </AccordionTab>
      );
    }
  };

  return (
    <>
      <Accordion>
        {filterByReporters()}
        {filterByTables()}
      </Accordion>
      <StatusList filterDispatch={filterDispatch}></StatusList>
    </>
  );
}

export { FilterList };
