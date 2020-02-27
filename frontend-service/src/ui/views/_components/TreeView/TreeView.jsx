import React, { useEffect, useReducer, useRef } from 'react';

import { capitalize, isUndefined, isNull } from 'lodash';

import styles from './TreeView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { MultiSelect } from 'primereact/multiselect';
import { TreeViewExpandableItem } from './_components/TreeViewExpandableItem';

import { treeViewReducer } from './_functions/Reducers/treeViewReducer';

const TreeView = ({ columnOptions = {}, property, propertyName, rootProperty }) => {
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
      console.log(treeViewState.filters[field], columnOptions[propertyName]['filterType']['multiselect'][field]);
      return (
        <MultiSelect
          style={{ width: '100%' }}
          value={treeViewState.filters[field]}
          options={columnOptions[propertyName]['filterType']['multiselect'][field]}
          onChange={e => onFilterChange(e, field)}
        />
      );
    }
  };

  const groupFields = fields => {
    parseData(fields);
    if (!isUndefined(fields) && !isNull(fields) && fields.length > 0) {
      return (
        <DataTable ref={dataTableRef} style={{ width: '100%', marginTop: '1rem', marginBottom: '1rem' }} value={fields}>
          {renderColumns(fields)}
        </DataTable>
      );
    } else {
      return null;
    }
  };

  const parseData = fieldsDTO => {
    fieldsDTO.forEach(fieldDTO => {
      for (let [key, value] of Object.entries(fieldDTO)) {
        if (typeof value !== 'string') {
          fieldDTO[key] = value.toString();
        }
      }
    });
  };

  const renderColumns = fields =>
    Object.keys(fields[0]).map(field => (
      <Column
        body={field === 'type' ? typeTemplate : field === 'automatic' || field === 'enabled' ? automaticTemplate : null}
        key={field}
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
        sortable={true}
        style={{
          width: field.toUpperCase() === 'DESCRIPTION' ? '60%' : '20%',
          display:
            !isUndefined(columnOptions[propertyName]) &&
            !isUndefined(columnOptions[propertyName]['invisible']) &&
            columnOptions[propertyName]['invisible'].indexOf(field) === 0
              ? 'none'
              : 'auto'
        }}
      />
    ));

  return (
    <React.Fragment>
      {!isUndefined(property) && !isNull(property) ? (
        <div
          style={{
            paddingTop: '6px',
            paddingLeft: '3px',
            marginLeft: '10px'
          }}>
          {typeof property === 'number' || typeof property === 'string' || typeof property === 'boolean' ? (
            <React.Fragment>
              <span className={styles.propertyTitle}>
                {!Number.isInteger(Number(propertyName)) ? `${camelCaseToNormal(propertyName)}: ` : ''}
              </span>
              {property !== '' ? <span className={styles.propertyValue}>{property.toString()}</span> : '-'}
            </React.Fragment>
          ) : (
            <TreeViewExpandableItem
              items={!Number.isInteger(Number(propertyName)) ? [{ label: camelCaseToNormal(propertyName) }] : []}
              expanded={true}>
              {!isUndefined(columnOptions[propertyName]) &&
              !isUndefined(columnOptions[propertyName]['groupable']) &&
              columnOptions[propertyName]['groupable']
                ? groupFields(property)
                : !isUndefined(property)
                ? Object.values(property).map((proper, index, { length }) => (
                    <TreeView
                      columnOptions={columnOptions}
                      excludeBottomBorder={index === length - 1}
                      key={index}
                      property={proper}
                      propertyName={Object.getOwnPropertyNames(property)[index]}
                    />
                  ))
                : null}
            </TreeViewExpandableItem>
          )}
        </div>
      ) : null}
    </React.Fragment>
  );
};

const automaticTemplate = rowData => (
  <div>
    {rowData.automatic || rowData.enabled ? (
      <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--main-color-font)' }} />
    ) : null}
  </div>
);

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const getFieldTypeValue = value => {
  const fieldTypes = [
    { fieldType: 'Number', value: 'Number', fieldTypeIcon: 'number' },
    { fieldType: 'Date', value: 'Date', fieldTypeIcon: 'calendar' },
    { fieldType: 'Latitude', value: 'Geospatial object (Latitude)', fieldTypeIcon: 'map' },
    { fieldType: 'Longitude', value: 'Geospatial object (Longitude)', fieldTypeIcon: 'map' },
    { fieldType: 'Text', value: 'Single line text', fieldTypeIcon: 'italic' },
    { fieldType: 'Boolean', value: 'Boolean', fieldTypeIcon: 'boolean' },
    { fieldType: 'Point', value: 'Point', fieldTypeIcon: 'point' },
    { fieldType: 'Circle', value: 'Circle', fieldTypeIcon: 'circle' },
    { fieldType: 'Polygon', value: 'Polygon', fieldTypeIcon: 'polygon' },
    { fieldType: 'Codelist', value: 'Codelist', fieldTypeIcon: 'list' }
  ];

  if (value.toUpperCase() === 'COORDINATE_LONG') {
    value = 'Longitude';
  }
  if (value.toUpperCase() === 'COORDINATE_LAT') {
    value = 'Latitude';
  }
  return fieldTypes.filter(field => field.fieldType.toUpperCase() === value.toUpperCase())[0];
};

const getInitialFilter = (columnOptions, field) =>
  !isUndefined(columnOptions.validations) &&
  !isUndefined(columnOptions.validations.filterType) &&
  !isUndefined(columnOptions.validations.filterType.multiselect)
    ? columnOptions.validations.filterType.multiselect[field]
    : [];

const typeTemplate = (rowData, column) => {
  return (
    <div>
      <span style={{ margin: '.5em .25em 0 0.5em' }}>{getFieldTypeValue(rowData.type).value}</span>
      <FontAwesomeIcon
        icon={AwesomeIcons(getFieldTypeValue(rowData.type).fieldTypeIcon)}
        style={{ float: 'right', color: 'var(--treeview-table-icon-color)' }}
      />
    </div>
  );
};

export { TreeView };
