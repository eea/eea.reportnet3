import { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import styles from './TableView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { RodUrl } from 'repositories/config/RodUrl';

export const TableView = ({
  checkedObligation,
  data,
  onSelectObligation,
  onChangePagination,
  pagination,
  paginatorRightText
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

  const onLoadCheckButton = row => (
    <div className={styles.checkColumn}>
      <Checkbox
        ariaLabel={resourcesContext.messages['selectedObligation']}
        checked={checkedObligation.id === row.id}
        id={`${row.id}_checkbox`}
        inputId={`${row.id}_checkbox`}
        onChange={() => onSelectObligation(row)}
        role="checkbox"
      />
    </div>
  );

  const onLoadTitleTemplate = row => (
    <div className={styles.titleColumn}>
      {row.title}
      <FontAwesomeIcon
        aria-hidden={false}
        className={styles.linkIcon}
        icon={AwesomeIcons('externalUrl')}
        onMouseDown={() => window.open(`${RodUrl.obligations}${row.id}`)}
      />
    </div>
  );

  const getColumns = () => {
    const columns = [
      {
        key: 'checkId',
        template: row => onLoadCheckButton(row)
      },
      {
        key: 'title',
        header: resourcesContext.messages['title'],
        template: onLoadTitleTemplate
      },
      {
        key: 'legalInstrument',
        header: resourcesContext.messages['legalInstrument']
      },
      {
        key: 'dueDate',
        header: resourcesContext.messages['nextReportDue']
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'checkId'}
      />
    ));
  };

  const renderObligations = () => {
    if (isEmpty(data)) {
      return (
        <h3 className={styles.noObligations}>{resourcesContext.messages['noObligationsWithSelectedParameters']}</h3>
      );
    }

    return (
      <DataTable
        autoLayout={true}
        className={styles.cursorPointer}
        first={pagination.first}
        getPageChange={onLoadPagination}
        onRowClick={event => onSelectObligation(event.data)}
        paginator={true}
        paginatorRight={paginatorRightText}
        rows={pagination.rows}
        rowsPerPageOptions={[5, 10, 15]}
        summary={resourcesContext.messages['reportingObligations']}
        totalRecords={data.length}
        value={data}>
        {getColumns()}
      </DataTable>
    );
  };

  return renderObligations();
};
