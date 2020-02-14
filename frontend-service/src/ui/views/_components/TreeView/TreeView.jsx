import React from 'react';

import { capitalize, isUndefined, isNull } from 'lodash';

import styles from './TreeView.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { TreeViewExpandableItem } from './_components/TreeViewExpandableItem';

const TreeView = ({ groupableProperties = [], propertyName, property, rootProperty }) => {
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
              {groupableProperties.indexOf(propertyName) > -1
                ? groupFields(property)
                : !isUndefined(property)
                ? Object.values(property).map((proper, index, { length }) => (
                    <TreeView
                      key={index}
                      property={proper}
                      propertyName={Object.getOwnPropertyNames(property)[index]}
                      excludeBottomBorder={index === length - 1}
                      groupableProperties={groupableProperties}
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

const groupFields = fields => {
  if (!isUndefined(fields) && !isNull(fields) && fields.length > 0) {
    return (
      <DataTable value={fields} style={{ width: '100%', marginTop: '1rem', marginBottom: '1rem' }}>
        {renderColumns(fields)}
      </DataTable>
    );
  } else {
    return null;
  }
};

const renderColumns = fields =>
  Object.keys(fields[0]).map(field => (
    <Column
      body={field === 'type' ? typeTemplate : null}
      key={field}
      columnResizeMode="expand"
      field={field}
      filter={false}
      filterMatchMode="contains"
      header={capitalize(field)}
      sortable={true}
      style={{ width: field.toUpperCase() === 'DESCRIPTION' ? '60%' : '20%' }}
    />
  ));

const typeTemplate = (rowData, column) => {
  return (
    <div>
      <span style={{ margin: '.5em .25em 0 0.5em' }}>{getFieldTypeValue(rowData.type).value}</span>
      <FontAwesomeIcon icon={AwesomeIcons(getFieldTypeValue(rowData.type).fieldTypeIcon)} style={{ float: 'right' }} />
    </div>
  );
};

export { TreeView };
