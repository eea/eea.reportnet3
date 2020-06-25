import React, { useContext } from 'react';
import { withRouter } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import { routes } from 'ui/routes';
import { getUrl } from 'core/infrastructure/CoreUtils';

import styles from './TableViewSchemas.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const TableViewSchemas = withRouter(
  ({ data, history, onChangePagination, onSelectDataflow, pagination, selectedDataflowId }) => {
    const resources = useContext(ResourcesContext);

    const headerTableTemplate = dataflow => {
      if (dataflow === 'expirationDate') return resources.messages['nextReportDue'];
      else if (dataflow === 'obligation') return resources.messages['obligationTitle'];
      else return resources.messages[dataflow];
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

    // const onLoadLegalInstrumentTemplate = row => (
    //   <div className={styles.titleColum}>{row.obligation.legalInstruments.title}</div>
    // );

    const onLoadObligationTemplate = row => {
      console.log('row', row);
      return <div className={styles.titleColum}>{row.obligation.title}</div>;
    };

    const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

    const onLoadTitleTemplate = row => {
      return (
        <div className={styles.titleColum}>
          {row.name}
          <FontAwesomeIcon
            className={styles.linkIcon}
            icon={AwesomeIcons('externalLink')}
            onMouseDown={() => window.open(getUrl(`/dataflow/${row.id}`))}
            // onMouseDown={() => window.history.push(getUrl(routes.DATAFLOW, { selectedRowId }))}
          />
        </div>
      );
    };

    const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

    const renderColumns = dataflows => {
      console.log('dataflows', dataflows);
      console.log('Object.keys(dataflows[0]', Object.keys(dataflows[0]));
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
          // if (field.obligation === 'legalInstruments') template = onLoadLegalInstrumentTemplate;

          return (
            <Column body={template} field={field} header={headerTableTemplate(field)} key={field} sortable={true} />
          );
        });

      fieldColumns.unshift(renderCheckColum);

      console.log('fieldColumns', fieldColumns);
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
  }
);
