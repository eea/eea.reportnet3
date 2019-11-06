import React, { useState, useContext } from 'react';
import PropTypes from 'prop-types';
import { isUndefined, isNull } from 'lodash';

import styles from './FieldsDesigner.module.css';

import { DataViewer } from 'ui/views/ReporterDataSet/_components/TabsSchema/_components/DataViewer';
import { FieldDesigner } from './_components/FieldDesigner';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const FieldsDesigner = ({ datasetId, datasetSchemaId, table }) => {
  const [isPreviewModeOn, setIsPreviewModeOn] = useState(false);

  const resources = useContext(ResourcesContext);

  const previewData = () => {
    const tableSchemaColumns =
      !isNull(table.records) && !isUndefined(table.records)
        ? table.records[0].fields.map(field => {
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${field['name'].charAt(0).toUpperCase()}${field['name'].slice(1)}`,
              type: field['type'],
              recordId: field['recordId']
            };
          })
        : [];

    return !isUndefined(table) && !isNull(table.records) && !isUndefined(table.records) ? (
      <DataViewer
        hasWritePermissions={true}
        isWebFormMMR={false}
        // buttonsList={[]}
        key={table.id}
        tableId={table.tableSchemaId}
        tableName={table}
        tableSchemaColumns={tableSchemaColumns}
      />
    ) : (
      <div>
        <h3>{resources.messages['datasetDesignerNoFields']}</h3>
      </div>
    );
  };

  const renderFields = () => {
    const fields =
      !isUndefined(table) && !isUndefined(table.records) && !isNull(table.records) && !isUndefined(table.records[0]) ? (
        table.records[0].fields.map(field => (
          <div className={styles.fieldDesignerWrapper} key={field.fieldId}>
            <FieldDesigner fieldId={field.fieldId} fieldName={field.name} fieldType={field.type} />
          </div>
        ))
      ) : (
        <div className={styles.fieldDesignerWrapper} key="0">
          <FieldDesigner fieldId="-1" fieldName="" fieldType="" />
        </div>
      );
    if (fields.length > 1) {
      fields.push(
        <div className={styles.fieldDesignerWrapper} key="0">
          <FieldDesigner fieldId="-1" fieldName="" fieldType="" />
        </div>
      );
    }

    return fields;
  };

  //return fieldsSchema.map(field => {
  return (
    <React.Fragment>
      <div className={styles.InputSwitchContainer}>
        <div className={styles.InputSwitchDiv}>
          <span className={styles.InputSwitchText}>{resources.messages['design']}</span>
          <InputSwitch
            checked={isPreviewModeOn}
            onChange={e => {
              setIsPreviewModeOn(e.value);
            }}
          />
          <span className={styles.InputSwitchText}>{resources.messages['preview']}</span>
        </div>
      </div>

      {isPreviewModeOn ? previewData() : renderFields()}
    </React.Fragment>
  );
  // });
};
FieldsDesigner.propTypes = {};
