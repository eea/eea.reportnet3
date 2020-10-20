import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './WebformTable.module.scss';

import { Button } from 'ui/views/_components/Button';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { Spinner } from 'ui/views/_components/Spinner';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const WebformTable = ({ dataflowId, datasetId, isReporting, onTabChange, webform }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [webformTableState, webformTableDispatch] = useReducer(webformTableReducer, {
    isAddingMultiple: false,
    addingOnTableSchemaId: null,
    isDataUpdated: false,
    isLoading: true,
    webformData: {}
  });

  const { webformData, isDataUpdated } = webformTableState;

  useEffect(() => {
    webformTableDispatch({ type: 'INITIAL_LOAD', payload: { webformData: { ...webform } } });
  }, [webform]);

  useEffect(() => {
    if (!isNil(webform) && webform.tableSchemaId) {
      isLoading(true);
      onLoadTableData();
    } else if (!isNil(webform) && isNil(webform.tableSchemaId)) {
      isLoading(false);
    }
  }, [onTabChange, webform]);

  useEffect(() => {
    if (!isNil(webform) && webform.tableSchemaId) {
      onLoadTableData();
    }
  }, [isDataUpdated, webform]);

  const isLoading = value => webformTableDispatch({ type: 'IS_LOADING', payload: { value } });

  const parseNewRecord = (columnsSchema, data) => {
    if (!isEmpty(columnsSchema)) {
      let fields;

      if (!isUndefined(columnsSchema)) {
        fields = columnsSchema.map(column => {
          if (column.type === 'FIELD') {
            return {
              fieldData: { [column.fieldSchema]: null, type: column.fieldType, fieldSchemaId: column.fieldSchema }
            };
          }
        });
      }

      const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

      obj.datasetPartitionId = null;
      if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

      return obj;
    }
  };

  const onAddMultipleWebform = async tableSchemaId => {
    webformTableDispatch({
      type: 'SET_IS_ADDING_MULTIPLE',
      payload: { isAddingMultiple: true, addingOnTableSchemaId: tableSchemaId }
    });

    if (!isEmpty(webformData.elementsRecords)) {
      const newEmptyRecord = parseNewRecord(
        webformData.elementsRecords[0].elements.filter(element => element.tableSchemaId === tableSchemaId)[0].elements
      );

      try {
        const response = await DatasetService.addRecordsById(datasetId, tableSchemaId, [newEmptyRecord]);
        if (response) {
          onUpdateData();
        }
      } catch (error) {
        console.error('error', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'ADD_RECORDS_BY_ID_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName, tableName: webformData.title }
        });
      } finally {
        webformTableDispatch({
          type: 'SET_IS_ADDING_MULTIPLE',
          payload: { isAddingMultiple: false, addingOnTableSchemaId: null }
        });
      }
    }
  };

  const onLoadTableData = async () => {
    try {
      const parentTableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', 100, undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      if (!isNil(parentTableData.records)) {
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
      console.error('ERROR', error);

      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'TABLE_DATA_BY_ID_ERROR',
        content: { dataflowId, dataflowName, datasetId, datasetName }
      });
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
            maxSize: element.maxSize,
            name: element.name,
            recordId: record.recordId,
            type: element.type,
            validExtensions: element.validExtensions
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
            addingOnTableSchemaId={webformTableState.addingOnTableSchemaId}
            columnsSchema={webformData.elementsRecords[0].elements}
            dataflowId={dataflowId}
            datasetId={datasetId}
            isAddingMultiple={webformTableState.isAddingMultiple}
            isReporting={isReporting}
            key={i}
            multipleRecords={webformData.multipleRecords}
            onAddMultipleWebform={onAddMultipleWebform}
            onRefresh={onUpdateData}
            onTabChange={onTabChange}
            record={record}
            tableId={webformData.tableSchemaId}
            tableName={webformData.title}
          />
        );
      })
    ) : (
      <WebformRecord
        addingOnTableSchemaId={webformTableState.addingOnTableSchemaId}
        columnsSchema={webformData.elementsRecords[0] ? webformData.elementsRecords[0].elements : []}
        dataflowId={dataflowId}
        datasetId={datasetId}
        isAddingMultiple={webformTableState.isAddingMultiple}
        isReporting={isReporting}
        multipleRecords={webformData.multipleRecords}
        onAddMultipleWebform={onAddMultipleWebform}
        onRefresh={onUpdateData}
        onTabChange={onTabChange}
        record={webformData.elementsRecords[0]}
        tableId={webformData.tableSchemaId}
      />
    );
  };

  if (webformTableState.isLoading) return <Spinner style={{ top: 0, margin: '1rem' }} />;

  const childHasErrors = webformData.elements
    .filter(element => element.type === 'TABLE' && !isNil(element.hasErrors))
    .map(table => table.hasErrors);

  const hasErrors = [webformData.hasErrors].concat(childHasErrors);

  return (
    <div className={styles.contentWrap}>
      <h3 className={styles.title}>
        <div>
          {webformData.title ? webformData.title : webformData.name}
          {hasErrors.includes(true) && (
            <IconTooltip levelError={'ERROR'} message={resources.messages['tableWithErrorsTooltip']} />
          )}
        </div>
        {webformData.multipleRecords && (
          <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform(webformData.tableSchemaId)} />
        )}
      </h3>
      {isNil(webformData.tableSchemaId) && (
        <span
          className={styles.nonExistTable}
          dangerouslySetInnerHTML={{
            __html: TextUtils.parseText(resources.messages['tableIsNotCreated'], { tableName: webformData.name })
          }}
        />
      )}
      {!isNil(webformData.elementsRecords) && renderWebformFields(webformData.multipleRecords)}
    </div>
  );
};
