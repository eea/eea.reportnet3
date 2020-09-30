import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import sortBy from 'lodash/sortBy';

import styles from './WebformTable.module.scss';

import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { Button } from 'ui/views/_components/Button';
import { Spinner } from 'ui/views/_components/Spinner';
import { WebformField } from './_components/WebformField';
import { WebformRecord } from '../WebformContent/_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { WebformTableUtils } from './_functions/Utils/WebformTableUtils';
import { Article15Utils } from 'ui/views/Webform/Article15/_functions/Utils/Article15Utils';

export const WebformTable = ({ datasetId, onTabChange, webform }) => {
  const [webformTableState, webformTableDispatch] = useReducer(webformTableReducer, {
    isDataUpdated: false,
    isLoading: true,
    tableData: {},
    webformData: {}
  });

  const { webformData, isDataUpdated, tableData } = webformTableState;

  useEffect(() => {
    webformTableDispatch({
      type: 'INITIAL_LOAD',
      payload: { webformData: { ...webform } }
    });
  }, [webform]);

  useEffect(() => {
    if (webform.tableSchemaId) {
      onLoadTableData();
    }
  }, [isDataUpdated, onTabChange, webform]);

  const isLoading = value => webformTableDispatch({ type: 'IS_LOADING', payload: { value } });

  const parseNewRecord = (columnsSchema, data) => {
    if (!isEmpty(columnsSchema)) {
      let fields;

      if (!isUndefined(columnsSchema)) {
        fields = columnsSchema.map(column => {
          if (column.type === 'FIELD') {
            return {
              fieldData: { [column.fieldSchemaId]: null, type: column.fieldType, fieldSchemaId: column.fieldSchemaId }
            };
          }
        });
      }

      const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

      obj.datasetPartitionId = null;
      //dataSetPartitionId is needed for checking the rows owned by delegated contributors
      if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

      return obj;
    }
  };

  const onAddMultipleWebform = async tableSchemaId => {
    if (!isEmpty(webformData.elementsRecords)) {
      const newEmptyRecord = parseNewRecord(webformData.elementsRecords[0].elements);

      try {
        await DatasetService.addRecordsById(datasetId, tableSchemaId, [newEmptyRecord]);
      } catch (error) {}
    }
  };

  const onLoadTableData = async () => {
    isLoading(true);
    try {
      const parentTableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', '', undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      if (!isNil(parentTableData.records)) {
        //GET ALL TABLE SCHEMA ID
        const tableSchemaIds = webform.elements
          .filter(element => element.type === 'TABLE' && !isNil(element.tableSchemaId))
          .map(table => table.tableSchemaId);

        const tableData = {};

        for (let index = 0; index < tableSchemaIds.length; index++) {
          const tableSchemaId = tableSchemaIds[index];

          const tableChildData = await DatasetService.tableDataById(datasetId, tableSchemaId, '', '', undefined, [
            'CORRECT',
            'INFO',
            'WARNING',
            'ERROR',
            'BLOCKER'
          ]);

          tableData[tableSchemaId] = tableChildData;
        }

        const records = onParseWebformRecords(parentTableData.records, webform, tableData);

        webformTableDispatch({ type: 'ON_LOAD_DATA', payload: { records } });
      }
    } catch (error) {
      console.log('ERROR', error);
    } finally {
      isLoading(false);
    }
  };

  const onParseWebformRecords = (records, webform, tableData) => {
    return records.map(record => {
      const { fields } = record;
      const { elements } = webform;

      const result = [];

      for (let index = 0; index < elements.length; index++) {
        const element = elements[index];

        if (element.type === 'FIELD') {
          result.push({
            fieldType: 'EMPTY',
            ...element,
            ...fields.find(field => field['fieldSchemaId'] === element['fieldSchema']),
            codelistItems: element.codelistItems || [],
            description: element.description || '',
            isDisabled: isNil(element.fieldSchema),
            name: element.name,
            recordId: record.recordId,
            type: element.type
          });
        } else {
          if (tableData[element.tableSchemaId]) {
            const tableElementsRecords = onParseWebformRecords(
              tableData[element.tableSchemaId].records,
              element,
              tableData
            );
            result.push({ ...element, elementsRecords: tableElementsRecords });
          } else {
            result.push({ ...element, tableNotCreated: true, elementsRecords: [] });
          }
        }
      }

      return { ...record, elements: result };
    });
  };

  const onUpdateData = () => {
    webformTableDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !webformTableState.isDataUpdated } });
  };

  const renderWebformFields = isMultiple => {
    return isMultiple ? (
      webformData.elementsRecords.map((record, i) => {
        return (
          <WebformRecord
            datasetId={datasetId}
            key={i}
            onRefresh={onUpdateData}
            onTabChange={onTabChange}
            record={record}
            tableId={webformData.tableSchemaId}
            onAddMultipleWebform={onAddMultipleWebform}
          />
        );
      })
    ) : (
      <WebformRecord
        datasetId={datasetId}
        onRefresh={onUpdateData}
        onTabChange={onTabChange}
        record={webformData.elementsRecords[0]}
        tableId={webformData.tableSchemaId}
        onAddMultipleWebform={onAddMultipleWebform}
      />
    );
  };

  if (webformTableState.isLoading) return <Spinner style={{ top: 0, margin: '1rem' }} />;

  const childHasErrors = webformData.elements
    .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
    .map(table => table.hasErrors);

  const hasErrors = [webformData.hasErrors].concat(childHasErrors);

  return (
    <div className={styles.body}>
      <h3 className={styles.title}>
        <div>
          {webformData.title ? webformData.title : webformData.name}
          {hasErrors.includes(true) && <IconTooltip levelError={'ERROR'} message={'This table has errors'} />}
        </div>
        {webformData.multipleRecords && (
          <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform(webformData.tableSchemaId)} />
        )}
      </h3>
      {isNil(webformData.tableSchemaId) && (
        <span style={{ color: 'red' }}>
          {`The table ${webformData.name} is not created in the design, please check it`}
        </span>
      )}
      {!isNil(webformData.elementsRecords) && renderWebformFields(webformData.multipleRecords)}
    </div>
  );
};
