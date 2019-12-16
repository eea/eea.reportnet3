import React, { useContext, useState } from 'react';

import { uniqBy } from 'lodash';

import styles from './FilterList.module.scss';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { ReporterList } from './_components/ReporterList';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { StatusList } from './_components/StatusList';
import { TableListItem } from './_components/TableListItem';

const FilterList = ({
  datasetSchemaId,
  color,
  filterDispatch,
  levelErrors,
  originalData: { datasets, labels },
  reporterFilters,
  statusFilters,
  tableFilters
}) => {
  const resources = useContext(ResourcesContext);

  const tables = [];
  const allTables = uniqBy(datasets, 'tableId');
  allTables.forEach(dataset => {
    const table = {};
    table.tableName = dataset.tableName;
    table.tableId = dataset.tableId;
    tables.push(table);
  });

  const filterByReporters = () => {
    return labels.length > 0 ? (
      <AccordionTab header={resources.messages['filterByDataset']}>
        <ReporterList
          datasetSchemaId={datasetSchemaId}
          filterDispatch={filterDispatch}
          reporterFilters={reporterFilters}
          labels={labels}
        />
      </AccordionTab>
    ) : (
      <AccordionTab header={resources.messages['filterByTable']} disabled={true} />
    );
  };

  const filterByTables = () => {
    return tables.length > 0 ? (
      <AccordionTab header={resources.messages['filterByTable']}>
        <ul className={styles.list}>
          {tables.map(table => (
            <TableListItem
              key={table.id}
              datasetSchemaId={datasetSchemaId}
              filterDispatch={filterDispatch}
              item={table}
              tableFilters={tableFilters}
            />
          ))}
        </ul>
      </AccordionTab>
    ) : (
      <AccordionTab header={resources.messages['filterByTable']} disabled={true} />
    );
  };

  return (
    <>
      <Accordion multiple={true}>
        {filterByReporters()}
        {filterByTables()}
      </Accordion>
      <StatusList
        datasetSchemaId={datasetSchemaId}
        statusFilters={statusFilters}
        color={color}
        levelErrors={levelErrors}
        filterDispatch={filterDispatch}
      />
    </>
  );
};

export { FilterList };
