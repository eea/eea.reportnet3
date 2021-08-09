import { Fragment, useContext } from 'react';

import uniqBy from 'lodash/uniqBy';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { ReporterList } from './_components/ReporterList';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { StatusList } from './_components/StatusList';
import { TableList } from './_components/TableList';

const FilterList = ({
  color,
  datasetSchemaId,
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
          labels={labels}
          reporterFilters={reporterFilters}
        />
      </AccordionTab>
    ) : (
      <AccordionTab disabled={true} header={resources.messages['filterByTable']} />
    );
  };

  const filterByTables = () => {
    return tables.length > 0 ? (
      <AccordionTab header={resources.messages['filterByTable']}>
        <TableList
          datasetSchemaId={datasetSchemaId}
          filterDispatch={filterDispatch}
          tableFilters={tableFilters}
          tables={tables}
        />
      </AccordionTab>
    ) : (
      <AccordionTab disabled={true} header={resources.messages['filterByTable']} />
    );
  };

  return (
    <Fragment>
      <Accordion multiple={true}>
        {filterByReporters()}
        {filterByTables()}
      </Accordion>
      <StatusList
        color={color}
        datasetSchemaId={datasetSchemaId}
        filterDispatch={filterDispatch}
        levelErrors={levelErrors}
        statusFilters={statusFilters}
      />
    </Fragment>
  );
};

export { FilterList };
