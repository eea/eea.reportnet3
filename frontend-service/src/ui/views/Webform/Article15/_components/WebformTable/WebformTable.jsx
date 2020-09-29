import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './WebformTable.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Spinner } from 'ui/views/_components/Spinner';
import { WebformField } from './_components/WebformField';
import { WebformRecord } from '../WebformContent/_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { WebformTableUtils } from './_functions/Utils/WebformTableUtils';

export const WebformTable = ({ datasetId, onTabChange, webform }) => {
  const [webformTableState, webformTableDispatch] = useReducer(webformTableReducer, {
    isDataUpdated: false,
    isLoading: true,
    webformData: { ...webform, here: webform.elements.map(element => ({ ...element })) }
  });

  const { webformData, isDataUpdated } = webformTableState;

  useEffect(() => {
    webformTableDispatch({
      type: 'INITIAL_LOAD',
      payload: { webformData: { ...webform, here: webform.elements.map(element => ({ ...element })) } }
    });
  }, [webform]);

  useEffect(() => {
    onLoadTableData();
  }, [isDataUpdated, onTabChange, webform]);

  const isLoading = value => webformTableDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadTableData = async () => {
    isLoading(true);
    try {
      const tableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', '', undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      if (!isNil(tableData.records)) {
        const records = tableData.records.map(record => {
          const { fields } = record;
          const { elements } = webform;

          const result = [];
          for (let i = 0; i < fields.length; i++) {
            result.push({
              ...fields[i],
              ...elements.find(element => element['fieldSchema'] === fields[i]['fieldSchemaId']),
              fieldSchemaId: fields[i].fieldSchemaId,
              recordId: record.recordId,
              fieldId: fields[i].fieldId,
              validations: fields[i].validations || [],
              value: fields[i].value
            });
          }

          return { ...record, elementsRecords: result };
        });

        webformTableDispatch({ type: 'ON_LOAD_DATA', payload: { records } });
      }
    } catch (error) {
    } finally {
      isLoading(false);
    }
  };

  const onUpdateData = () => {
    webformTableDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !webformTableState.isDataUpdated } });
  };

  const renderWebformFields = isMultiple => {
    return isMultiple ? (
      webformData.here.map((record, i) => (
        <WebformRecord
          datasetId={datasetId}
          key={i}
          onRefresh={onUpdateData}
          record={record}
          tableId={webformData.tableSchemaId}
        />
      ))
    ) : (
      <WebformRecord
        datasetId={datasetId}
        onRefresh={onUpdateData}
        record={webformData.here[0]}
        tableId={webformData.tableSchemaId}
      />
    );
  };

  if (webformTableState.isLoading) return <Spinner style={{ top: 0, margin: '1rem' }} />;

  return (
    <div className={styles.body}>
      <h3 className={styles.title}>
        {webformData.title ? webformData.title : webformData.name}
        {webformData.multipleRecords ? <Button label={'Add'} icon={'plus'} onClick={() => {}} /> : <Fragment />}
      </h3>
      {renderWebformFields(webformData.multipleRecords)}
    </div>
  );
};
