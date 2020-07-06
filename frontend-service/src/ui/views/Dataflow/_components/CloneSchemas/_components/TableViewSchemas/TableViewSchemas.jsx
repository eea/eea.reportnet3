import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import styles from './TableViewSchemas.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableViewSchemas = ({
  checkedDataflow,
  data,
  handleRedirect,
  onChangePagination,
  onSelectDataflow,
  pagination
}) => {
  const resources = useContext(ResourcesContext);

  const fieldTables = {
    expirationDate: resources.messages['nextReportDue'],
    obligation: resources.messages['obligationTitle'],
    legalInstruments: resources.messages['legalInstruments']
  };

  const getOrderedFields = dataflows => {
    const dataflowsWithPriority = [
      { id: 'name', index: 0 },
      { id: 'description', index: 1 },
      { id: 'obligationTitle', index: 2 },
      { id: 'legalInstruments', index: 3 },
      { id: 'status', index: 4 },
      { id: 'expirationDate', index: 5 }
    ];

    return dataflows
      .map(field => dataflowsWithPriority.filter(e => field === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedField => orderedField.id);
  };

  const headerTableTemplate = field => fieldTables[field] || resources.messages[field];

  const onLoadCheckButton = row => {
    return (
      <div className={styles.checkColum}>
        <Checkbox
          id={`${row.id}_checkbox`}
          inputId={`${row.id}_checkbox`}
          isChecked={checkedDataflow.id === row.id}
          onChange={() => onSelectDataflow(row)}
          role="checkbox"
        />
        <label for={`${row.id}_checkbox`} className="srOnly">
          {resources.messages['selectedDataflow']}
        </label>
      </div>
    );
  };

  const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

  const onLoadTitleTemplate = row => (
    <div className={styles.titleColum}>
      {row.name}
      <FontAwesomeIcon
        className={styles.linkIcon}
        icon={AwesomeIcons('externalLink')}
        onMouseDown={() => handleRedirect(row.id)}
      />
    </div>
  );

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = dataflows => {
    const fieldColumns = getOrderedFields(Object.keys(dataflows[0])).map(field => {
      let template = null;
      if (field === 'name') template = onLoadTitleTemplate;

      return <Column body={template} field={field} header={headerTableTemplate(field)} key={field} sortable={true} />;
    });

    fieldColumns.unshift(renderCheckColum);

    return fieldColumns;
  };

  const paginatorRightText = `${resources.messages['totalDataflows']}: ${data.length}`;

  return isEmpty(data) ? (
    <h3 className={styles.noDataflows}>{resources.messages['noDataflowsWithSelectedParameters']}</h3>
  ) : (
    <DataTable
      autoLayout={true}
      first={pagination.first}
      getPageChange={onLoadPagination}
      onRowClick={event => onSelectDataflow(event.data)}
      paginator={true}
      paginatorRight={paginatorRightText}
      rows={pagination.rows}
      rowsPerPageOptions={[5, 10, 15]}
      totalRecords={data.length}
      value={data}>
      {renderColumns(data)}
    </DataTable>
  );
};
