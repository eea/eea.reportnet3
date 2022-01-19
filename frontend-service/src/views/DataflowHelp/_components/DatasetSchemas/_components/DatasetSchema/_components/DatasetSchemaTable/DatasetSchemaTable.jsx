import { Fragment, useContext, useRef, useState } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './DatasetSchemaTable.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Chips } from 'views/_components/Chips';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { MultiSelect } from 'views/_components/MultiSelect';

import { RecordUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const DatasetSchemaTable = ({ columnOptions, fields, type }) => {
  const resourcesContext = useContext(ResourcesContext);
  const dataTableRef = useRef();
  const [filters, setFilters] = useState({
    automatic: [],
    entityType: [],
    enabled: [],
    levelError: []
  });
  const [pagination, setPagination] = useState({ first: 0, page: 0, rows: 10 });

  const onFilterChange = (event, field) => {
    dataTableRef.current.filter(event.value, field, 'in');
    const inmFilters = { ...filters };
    inmFilters[field] = event.value;

    setFilters(inmFilters);
  };

  const onPaginate = event => {
    const inmPagination = { first: event.first, page: event.page, rows: event.rows };
    setPagination(inmPagination);
  };

  const filterReferencedFieldAndCodelist = columns => {
    if (type === 'fields') {
      const hasReferencedFields = fields.some(
        field => !isNil(field.referencedField) && !isEmpty(field.referencedField)
      );
      const hasCodelists = fields.some(field => !isNil(field.codelistItems) && !isEmpty(field.codelistItems));
      if (!hasReferencedFields) {
        columns = columns.filter(column => column !== 'referencedField');
      }
      if (!hasCodelists) {
        columns = columns.filter(column => column !== 'codelistItems');
      }
    }
    return columns;
  };

  const getMultiselectFilter = field => {
    if (columnOptions[type]?.filterType?.multiselect[field]) {
      const id = uniqueId();
      return (
        <MultiSelect
          ariaLabelledBy={`${type}_${id}_input`}
          id={`${type}_${id}`}
          inputId={`${type}_${id}_input`}
          itemTemplate={
            !isNil(columnOptions[type]['filterType']['multiselect'][field][0]['class']) &&
            !isNil(columnOptions[type]['filterType']['multiselect'][field][0]['subclass'])
              ? multiselectItemTemplate
              : null
          }
          onChange={e => onFilterChange(e, field)}
          options={columnOptions[type]['filterType']['multiselect'][field]}
          style={{ width: '100%' }}
          value={filters[field]}
        />
      );
    }
  };

  const getTemplate = field => {
    switch (field) {
      case 'type':
        return typeTemplate;
      case 'automatic':
      case 'enabled':
      case 'pk':
      case 'required':
      case 'mandatory':
      case 'prefilled':
      case 'fixedNumber':
      case 'readOnly':
        return rowData => itemTemplate(rowData, field);
      case 'codelistItems':
        return codelistTemplate;
      case 'levelError':
        return levelErrorTemplate;
      case 'referencedField':
        return referencedFieldTemplate;
      case 'operation':
        return operationTemplate;
      default:
        return null;
    }
  };

  const renderColumns = colFields => {
    return colFields.map(colField => (
      <Column
        body={getTemplate(colField)}
        columnResizeMode="expand"
        field={colField}
        filter={!isNil(columnOptions) && !isNil(columnOptions[type]) ? columnOptions[type]['filtered'] : false}
        filterElement={getMultiselectFilter(colField)}
        filterMatchMode="contains"
        header={
          !isNil(columnOptions[type] && columnOptions[type]['names'] && columnOptions[type]['names'][colField])
            ? columnOptions[type]['names'][colField]
            : capitalize(colField)
        }
        key={colField}
        sortable={true}
        style={{
          width: TextUtils.areEquals(colField, 'DESCRIPTION')
            ? '55%'
            : TextUtils.areEquals(colField, 'TYPE')
            ? '25%'
            : TextUtils.areEquals(colField, 'REFERENCEDFIELD') ||
              TextUtils.areEquals(colField, 'OPERATION') ||
              TextUtils.areEquals(colField, 'CODELISTITEMS')
            ? '30%'
            : TextUtils.areEquals(colField, 'PK') ||
              TextUtils.areEquals(colField, 'REQUIRED') ||
              TextUtils.areEquals(colField, 'READONLY')
            ? '15%'
            : '20%',
          display:
            !isNil(columnOptions[type]) &&
            !isNil(columnOptions[type]['invisible']) &&
            columnOptions[type]['invisible'].indexOf(colField) === 0
              ? 'none'
              : 'auto'
        }}
      />
    ));
  };

  const levelErrorTemplate = rowData => {
    if (!isNil(rowData.levelError)) {
      return (
        <div className={styles.levelErrorTemplateWrapper}>
          <span
            className={`${columnOptions['levelErrorTypes']['class']} ${columnOptions['levelErrorTypes']['subClasses']
              .filter(cl => cl.toUpperCase().includes(rowData.levelError.toString().toUpperCase()))
              .join(' ')}`}>
            {rowData.levelError}
          </span>
        </div>
      );
    }
  };

  const multiselectItemTemplate = option => {
    if (!isNil(option.value)) {
      return <span className={`${option.class} ${option.subclass}`}>{option.value}</span>;
    }
  };

  const operationTemplate = rowData => {
    if (!isEmpty(rowData.operation)) {
      return rowData.operation.replaceAll('_', ' ');
    }
  };

  const referencedFieldTemplate = rowData => {
    if (!isNil(rowData?.referencedField) && rowData?.referencedField !== '') {
      return (
        <div>
          <h5>{`${rowData.referencedField?.tableName} - ${rowData.referencedField?.fieldName}`}</h5>
          {rowData.type === 'LINK' && (
            <Fragment>
              <div>
                <span className={styles.propertyValueTableName}>{`${resourcesContext.messages['linkedLabel']}: `}</span>
                <span>{`${rowData.referencedField?.linkedTableLabel ?? '-'}`}</span>
              </div>
              <div>
                <span
                  className={
                    styles.propertyValueTableName
                  }>{`${resourcesContext.messages['linkedConditional']}: `}</span>
                <span>{`${rowData.referencedField?.linkedTableConditional ?? '-'}`}</span>
              </div>
              <div>
                <span
                  className={
                    styles.propertyValueTableName
                  }>{`${resourcesContext.messages['masterConditional']}: `}</span>
                <span>{`${rowData.referencedField?.masterTableConditional ?? '-'}`}</span>
              </div>
            </Fragment>
          )}
          <div>
            <span className={styles.propertyValueTableName}>{resourcesContext.messages['supportsMultipleValues']}</span>
            <FontAwesomeIcon
              aria-label={
                rowData.referencedField?.pkHasMultipleValues
                  ? resourcesContext.messages['yes']
                  : resourcesContext.messages['no']
              }
              icon={AwesomeIcons(rowData.referencedField?.pkHasMultipleValues ? 'check' : 'cross')}
              style={{ float: 'center' }}
            />
          </div>
          <div>
            <span
              className={styles.propertyValueTableName}>{`${resourcesContext.messages['pkValuesMustBeUsed']}?`}</span>
            <FontAwesomeIcon
              aria-label={
                rowData.referencedField?.pkMustBeUsed
                  ? resourcesContext.messages['yes']
                  : resourcesContext.messages['no']
              }
              icon={AwesomeIcons(rowData.referencedField?.pkMustBeUsed ? 'check' : 'cross')}
              style={{ float: 'center' }}
            />
          </div>
        </div>
      );
    } else {
      return '';
    }
  };

  const codelistTemplate = rowData => (
    <Chips
      disabled={true}
      name={resourcesContext.messages['codelistSelectedItems']}
      pasteSeparator=";"
      value={rowData.codelistItems}></Chips>
  );

  const itemTemplate = (rowData, key) => {
    return (
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        {rowData[key] ? (
          <FontAwesomeIcon aria-label={key} icon={AwesomeIcons('check')} style={{ float: 'center' }} />
        ) : null}
      </div>
    );
  };

  const typeTemplate = rowData => {
    return (
      <div style={{ display: 'flex', alignItems: 'baseline' }}>
        <span style={{ margin: '.5em .25em 0 0.5em' }}>{RecordUtils.getFieldTypeValue(rowData.type).value}</span>
        <FontAwesomeIcon
          icon={AwesomeIcons(RecordUtils.getFieldTypeValue(rowData.type).fieldTypeIcon)}
          role="presentation"
          style={{ marginLeft: 'auto' }}
        />
      </div>
    );
  };

  return !isNil(fields) && !isEmpty(fields) ? (
    <DataTable
      getPageChange={onPaginate}
      paginator={true}
      ref={dataTableRef}
      rows={pagination.rows}
      rowsPerPageOptions={[5, 10, 15]}
      style={{
        width: columnOptions[type]['narrow'] ? '25%' : '100%',
        marginTop: '1rem',
        marginBottom: '1rem'
      }}
      summary={resourcesContext.messages['datasetSchema']}
      value={fields}>
      {renderColumns(filterReferencedFieldAndCodelist(columnOptions[type].columns))}
    </DataTable>
  ) : (
    <span className={styles.noRecords}>{resourcesContext.messages['webformTableWithLessRecords']}</span>
  );
};
