import React, { useState } from 'react';

import styles from './TreeView.module.css';

import { capitalize, isUndefined, isNull, isEmpty } from 'lodash';

// import { CSSTransition } from 'react-transition-group';

import { TreeViewExpandableItem } from './_components/TreeViewExpandableItem';

const TreeView = ({ groupableProperties = [], propertyName, property, rootProperty }) => {
  return (
    <React.Fragment>
      {!isUndefined(property) && !isNull(property) ? (
        <div
          style={{
            paddingTop: '6px',
            paddingLeft: '3px',
            marginLeft: '10px',
            color: '#666'
          }}>
          {typeof property === 'number' || typeof property === 'string' || typeof property === 'boolean' ? (
            <React.Fragment>
              <span style={{ color: 'black', fontSize: '14px', fontWeight: 'bold' }}>
                {!Number.isInteger(Number(propertyName)) ? `${camelCaseToNormal(propertyName)}: ` : ''}
              </span>
              {property.toString()}
            </React.Fragment>
          ) : (
            <TreeViewExpandableItem
              title={!Number.isInteger(Number(propertyName)) ? camelCaseToNormal(propertyName) : ''}
              expanded={true}>
              {console.log(property, propertyName, groupableProperties)}

              {groupableProperties.indexOf(propertyName.toLowerCase()) > -1
                ? groupFields(property)
                : Object.values(property).map((proper, index, { length }) => (
                    <TreeView
                      key={index}
                      property={proper}
                      propertyName={Object.getOwnPropertyNames(property)[index]}
                      excludeBottomBorder={index === length - 1}
                      groupableProperties={groupableProperties}
                    />
                  ))}
            </TreeViewExpandableItem>
          )}
        </div>
      ) : null}
    </React.Fragment>
  );
};

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const groupFields = fields => {
  console.log(fields);
  if (!isUndefined(fields) && !isNull(fields) && fields.length > 0) {
    return (
      <DataTable autoLayout={true} paginator={true} rows={10} rowsPerPageOptions={[5, 10, 100]} value={fields}>
        {renderColumns()}
      </DataTable>

      // <CSSTransition key={0} classNames="slider" timeout={{ enter: 500, exit: 300 }}>
      // <table className={styles.fieldsTable}>
      //   <thead>
      //     <tr>
      //       {renderColumns()}
      //     </tr>
      //     {/* fields.map(field=>{})<tr>{data.titles}</tr> */}
      //   </thead>
      //   <tbody>
      //     {fields.map((field, i) => {
      //       return (
      //         <tr key={i}>
      //           {Object.values(field).map((fieldElmt, i) => {
      //             return <td key={i}>{fieldElmt}</td>;
      //           })}
      //         </tr>
      //       );
      //     })}
      //   </tbody>
      // </table>
      // </CSSTransition>
    );
  } else {
    return null;
  }
};

const renderColumns = () => {
  Object.keys(fields[0]).map(field => {
    return (
      <Column
        key={field}
        columnResizeMode="expand"
        field={field}
        filter={false}
        filterMatchMode="contains"
        header={capitalize(field)}
      />
    );
  });
};

export { TreeView };
