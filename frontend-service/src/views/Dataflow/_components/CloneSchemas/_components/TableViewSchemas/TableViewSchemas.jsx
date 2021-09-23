import { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';

import styles from './TableViewSchemas.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'views/_components/Checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const TableViewSchemas = ({
  checkedDataflow,
  data,
  handleRedirect,
  isReferenceDataflow,
  onChangePagination,
  onSelectDataflow,
  pagination,
  paginatorRightText
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const fieldTables = {
    expirationDate: resourcesContext.messages['nextReportDue'],
    obligationTitle: resourcesContext.messages['obligation'],
    legalInstrument: resourcesContext.messages['legalInstrument']
  };

  const getOrderedFields = dataflows => {
    const dataflowsWithPriority = isReferenceDataflow
      ? [
          { id: 'name', index: 0 },
          { id: 'description', index: 1 },
          { id: 'status', index: 2 }
        ]
      : [
          { id: 'name', index: 0 },
          { id: 'description', index: 1 },
          { id: 'obligationTitle', index: 2 },
          { id: 'legalInstrument', index: 3 },
          { id: 'status', index: 4 },
          { id: 'expirationDate', index: 5 }
        ];

    return dataflows
      .map(field => dataflowsWithPriority.filter(e => field === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedField => orderedField.id);
  };

  const headerTableTemplate = field => fieldTables[field] || resourcesContext.messages[field];

  const onLoadCheckButton = row => {
    return (
      <div className={styles.checkColumn}>
        <Checkbox
          ariaLabel={resourcesContext.messages['selectedDataflow']}
          checked={checkedDataflow.id === row.id}
          id={`${row.id}_checkbox`}
          inputId={`${row.id}_checkbox`}
          onChange={() => onSelectDataflow(row)}
          role="checkbox"
        />
      </div>
    );
  };

  const onLoadPagination = event => onChangePagination({ first: event.first, rows: event.rows, page: event.page });

  const onLoadTitleTemplate = row => (
    <div className={styles.titleColum}>
      {row.name}
      <FontAwesomeIcon
        aria-label={resourcesContext.messages['goToLink']}
        className={styles.linkIcon}
        icon={AwesomeIcons('externalUrl')}
        onMouseDown={() => handleRedirect(row.id)}
      />
    </div>
  );

  const renderCheckColumn = (
    <Column
      body={row => onLoadCheckButton(row)}
      className={styles.emptyTableHeader}
      header={resourcesContext.messages['selectedDataflow']}
      key="checkId"
    />
  );

  const renderColumns = dataflows => {
    const fieldColumns = getOrderedFields(Object.keys(dataflows[0])).map(field => {
      let template = null;
      if (field === 'name') template = onLoadTitleTemplate;

      return <Column body={template} field={field} header={headerTableTemplate(field)} key={field} sortable={true} />;
    });

    fieldColumns.unshift(renderCheckColumn);

    return fieldColumns;
  };

  return isEmpty(data) ? (
    <h3 className={styles.noDataflows}>{resourcesContext.messages['noDataflowsWithSelectedParameters']}</h3>
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
