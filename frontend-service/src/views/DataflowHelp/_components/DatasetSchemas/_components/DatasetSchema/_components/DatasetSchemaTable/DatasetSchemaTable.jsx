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
  console.log({ fields });

  const onFilterChange = (event, field) => {
    dataTableRef.current.filter(event.value, field, 'in');
    const inmFilters = { ...filters };
    inmFilters[field] = event.value;

    setFilters(inmFilters);
  };

  const getFieldTypeValue = value => {
    const fieldTypes = [
      { fieldType: 'Number_Integer', value: 'Number - Integer', fieldTypeIcon: 'number-integer' },
      { fieldType: 'Number_Decimal', value: 'Number - Decimal', fieldTypeIcon: 'number-decimal' },
      { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
      { fieldType: 'Datetime', value: 'Datetime', fieldTypeIcon: 'clock' },
      { fieldType: 'Text', value: 'Text', fieldTypeIcon: 'italic' },
      { fieldType: 'Textarea', value: 'Multiline text', fieldTypeIcon: 'align-right' },
      { fieldType: 'Email', value: 'Email', fieldTypeIcon: 'email' },
      { fieldType: 'URL', value: 'URL', fieldTypeIcon: 'url' },
      { fieldType: 'Phone', value: 'Phone number', fieldTypeIcon: 'mobile' },
      { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
      { fieldType: 'MultiPoint', value: 'Multiple points', fieldTypeIcon: 'multiPoint' },
      { fieldType: 'Linestring', value: 'Line', fieldTypeIcon: 'line' },
      { fieldType: 'MultiLineString', value: 'Multiple lines', fieldTypeIcon: 'multiLineString' },
      { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
      { fieldType: 'MultiPolygon', value: 'Multiple polygons', fieldTypeIcon: 'multiPolygon' },
      { fieldType: 'Codelist', value: 'Single select', fieldTypeIcon: 'list' },
      { fieldType: 'Multiselect_Codelist', value: 'Multiple select', fieldTypeIcon: 'multiselect' },
      { fieldType: 'Link', value: 'Link', fieldTypeIcon: 'link' },
      { fieldType: 'External_link', value: 'External link', fieldTypeIcon: 'externalLink' },
      { fieldType: 'Attachment', value: 'Attachment', fieldTypeIcon: 'clip' }
    ];
    return fieldTypes.filter(field => TextUtils.areEquals(field.fieldType, value))[0];
  };

  const getMultiselectFilter = field => {
    // console.log(columnOptions);
    if (
      !isNil(columnOptions[type]['filterType']) &&
      !isNil(columnOptions[type]['filterType']['multiselect']) &&
      !isNil(columnOptions[type]['filterType']['multiselect'][field])
    ) {
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

  const renderColumns = fields =>
    fields.map(field => (
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
        filter={!isNil(columnOptions) && !isNil(columnOptions[type]) ? columnOptions[type]['filtered'] : false}
        filterElement={getMultiselectFilter(field)}
        filterMatchMode="contains"
        header={
          !isNil(columnOptions[type] && columnOptions[type]['names'] && columnOptions[type]['names'][field])
            ? columnOptions[type]['names'][field]
            : capitalize(field)
        }
        key={field}
        sortable={true}
        style={{
          width: TextUtils.areEquals(field, 'DESCRIPTION')
            ? '55%'
            : TextUtils.areEquals(field, 'TYPE')
            ? '25%'
            : TextUtils.areEquals(field, 'REFERENCEDFIELD') || TextUtils.areEquals(field, 'OPERATION')
            ? '30%'
            : '20%',
          display:
            !isNil(columnOptions[type]) &&
            !isNil(columnOptions[type]['invisible']) &&
            columnOptions[type]['invisible'].indexOf(field) === 0
              ? 'none'
              : 'auto'
        }}
      />
    ));

  const levelErrorTemplate = rowData => {
    if (!isNil(rowData.levelError)) {
      return (
        <div className={styles.levelErrorTemplateWrapper}>
          <span
          // className={`${columnOptions['levelErrorTypes']['class']} ${columnOptions['levelErrorTypes']['subClasses']
          //   .filter(cl => cl.toUpperCase().includes(rowData.levelError.toString().toUpperCase()))
          //   .join(' ')}`}
          >
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

  const referencedFieldTemplate = rowData => {
    console.log(rowData?.referencedField);
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
              style={{ float: 'center' }}
            />
          </div>
          <div>
            <span className={styles.propertyValueTableName}>{`All PK values must be used on link?`}</span>
            <FontAwesomeIcon
              aria-label={rowData.referencedField?.pkMustBeUsed ? 'True' : 'False'}
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
      name="Multiple/single selected items"
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
        <span style={{ margin: '.5em .25em 0 0.5em' }}>{getFieldTypeValue(rowData.type).value}</span>
        <FontAwesomeIcon
          icon={AwesomeIcons(getFieldTypeValue(rowData.type).fieldTypeIcon)}
          role="presentation"
          style={{ marginLeft: 'auto' }}
        />
      </div>
    );
  };
  // console.log(`fields`, fields);
  return !isNil(fields) ? (
    <DataTable
      ref={dataTableRef}
      style={{
        width: columnOptions[type]['narrow'] ? '25%' : '100%',
        marginTop: '1rem',
        marginBottom: '1rem'
      }}
      value={fields}>
      {renderColumns(columnOptions[type].columns)}
    </DataTable>
  ) : (
    <span>{resourcesContext.messages['webformTableWithLessRecords']}</span>
  );
};
