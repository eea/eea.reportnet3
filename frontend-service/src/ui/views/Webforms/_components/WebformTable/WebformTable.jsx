import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

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
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

export const WebformTable = ({
  dataflowId,
  datasetId,
  isRefresh,
  isReporting,
  onTabChange,
  selectedTable = { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
  webform,
  webformType
}) => {
  const { onParseWebformRecords, parseNewTableRecord } = WebformsUtils;

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
  }, [isDataUpdated, webform, isRefresh]);

  const isLoading = value => webformTableDispatch({ type: 'IS_LOADING', payload: { value } });

  const onAddMultipleWebform = async tableSchemaId => {
    webformTableDispatch({
      type: 'SET_IS_ADDING_MULTIPLE',
      payload: { isAddingMultiple: true, addingOnTableSchemaId: tableSchemaId }
    });

    if (!isEmpty(webformData.elementsRecords)) {
      const filteredTable = webformData.elementsRecords[0].elements.filter(
        element => element.tableSchemaId === tableSchemaId
      )[0];
      const newEmptyRecord = parseNewTableRecord(filteredTable, selectedTable.pamsId);

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
        webformTableDispatch({
          type: 'SET_IS_ADDING_MULTIPLE',
          payload: { isAddingMultiple: false, addingOnTableSchemaId: null }
        });
      }
    }
  };

  const onLoadTableData = async () => {
    try {
      const parentTableData = await DatasetService.tableDataById(
        datasetId,
        webform.tableSchemaId,
        '',
        100,
        undefined,
        ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
        undefined,
        selectedTable.fieldSchemaId,
        selectedTable.pamsId
      );
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

        const records = onParseWebformRecords(
          parentTableData.records,
          webform,
          tableData,
          parentTableData.totalRecords
        );

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
      webformTableDispatch({
        type: 'SET_IS_ADDING_MULTIPLE',
        payload: { isAddingMultiple: false, addingOnTableSchemaId: null }
      });
    }
  };

  const onUpdateData = () => {
    webformTableDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !webformTableState.isDataUpdated } });
  };

  const renderWebformRecord = (record, index) => (
    <WebformRecord
      addingOnTableSchemaId={webformTableState.addingOnTableSchemaId}
      columnsSchema={webformData.elementsRecords[0] ? webformData.elementsRecords[0].elements : []}
      dataflowId={dataflowId}
      datasetId={datasetId}
      hasFields={isNil(webformData.records) || isEmpty(webformData.records[0].fields)}
      isAddingMultiple={webformTableState.isAddingMultiple}
      isFixedNumber={webformData.fixedNumber || null}
      isReporting={isReporting}
      key={index}
      multipleRecords={webformData.multipleRecords}
      onAddMultipleWebform={onAddMultipleWebform}
      onRefresh={onUpdateData}
      onTabChange={onTabChange}
      record={record}
      tableId={webformData.tableSchemaId}
      tableName={webformData.title}
      webformType={webformType}
    />
  );

  const renderArticle15WebformRecords = isMultiple => {
    const { elementsRecords } = webformData;

    return isMultiple
      ? elementsRecords.map((record, index) => renderWebformRecord(record, index))
      : renderWebformRecord(elementsRecords[0], null);
  };

  const renderArticle13WebformRecords = () => {
    // const filteredRecord = webformData.elementsRecords.filter(record => {
    //   // console.log('record', record);
    //   console.log('selectedId', selectedId);
    //   return record.recordId === selectedId;
    // });

    // //TODO: Filter by idPam
    // return renderWebformRecord(!isEmpty(filteredRecord) ? filteredRecord[0] : webformData.elementsRecords[0], null);
    return renderWebformRecord(webformData.elementsRecords[0], null);
  };

  const renderWebform = isMultiple => {
    switch (webformType) {
      case 'ARTICLE_15':
        return renderArticle15WebformRecords(isMultiple);

      case 'ARTICLE_13':
        return renderArticle13WebformRecords();

      default:
        return <Fragment />;
    }
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
          <Button
            icon={'plus'}
            label={resources.messages['addRecord']}
            onClick={() => onAddMultipleWebform(webformData.tableSchemaId)}
          />
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
      {!isNil(webformData.elementsRecords) && renderWebform(webformData.multipleRecords)}
    </div>
  );
};
