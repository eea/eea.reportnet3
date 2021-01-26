import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';

import styles from './WebformTable.module.scss';

import { Button } from 'ui/views/_components/Button';
import { IconTooltip } from 'ui/views/_components/IconTooltip';
import { Spinner } from 'ui/views/_components/Spinner';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { DataViewerUtils } from 'ui/views/_components/DataViewer/_functions/Utils/DataViewerUtils';
import { MetadataUtils } from 'ui/views/_functions/Utils';
import { TextUtils } from 'ui/views/_functions/Utils';
import { WebformsUtils } from 'ui/views/Webforms/_functions/Utils/WebformsUtils';

export const WebformTable = ({
  calculateSingle,
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
  const { onParseWebformRecords, parseNewTableRecord, parseOtherObjectivesRecord } = WebformsUtils;

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
    obj.elements.filter(element => {
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
        const response = await DatasetService.addRecordsById(datasetId, tableSchemaId, [newEmptyRecord]);
        if (response.status >= 200 && response.status <= 299) {
          onUpdateData();
        }
      } catch (error) {
        console.error('error', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add({
          type: 'ADD_RECORDS_ERROR',
          content: { dataflowId, dataflowName, datasetId, datasetName, tableName: webformData.title }
        });
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

      const parentTableData = await DatasetService.tableDataById(
        datasetId,
        webform.tableSchemaId,
        '',
        300,
        undefined,
        ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
        undefined,
        fieldSchema || fieldId,
        selectedTable.pamsId
      );
      if (!isNil(parentTableData.records)) {
        const tables = getTableElements(webform);
        const tableSchemaIds = tables.map(table => table.tableSchemaId);

        const tableData = {};

        for (let index = 0; index < tableSchemaIds.length; index++) {
          const tableSchemaId = tableSchemaIds[index];
          const { fieldSchema, fieldId } = getFieldSchemaId(tables, tableSchemaId);
          const tableChildData = await DatasetService.tableDataById(
            datasetId,
            tableSchemaId,
            '',
            '',
            undefined,
            ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
            undefined,
            fieldSchema || fieldId,
            selectedTable.pamsId
          );
          tableData[tableSchemaId] = tableChildData;
          // 'tableChildData', tableChildData;
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
      onUpdateSinglesList={onUpdateSinglesList}
      onUpdatePamsValue={onUpdatePamsValue}
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
        return <Fragment />;
    }
  };

  if (webformTableState.isLoading) {
    return <Spinner style={{ top: 0, margin: '1rem' }} />;
  }

  const validationsTemplate = recordData => {
    const validationsGroup = DataViewerUtils.groupValidations(
      parseData(webformData)[0],
      resources.messages['recordBlockers'],
      resources.messages['recordErrors'],
      resources.messages['recordWarnings'],
      resources.messages['recordInfos']
    );
    return getIconsValidationsErrors(validationsGroup);
  };

  const addIconLevelError = (validation, levelError, message) => {
    let icon = [];
    if (!isEmpty(validation)) {
      icon.push(
        <IconTooltip
          className={styles.iconTooltipLevelError}
          key={levelError}
          levelError={levelError}
          message={message}
        />
      );
    }
    return icon;
  };

  const getIconsValidationsErrors = validations => {
    let icons = [];
    if (isNull(validations)) return icons;

    const blockerIcon = addIconLevelError(validations.blockers, 'BLOCKER', validations.messageBlockers);
    const errorIcon = addIconLevelError(validations.errors, 'ERROR', validations.messageErrors);
    const warningIcon = addIconLevelError(validations.warnings, 'WARNING', validations.messageWarnings);
    const infoIcon = addIconLevelError(validations.infos, 'INFO', validations.messageInfos);

    icons = blockerIcon.concat(errorIcon, warningIcon, infoIcon);
    return icons;
  };

  const parseData = data => {
    if (isNil(data.elementsRecords)) return [];

    return data.elementsRecords.map(record => {
      const datasetPartitionId = record.datasetPartitionId;
      const providerCode = record.providerCode;
      const recordValidations = record.validations;
      const recordId = record.recordId;
      const recordSchemaId = record.recordSchemaId;
      const arrayDataFields = record.fields.map(field => {
        return {
          fieldData: {
            [field.fieldSchemaId]: field.value,
            type: field.type,
            id: field.fieldId,
            fieldSchemaId: field.fieldSchemaId
          },
          fieldValidations: field.validations
        };
      });
      arrayDataFields.push({ fieldData: { id: record.recordId }, fieldValidations: null });
      arrayDataFields.push({ fieldData: { datasetPartitionId: record.datasetPartitionId }, fieldValidations: null });
      const arrayDataAndValidations = {
        dataRow: arrayDataFields,
        recordValidations,
        recordId,
        datasetPartitionId,
        providerCode,
        recordSchemaId
      };
      return arrayDataAndValidations;
    });
  };

  return (
    <div className={styles.contentWrap}>
      <h3 className={styles.title}>
        <div>
          {webformData.title
            ? `${webformData.title}${webformData.subtitle ? `: ${webform.subtitle}` : ''}`
            : webformData.name}

          {validationsTemplate()}
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
