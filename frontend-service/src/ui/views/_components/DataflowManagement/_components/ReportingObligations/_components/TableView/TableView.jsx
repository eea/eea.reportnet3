import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import styles from './TableView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableView = ({ checkedObligation, data, onSelectObl, onChangePagination, pagination }) => {
  const resources = useContext(ResourcesContext);

  const headerTableTemplate = obligation => {
    if (obligation === 'dueDate') return resources.messages['nextReportDue'];
    else return resources.messages[obligation];
  };

  const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

  const onLoadCheckButton = row => (
    <div className={styles.checkColum}>
      <Checkbox
        id={`${row.id}_checkbox`}
        isChecked={checkedObligation.id === row.id}
        onChange={() => onSelectObl(row)}
        role="checkbox"
      />
    </div>
  );

  const onLoadTitleTemplate = row => (
    <div className={styles.titleColum}>
      {row.title}
      <FontAwesomeIcon
        aria-hidden={false}
        className={styles.linkIcon}
        icon={AwesomeIcons('externalLink')}
        onMouseDown={() => window.open(`http://rod3.devel1dub.eionet.europa.eu/obligations/${row.id}`)}
      />
    </div>
  );

  const paginatorRightText = `${resources.messages['totalObligations']}: ${data.length}`;

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = data => {
    const repOblCols = [];
    const repOblKeys = !isEmpty(data) ? Object.keys(data[0]) : [];
    repOblCols.push(
      repOblKeys
        .filter(key => key !== 'id')
        .map(obligation => {
          let template = null;
          if (obligation === 'title') template = onLoadTitleTemplate;

          return (
            <Column
              body={template}
              columnResizeMode="expand"
              field={obligation}
              header={headerTableTemplate(obligation)}
              key={obligation}
              sortable={true}
            />
          );
        })
    );
    return [renderCheckColum, ...repOblCols];
  };

  return isEmpty(data) ? (
    <h3 className={styles.noObligations}>{resources.messages['noObligationsWithSelectedParameters']}</h3>
  ) : (
    <DataTable
      autoLayout={true}
      first={pagination.first}
      getPageChange={onLoadPagination}
      onRowClick={event => onSelectObl(event.data)}
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
