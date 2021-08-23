import { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformTable.module.scss';

import { Button } from 'views/_components/Button';
import { GroupedRecordValidations } from 'views/Webforms/_components/GroupedRecordValidations';
import { Spinner } from 'views/_components/Spinner';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { MetadataUtils } from 'views/_functions/Utils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const WebformTable = ({
  calculateSingle,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  getFieldSchemaId = () => ({ fieldSchema: undefined, fieldId: undefined }),
  isGroup,
  isRefresh,
  isReporting,
  onTabChange,
  onUpdatePamsValue,
  onUpdateSinglesList,
  pamsRecords,
  selectedTable = { fieldSchemaId: null, pamsId: null, recordId: null, tableName: null },
  setIsLoading = () => {},
  webform,
  webformType
}) => {
  const { onParseWebformRecords, parseNewTableRecord, parseOtherObjectivesRecord, parseRecordsValidations } =
    WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [webformTableState, webformTableDispatch] = useReducer(webformTableReducer, {
    addingOnTableSchemaId: null,
    isAddingMultiple: false,
    isDataUpdated: 0,
    isLoading: true,
    webformData: {}
  });

  const { isDataUpdated, webformData } = webformTableState;

  useEffect(() => {
    webformTableDispatch({ type: 'INITIAL_LOAD', payload: { webformData: { ...webform } } });
  }, [webform]);

  useEffect(() => {
    if (!isNil(webform) && isNil(webform.tableSchemaId)) isLoading(false);

    if (!isNil(webform) && webform.tableSchemaId) {
      if (webformType === 'ARTICLE_13' && !isNil(selectedTable.pamsId)) {
        isLoading(true);
        onLoadTableData();
      }

      if (webformType === 'ARTICLE_15') {
        isLoading(true);
        onLoadTableData();
      }
    }
  }, [isRefresh, onTabChange, selectedTable.pamsId, webform]);

  useEffect(() => {
    if (isDataUpdated !== 0) {
      onLoadTableData();
    }
  }, [isDataUpdated]);

  const getTableElements = obj => {
    const tableElements = [];
    obj.elements.forEach(element => {
      if (
        element.type === 'TABLE' &&
        !isNil(element.tableSchemaId) &&
        element.elements.filter(el => el.type === 'TABLE').length === 0
      ) {
        tableElements.push(element);
      } else {
        if (!isNil(element.elements) && element.type === 'TABLE' && !isNil(element.tableSchemaId)) {
          tableElements.push(element);
          tableElements.push(...getTableElements(element));
        }
      }
    });

    return tableElements;
  };

  const isLoading = value => webformTableDispatch({ type: 'IS_LOADING', payload: { value } });

  const onAddMultipleWebform = async (tableSchemaId, filteredRecordId = null) => {
    webformTableDispatch({
      type: 'SET_IS_ADDING_MULTIPLE',
      payload: { isAddingMultiple: true, addingOnTableSchemaId: tableSchemaId }
    });

    if (!isEmpty(webformData.elementsRecords)) {
      let sectorObjectivesTable;
      const filteredTable = getTableElements(webformData.elementsRecords[0]).filter(element => {
        if (TextUtils.areEquals(element.name, 'SectorObjectives')) {
          sectorObjectivesTable = element;
        }
        return element.tableSchemaId === tableSchemaId;
      })[0];

      const newEmptyRecord = TextUtils.areEquals(filteredTable.name, 'OtherObjectives')
        ? parseOtherObjectivesRecord(filteredTable, sectorObjectivesTable, selectedTable.pamsId, filteredRecordId)
        : parseNewTableRecord(filteredTable, selectedTable.pamsId, sectorObjectivesTable);

      try {
        await DatasetService.createRecord(datasetId, tableSchemaId, [newEmptyRecord]);
        onUpdateData();
      } catch (error) {
        console.error('WebformTable - onAddMultipleWebform.', error);
        if (error.response.status === 423) {
          notificationContext.add({
            type: 'GENERIC_BLOCKED_ERROR'
          });
        } else {
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add({
            type: 'ADD_RECORDS_ERROR',
            content: { dataflowId, dataflowName, datasetId, datasetName, tableName: webformData.title }
          });
        }
        webformTableDispatch({
          type: 'SET_IS_ADDING_MULTIPLE',
          payload: { addingOnTableSchemaId: null, isAddingMultiple: false }
        });
      }
    }
  };

  const onLoadTableData = async () => {
    setIsLoading(true);
    try {
      const { fieldSchema, fieldId } = getFieldSchemaId([webform], webform.tableSchemaId);

      const data = await DatasetService.getTableData({
        datasetId,
        tableSchemaId: webform.tableSchemaId,
        pageNum: '',
        pageSize: 300,
        levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
        fieldSchemaId: fieldSchema || fieldId,
        value: selectedTable.pamsId
      });
      if (!isNil(data.records)) {
        const tables = getTableElements(webform);
        const tableSchemaIds = tables.map(table => table.tableSchemaId);

        const tableData = {};

        for (let index = 0; index < tableSchemaIds.length; index++) {
          const tableSchemaId = tableSchemaIds[index];
          const { fieldSchema, fieldId } = getFieldSchemaId(tables, tableSchemaId);
          tableData[tableSchemaId] = await DatasetService.getTableData({
            datasetId,
            tableSchemaId,
            levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
            fieldSchemaId: fieldSchema || fieldId,
            value: selectedTable.pamsId
          });
        }
        const records = onParseWebformRecords(data.records, webform, tableData, data.totalRecords);

        webformTableDispatch({ type: 'ON_LOAD_DATA', payload: { records } });
      }
    } catch (error) {
      console.error('WebformTable - onLoadTableData.', error);
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
      setIsLoading(false);
      webformTableDispatch({
        type: 'SET_IS_ADDING_MULTIPLE',
        payload: { isAddingMultiple: false, addingOnTableSchemaId: null }
      });
    }
  };

  const onUpdateData = () => {
    webformTableDispatch({ type: 'ON_UPDATE_DATA', payload: { value: isDataUpdated + 1 } });
  };

  const renderWebformRecord = (record, index) => (
    <WebformRecord
      addingOnTableSchemaId={webformTableState.addingOnTableSchemaId}
      calculateSingle={calculateSingle}
      columnsSchema={webformData.elementsRecords[0] ? webformData.elementsRecords[0].elements : []}
      dataProviderId={dataProviderId}
      dataflowId={dataflowId}
      datasetId={datasetId}
      datasetSchemaId={datasetSchemaId}
      hasFields={isNil(webformData.records) || isEmpty(webformData.records[0].fields)}
      isAddingMultiple={webformTableState.isAddingMultiple}
      isFixedNumber={webformData.fixedNumber || webformData.tableSchemaFixedNumber || null}
      isGroup={isGroup}
      isReporting={isReporting}
      key={index}
      multipleRecords={webformData.multipleRecords}
      onAddMultipleWebform={onAddMultipleWebform}
      onRefresh={onUpdateData}
      onTabChange={onTabChange}
      onUpdatePamsValue={onUpdatePamsValue}
      onUpdateSinglesList={onUpdateSinglesList}
      pamsRecords={pamsRecords}
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
    return renderWebformRecord(webformData.elementsRecords[0], null);
  };

  const renderWebform = isMultiple => {
    switch (webformType) {
      case 'ARTICLE_15':
        return renderArticle15WebformRecords(isMultiple);

      case 'ARTICLE_13':
        return renderArticle13WebformRecords();

      default:
        return <div />;
    }
  };

  if (webformTableState.isLoading) {
    return <Spinner style={{ top: 0, margin: '1rem' }} />;
  }

  return (
    <div className={styles.contentWrap}>
      <h3 className={styles.title}>
        <div>
          {webformData.title
            ? `${webformData.title}${webformData.subtitle ? `: ${webform.subtitle}` : ''}`
            : webformData.name}

          <GroupedRecordValidations parsedRecordData={parseRecordsValidations(webformData.elementsRecords)[0]} />
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
