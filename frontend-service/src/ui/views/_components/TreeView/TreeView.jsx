import { Fragment, useReducer, useRef } from 'react';

import uuid from 'uuid';

import capitalize from 'lodash/capitalize';
import isNull from 'lodash/isNull';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniqueId from 'lodash/uniqueId';

import styles from './TreeView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Chips } from 'ui/views/_components/Chips';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { MultiSelect } from 'ui/views/_components/MultiSelect';
import { TreeViewExpandableItem } from './_components/TreeViewExpandableItem';

import { treeViewReducer } from './_functions/Reducers/treeViewReducer';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const TreeView = ({ className = '', columnOptions = {}, expandAll = true, property, propertyName }) => {
  const dataTableRef = useRef();
  const initialTreeViewState = {
    filters: {
      automatic: [],
      entityType: [],
      enabled: [],
      levelError: []
    },
    options: {}
  };

  const [treeViewState, dispatchTreeView] = useReducer(treeViewReducer, initialTreeViewState);

  const onFilterChange = (event, field) => {
    dataTableRef.current.filter(event.value, field, 'in');
    dispatchTreeView({ type: 'SET_FILTER', payload: { value: event.value, field } });
  };

  const getMultiselectFilter = field => {
    if (
      !isUndefined(columnOptions[propertyName]['filterType']) &&
      !isUndefined(columnOptions[propertyName]['filterType']['multiselect']) &&
      !isUndefined(columnOptions[propertyName]['filterType']['multiselect'][field])
    ) {
      const id = uniqueId();
      return (
        <MultiSelect
          ariaLabelledBy={`${propertyName}_${id}_input`}
          id={`${propertyName}_${id}`}
          inputId={`${propertyName}_${id}_input`}
          itemTemplate={
            !isUndefined(columnOptions[propertyName]['filterType']['multiselect'][field][0]['class']) &&
            !isUndefined(columnOptions[propertyName]['filterType']['multiselect'][field][0]['subclass'])
              ? multiselectItemTemplate
              : null
          }
          onChange={e => onFilterChange(e, field)}
          options={columnOptions[propertyName]['filterType']['multiselect'][field]}
          style={{ width: '100%' }}
          value={treeViewState.filters[field]}
          valuesSeparator=";"
        />
      );
    }
  };

  const groupFields = fields => {
    parseData(fields);
    if (!isUndefined(fields) && !isNull(fields) && fields.length > 0) {
      return (
        <DataTable
          ref={dataTableRef}
          style={{
            width: columnOptions[propertyName]['narrow'] ? '25%' : '100%',
            marginTop: '1rem',
            marginBottom: '1rem'
          }}
          value={fields}>
          {renderColumns(fields)}
        </DataTable>
      );
    } else {
      return null;
    }
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

  const referencedFieldTemplate = rowData => {
    if (!isNil(rowData?.referencedField) && rowData?.referencedField !== '') {
      return (
        <div>
          <h5>{`${rowData.referencedField?.tableName} - ${rowData.referencedField?.fieldName}`}</h5>
          {rowData.type === 'LINK' && (
            <Fragment>
              <div>
                <span className={styles.propertyValueTableName}>{`Linked label: `}</span>
                <span>{`${rowData.referencedField?.linkedTableLabel ?? '-'}`}</span>
              </div>
              <div>
                <span className={styles.propertyValueTableName}>{`Linked conditional: `}</span>
                <span>{`${rowData.referencedField?.linkedTableConditional ?? '-'}`}</span>
              </div>
              <div>
                <span className={styles.propertyValueTableName}>{`Master conditional: `}</span>
                <span>{`${rowData.referencedField?.masterTableConditional ?? '-'}`}</span>
              </div>
            </Fragment>
          )}
          <div>
            <span className={styles.propertyValueTableName}>{`Supports multiple values?`}</span>
            <FontAwesomeIcon
              aria-label={rowData.referencedField?.pkHasMultipleValues ? 'True' : 'False'}
              icon={AwesomeIcons(rowData.referencedField?.pkHasMultipleValues ? 'check' : 'cross')}
              style={{ float: 'center', color: 'var(--treeview-table-icon-color)' }}
            />
          </div>
          <div>
            <span className={styles.propertyValueTableName}>{`All PK values must be used on link?`}</span>
            <FontAwesomeIcon
              aria-label={rowData.referencedField?.pkMustBeUsed ? 'True' : 'False'}
              icon={AwesomeIcons(rowData.referencedField?.pkMustBeUsed ? 'check' : 'cross')}
              style={{ float: 'center', color: 'var(--treeview-table-icon-color)' }}
            />
          </div>
        </div>
      );
    } else {
      return '';
    }
  };

  const parseData = fieldsDTO => {
    fieldsDTO.forEach(fieldDTO => {
      for (let [key, value] of Object.entries(fieldDTO)) {
        if (typeof value === 'boolean' && !isUndefined(value)) {
          fieldDTO[key] = value.toString();
        }
      }
    });
  };

  const renderColumns = fields =>
    Object.keys(fields[0]).map(field => (
      <Column
        body={
          field === 'type'
            ? typeTemplate
            : field === 'automatic'
            ? rowData => itemTemplate(rowData, 'automatic')
            : field === 'enabled'
            ? rowData => itemTemplate(rowData, 'enabled')
            : field === 'codelistItems'
            ? codelistTemplate
            : field === 'pk'
            ? rowData => itemTemplate(rowData, 'pk')
            : field === 'required'
            ? rowData => itemTemplate(rowData, 'required')
            : field === 'mandatory'
            ? rowData => itemTemplate(rowData, 'mandatory')
            : field === 'prefilled'
            ? rowData => itemTemplate(rowData, 'prefilled')
            : field === 'fixedNumber'
            ? rowData => itemTemplate(rowData, 'fixedNumber')
            : field === 'readOnly'
            ? rowData => itemTemplate(rowData, 'readOnly')
            : field === 'levelError'
            ? levelErrorTemplate
            : field === 'referencedField'
            ? referencedFieldTemplate
            : null
        }
        columnResizeMode="expand"
        field={field}
        filter={
          !isUndefined(columnOptions) && !isUndefined(columnOptions[propertyName])
            ? columnOptions[propertyName]['filtered']
            : false
        }
        filterElement={getMultiselectFilter(field)}
        filterMatchMode="contains"
        header={
          !isUndefined(
            columnOptions[propertyName] &&
              columnOptions[propertyName]['names'] &&
              columnOptions[propertyName]['names'][field]
          )
            ? columnOptions[propertyName]['names'][field]
            : capitalize(field)
        }
        key={field}
        sortable={true}
        style={{
          width: TextUtils.areEquals(field, 'DESCRIPTION')
            ? '55%'
            : TextUtils.areEquals(field, 'TYPE')
            ? '25%'
            : TextUtils.areEquals(field, 'REFERENCEDFIELD')
            ? '30%'
            : '20%',
          display:
            !isUndefined(columnOptions[propertyName]) &&
            !isUndefined(columnOptions[propertyName]['invisible']) &&
            columnOptions[propertyName]['invisible'].indexOf(field) === 0
              ? 'none'
              : 'auto'
        }}
      />
    ));

  const multiselectItemTemplate = option => {
    if (!isNil(option.value)) {
      return <span className={`${option.class} ${option.subclass}`}>{option.value}</span>;
    }
  };

  return (
    !isUndefined(property) &&
    !isNull(property) && (
      <div
        style={{
          paddingTop: '12px',
          paddingLeft: '3px',
          marginLeft: '10px'
        }}>
        {typeof property === 'number' || typeof property === 'string' || typeof property === 'boolean' ? (
          <Fragment>
            <span className={styles.propertyTitle}>
              {!Number.isInteger(Number(propertyName)) ? `${camelCaseToNormal(propertyName)}: ` : ''}
            </span>
            {property !== '' ? (
              <span
                className={`${styles.propertyValue} ${className} ${
                  propertyName === 'tableSchemaName' ? styles.propertyValueTableName : ''
                }`}>
                {property.toString()}
              </span>
            ) : (
              '-'
            )}
          </Fragment>
        ) : (
          <TreeViewExpandableItem
            expanded={expandAll}
            items={!Number.isInteger(Number(propertyName)) ? [{ label: camelCaseToNormal(propertyName) }] : []}>
            {!isUndefined(columnOptions[propertyName]) &&
            !isUndefined(columnOptions[propertyName]['groupable']) &&
            columnOptions[propertyName]['groupable']
              ? groupFields(property)
              : !isUndefined(property)
              ? Object.values(property).map((proper, index, { length }) => (
                  <TreeView
                    className={
                      !isUndefined(columnOptions[propertyName]) &&
                      columnOptions[propertyName]['hasClass'] &&
                      columnOptions[propertyName]['subClasses']
                        ? `${columnOptions[propertyName]['class']} ${columnOptions[propertyName]['subClasses']
                            .filter(cl => cl.toUpperCase().includes(proper.toString().toUpperCase()))
                            .join(' ')}`
                        : ''
                    }
                    columnOptions={columnOptions}
                    excludeBottomBorder={index === length - 1}
                    expandAll={expandAll}
                    key={uuid.v4()}
                    property={proper}
                    propertyName={Object.getOwnPropertyNames(property)[index]}
                  />
                ))
              : null}
          </TreeViewExpandableItem>
        )}
      </div>
    )
  );
};

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const codelistTemplate = rowData => (
  <Chips disabled={true} name="Multiple/single selected items" pasteSeparator=";" value={rowData.codelistItems}></Chips>
);

const getFieldTypeValue = value => {
  const fieldTypes = [
    { fieldType: 'Number_Integer', value: 'Number - Integer', fieldTypeIcon: 'number-integer' },
    { fieldType: 'Number_Decimal', value: 'Number - Decimal', fieldTypeIcon: 'number-decimal' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    { fieldType: 'Datetime', value: 'Datetime', fieldTypeIcon: 'clock' },
    { fieldType: 'Text', value: 'Text', fieldTypeIcon: 'italic' },
    // { fieldType: 'Rich_Text', value: 'Rich text', fieldTypeIcon: 'align-right' },
    { fieldType: 'Textarea', value: 'Multiline text', fieldTypeIcon: 'align-right' },
    { fieldType: 'Email', value: 'Email', fieldTypeIcon: 'email' },
    { fieldType: 'URL', value: 'URL', fieldTypeIcon: 'url' },
    { fieldType: 'Phone', value: 'Phone number', fieldTypeIcon: 'mobile' },
    // { fieldType: 'Boolean', value: 'Boolean', fieldTypeIcon: 'boolean' },
    { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
    { fieldType: 'MultiPoint', value: 'Multiple points', fieldTypeIcon: 'multiPoint' },
    { fieldType: 'Linestring', value: 'Line', fieldTypeIcon: 'line' },
    { fieldType: 'MultiLineString', value: 'Multiple lines', fieldTypeIcon: 'multiLineString' },
    { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
    { fieldType: 'MultiPolygon', value: 'Multiple polygons', fieldTypeIcon: 'multiPolygon' },
    // { fieldType: 'Circle', value: 'Circle', fieldTypeIcon: 'circle' },
    { fieldType: 'Codelist', value: 'Single select', fieldTypeIcon: 'list' },
    { fieldType: 'Multiselect_Codelist', value: 'Multiple select', fieldTypeIcon: 'multiselect' },
    { fieldType: 'Link', value: 'Link', fieldTypeIcon: 'link' },
    { fieldType: 'External_link', value: 'External link', fieldTypeIcon: 'externalLink' },
    { fieldType: 'Attachment', value: 'Attachment', fieldTypeIcon: 'clip' }
  ];
  return fieldTypes.filter(field => TextUtils.areEquals(field.fieldType, value))[0];
};

const itemTemplate = (rowData, key) => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center' }}>
      {rowData[key] === 'true' ? (
        <FontAwesomeIcon
          aria-label={key}
          icon={AwesomeIcons('check')}
          style={{ float: 'center', color: 'var(--treeview-table-icon-color)' }}
        />
      ) : null}
    </div>
  );
};

const typeTemplate = rowData => {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline' }}>
      <span style={{ margin: '.5em .25em 0 0.5em' }}>{getFieldTypeValue(rowData.type).value}</span>
      <FontAwesomeIcon
        icon={AwesomeIcons(getFieldTypeValue(rowData.type).fieldTypeIcon)}
        role="presentation"
        style={{ marginLeft: 'auto', color: 'var(--treeview-table-icon-color)' }}
      />
    </div>
  );
};

export { TreeView };
