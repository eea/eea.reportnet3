import React, { useContext } from 'react';

import { uniqBy } from 'lodash';

import styles from './FilterList.module.scss';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { ReportersListItem } from './_components/ReportersListItem';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { StatusList } from './_components/StatusList';
import { TableListItem } from './_components/TableListItem';

const FilterList = ({
  color,
  filterDispatch,
  levelErrors,
  originalData: { datasets, labels },
  reporterFilters,
  statusFilters,
  tableFilters
}) => {
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
        <AccordionTab header={resources.messages['filterByDataset']}>
          <ul className={styles.list}>
            {labels.map(item => (
              <ReportersListItem
                key={item}
                reporterFilters={reporterFilters}
                filterDispatch={filterDispatch}
                item={item}
              />
            ))}
          </ul>
        </AccordionTab>
      );
    } else {
      return <AccordionTab header={resources.messages['filterByDataset']} disabled={true} />;
    }
  };

  const filterByTables = () => {
    if (tableNamesIdsArray.length > 0) {
      return (
        <AccordionTab header={resources.messages['filterByTable']}>
          <ul className={styles.list}>
            {tableNamesIdsArray.map(item => (
              <TableListItem
                key={item.tableId}
                tableFilters={tableFilters}
                filterDispatch={filterDispatch}
                item={item}
              />
            ))}
          </ul>
        </AccordionTab>
      );
    } else {
      return <AccordionTab header={resources.messages['filterByTable']} disabled={true} />;
    }
  };

  return (
    <>
      <Accordion multiple={true}>
        {filterByReporters()}
        {filterByTables()}
      </Accordion>
      <StatusList
        statusFilters={statusFilters}
        color={color}
        levelErrors={levelErrors}
        filterDispatch={filterDispatch}
      />
    </>
  );
};

export { FilterList };
