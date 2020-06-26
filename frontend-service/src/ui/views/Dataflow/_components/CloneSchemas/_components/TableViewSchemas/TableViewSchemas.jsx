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
  data,
  handleRedirect,
  onChangePagination,
  onSelectDataflow,
  pagination,
  selectedDataflowId
}) => {
  const resources = useContext(ResourcesContext);

  const headerTableTemplate = field => {
    if (field === 'expirationDate') return resources.messages['nextReportDue'];
    else if (field === 'obligation') return resources.messages['obligationTitle'];
    else if (field === 'legalInstruments') return resources.messages['legalInstrument'];
    else return resources.messages[field];
  };

  const onLoadCheckButton = row => {
    return (
      <div className={styles.checkColum}>
        <Checkbox
          id={`${row.id}_checkbox`}
          isChecked={selectedDataflowId === row.id}
          onChange={() => onSelectDataflow(row)}
          role="checkbox"
        />
      </div>
    );
  };

  const onLoadLegalInstrumentTemplate = row => (
    <div className={styles.titleColum}>{row.obligation.legalInstruments.alias}</div>
  );

  const onLoadObligationTemplate = row => <div className={styles.titleColum}>{row.obligation.title}</div>;

  const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

  const onLoadTitleTemplate = row => {
    return (
      <div className={styles.titleColum}>
        {row.name}
        <FontAwesomeIcon
          className={styles.linkIcon}
          icon={AwesomeIcons('externalLink')}
          onMouseDown={() => handleRedirect(row.id)}
        />
      </div>
    );
  };

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = dataflows => {
    const fieldColumns = Object.keys(dataflows[0])
      .filter(
        key =>
          key.includes('name') ||
          key.includes('expirationDate') ||
          key.includes('description') ||
          key.includes('status') ||
          key.includes('userRole') ||
          key.includes('obligation')
      )
      .map(field => {
        let template = null;
        if (field === 'name') template = onLoadTitleTemplate;
        if (field === 'obligation') template = onLoadObligationTemplate;

        return <Column body={template} field={field} header={headerTableTemplate(field)} key={field} sortable={true} />;
      });

    const legalFieldColumn = Object.values(dataflows[0]).filter(key => typeof key === 'object');

    const legalInstrument = Object.keys(legalFieldColumn[0])
      .filter(key => key.includes('legalInstruments'))
      .map(field => {
        let template = null;
        if (field === 'legalInstruments') template = onLoadLegalInstrumentTemplate;

        return <Column body={template} field={field} header={headerTableTemplate(field)} key={field} sortable={true} />;
      });

    fieldColumns.push(legalInstrument);
    fieldColumns.unshift(renderCheckColum);

    return fieldColumns;
  };

  const paginatorRightText = `${resources.messages['totalDataflows']}: ${data.length}`;

  return isEmpty(data) ? (
    <h3 className={styles.noObligations}>{resources.messages['noObligationsWithSelectedParameters']}</h3>
  ) : (
    <DataTable
      autoLayout={true}
      first={pagination.first}
      getPageChange={onLoadPagination}
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
