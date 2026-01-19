import { useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './WebformTable.module.scss';

import { Button } from 'views/_components/Button';
import { Spinner } from 'views/_components/Spinner';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { webformTableReducer } from './_functions/Reducers/webformTableReducer';

import { ErrorUtils, MetadataUtils } from 'views/_functions/Utils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const WebformTable = ({
  bigData,
  onFieldUpdate,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchema,
  datasetSchemaId,
  getFieldSchemaId = () => ({ fieldSchema: undefined, fieldId: undefined }),
  isEditor,
  isIcebergCreated,
  isLoadingIceberg,
  isRefresh,
  isReporting,
  isViewMode,
  updatingField,
  onTabChange,
  rootPkFieldId,
  rootTableName,
  selectedTable = { fieldSchemaId: null, rootTableId: undefined, recordId: null, tableName: null },
  setIsLoading = () => {},
  webform,
  webformType
}) => {
  const {
    onParseWebformRecords,
    parseRecordsValidations,
    parseNewEntitiesTableRecord,
    parseNewEntityTableRecordTable
  } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [webformTableState, webformTableDispatch] = useReducer(webformTableReducer, {
    addingOnTableSchemaId: null,
    isAddingMultiple: false,
    isDataUpdated: 0,
    isLoading: true,
    webformData: {}
  });

  const { isDataUpdated, webformData } = webformTableState;

  const [isSticky, setIsSticky] = useState(false);
  const [allManualCheck, setAllManualCheck] = useState(true);
  const [optionalAddRecordEnabled, setOptionalAddRecordEnabled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      const scrollPosition = window.scrollY;

      if (webformData.multipleRecords) {
        const scrollThreshold = document.documentElement.scrollHeight * 0.18;
        const maxScrollThreshold = document.documentElement.scrollHeight * 0.62;
        setIsSticky(scrollPosition > scrollThreshold && scrollPosition < maxScrollThreshold);
      } else {
        const scrollThreshold = document.documentElement.scrollHeight * 0.12;
        const maxScrollThreshold = document.documentElement.scrollHeight * 0.86;
        setIsSticky(scrollPosition > scrollThreshold && scrollPosition < maxScrollThreshold);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  useEffect(() => {
    webformTableDispatch({ type: 'INITIAL_LOAD', payload: { webformData: { ...webform } } });
  }, [webform]);

  useEffect(() => {
    if (!isNil(webform) && isNil(webform.tableSchemaId)) isLoading(false);

    if (!isNil(webform) && webform.tableSchemaId) {
      if (webformType === 'ENTITIES' && !isNil(selectedTable.rootTableId)) {
        isLoading(true);
        onLoadTableData();
      } else if (webformType === 'TABLES') {
        isLoading(true);
        onLoadTableData();
      }
    }
  }, [isRefresh, onTabChange, selectedTable.rootTableId, webform]);

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

  const onAddMultipleWebform = async (tableSchemaId, primaryFkValue, mainTable = false, fkFields) => {
    webformTableDispatch({
      type: 'SET_IS_ADDING_MULTIPLE',
      payload: { isAddingMultiple: true, addingOnTableSchemaId: tableSchemaId }
    });

    let newEmptyRecord;

    if (!isEmpty(webformData.elementsRecords)) {
      if (!mainTable) {
        const filteredTable = getTableElements(webformData.elementsRecords[0]).filter(
          element => element.tableSchemaId === tableSchemaId
        )[0];

        const primaryFkFieldId = filteredTable.records[0].fields.filter(
          field =>
            !isNil(field?.referencedField?.idPk) &&
            (selectedTable.fieldSchemaId === rootPkFieldId
              ? field?.referencedField?.idPk === rootPkFieldId
              : field?.referencedField?.idPk !== rootPkFieldId)
        )[0]?.referencedField?.idPk;

        newEmptyRecord = parseNewEntitiesTableRecord(
          filteredTable,
          selectedTable.rootTableId,
          rootPkFieldId,
          primaryFkFieldId,
          primaryFkValue,
          fkFields
        );
      }
    }

    if (mainTable) {
      let fkRootField;
      let webformDataWithFkRootField;

      // Add the FK field that is linked to root table PK in table elements if it is missing
      if (webformData?.isOptional || webformData?.multipleRecords) {
        fkRootField = datasetSchema.tables
          .find(datasetTable => datasetTable.tableSchemaName === webformData.name)
          ?.records?.[0]?.fields?.find(tableField => tableField?.referencedField?.idPk === rootPkFieldId);

        const webformFieldElements = webformData.elements.filter(el => el.type === 'FIELD');

        // Include field elements nested inside blocks so they are part of the record sent to the backend.
        // This ensures all table fields are included and created in the database if any columns are missing.
        const blockFieldElements = webformData.elements
          .filter(el => el.type === 'BLOCK' && Array.isArray(el.elements))
          .flatMap(block => block.elements.filter(blockElement => blockElement.type === 'FIELD'));

        const allFieldElements = [...webformFieldElements, ...blockFieldElements];

        webformDataWithFkRootField = fkRootField
          ? {
              ...webformData,
              elements: [
                ...allFieldElements.filter(fieldElement => fieldElement?.fieldSchema !== fkRootField?.fieldSchema),
                fkRootField
              ]
            }
          : undefined;
      }

      newEmptyRecord = parseNewEntityTableRecordTable(
        (webformData?.isOptional || webformData?.multipleRecords) && fkRootField && webformDataWithFkRootField
          ? webformDataWithFkRootField
          : webformData,
        selectedTable.rootTableId,
        rootPkFieldId
      );
    }

    if (!isEmpty(newEmptyRecord)) {
      try {
        bigData && onFieldUpdate(true);
        await DatasetService.createWebformTableRecord(datasetId, tableSchemaId, [newEmptyRecord]);
        onUpdateData();
      } catch (error) {
        console.error('WebformTable - onAddMultipleWebform.', error);
        if (error.response.status === 423) {
          notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
        } else {
          const {
            dataflow: { name: dataflowName },
            dataset: { name: datasetName }
          } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
          notificationContext.add(
            {
              type: 'ADD_RECORDS_ERROR',
              content: {
                dataflowId,
                dataflowName,
                datasetId,
                datasetName,
                customContent: { tableName: webformData.title }
              }
            },
            true
          );
        }
        webformTableDispatch({
          type: 'SET_IS_ADDING_MULTIPLE',
          payload: { addingOnTableSchemaId: null, isAddingMultiple: false }
        });
      } finally {
        bigData && onFieldUpdate(false);
      }
    }
  };

  const onLoadTableData = async () => {
    setIsLoading(true);

    webform?.elements?.forEach(table => {
      if (table.type === 'TABLE') {
        if (!table.dataAreManuallyEditable) setAllManualCheck(false);
      }
    });

    try {
      const { fieldSchema, fieldId } = getFieldSchemaId([webform], webform.tableSchemaId, rootPkFieldId, rootTableName);
      let data;
      if (bigData) {
        data = await DatasetService.getTableDataDL({
          datasetId,
          tableSchemaId: webform.tableSchemaId,
          pageNum: '',
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
          fieldSchemaId: fieldSchema || fieldId,
          value: selectedTable.rootTableId
        });
      } else {
        data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: webform.tableSchemaId,
          pageNum: '',
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
          fieldSchemaId: fieldSchema || fieldId,
          value: selectedTable.rootTableId
        });
      }
      if (!isNil(data.records)) {
        const tables = getTableElements(webform);
        const tableSchemaIds = tables.map(table => table.tableSchemaId);

        const tableData = {};

        for (let index = 0; index < tableSchemaIds.length; index++) {
          const tableSchemaId = tableSchemaIds[index];
          const { fieldSchema, fieldId } = getFieldSchemaId(tables, tableSchemaId, rootPkFieldId, rootTableName);
          if (bigData) {
            tableData[tableSchemaId] = await DatasetService.getTableDataDL({
              datasetId,
              tableSchemaId,
              levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
              fieldSchemaId: fieldSchema || fieldId,
              value: selectedTable.rootTableId
            });
          } else {
            tableData[tableSchemaId] = await DatasetService.getTableData({
              datasetId,
              tableSchemaId,
              levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
              fieldSchemaId: fieldSchema || fieldId,
              value: selectedTable.rootTableId
            });
          }
        }
        const records = onParseWebformRecords(
          data.records,
          webform,
          tableData,
          data.totalRecords,
          rootTableName,
          rootPkFieldId
        );

        if (webform?.isOptional && isEmpty(data.records)) {
          setOptionalAddRecordEnabled(true);
        } else {
          setOptionalAddRecordEnabled(false);
        }

        webformTableDispatch({ type: 'ON_LOAD_DATA', payload: { records } });
      }
    } catch (error) {
      console.error('WebformTable - onLoadTableData.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
      notificationContext.add(
        {
          type: 'TABLE_DATA_BY_ID_ERROR',
          content: { dataflowId, dataflowName, datasetId, datasetName }
        },
        true
      );
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
      bigData={bigData}
      columnsSchema={webformData.elementsRecords[0] ? webformData.elementsRecords[0].elements : []}
      dataflowId={dataflowId}
      dataProviderId={dataProviderId}
      datasetId={datasetId}
      datasetSchemaId={datasetSchemaId}
      hasFields={isNil(webformData.records) || isEmpty(webformData.records[0].fields)}
      isAddingMultiple={webformTableState.isAddingMultiple}
      isFixedNumber={webformData.fixedNumber || webformData.tableSchemaFixedNumber || null}
      isOptional={webformData.isOptional}
      isReporting={isReporting}
      isViewMode={isViewMode}
      key={index}
      multipleRecords={webformData.multipleRecords}
      onAddMultipleWebform={onAddMultipleWebform}
      onFieldUpdate={onFieldUpdate}
      onRefresh={onUpdateData}
      onTabChange={onTabChange}
      record={record}
      rootPkFieldId={rootPkFieldId}
      rootTableName={rootTableName}
      tableId={webformData.tableSchemaId}
      tableName={webformData.title}
      tableSchemaName={webformData.name}
      updatingField={updatingField}
      webformType={webformType}
    />
  );

  const renderWebform = isMultiple => {
    const { elementsRecords } = webformData;

    if (!isMultiple) {
      return renderWebformRecord(elementsRecords[0], null);
    } else {
      if (elementsRecords.length > 0) {
        return elementsRecords.map((record, index) => renderWebformRecord(record, index));
      } else {
        return renderWebformRecord(elementsRecords[0], null);
      }
    }
  };

  const validationsTemplate = recordData => {
    return ErrorUtils.getValidationsTemplate(recordData, {
      blockers: resourcesContext.messages['recordBlockers'],
      errors: resourcesContext.messages['recordErrors'],
      warnings: resourcesContext.messages['recordWarnings'],
      infos: resourcesContext.messages['recordInfos']
    });
  };

  if (isLoadingIceberg) {
    if (webformTableState.isLoading) {
      return <Spinner style={{ top: 0, margin: '1rem' }} />;
    } else {
      return (
        <div style={{ top: 0, margin: '1rem' }}>
          <Spinner style={{ top: 0, margin: '1rem' }} />
          <p style={{ position: 'absolute', left: '50%', transform: 'translateX(-50%)', margin: 0 }}>
            {resourcesContext.messages['tablesAreBeingConverted']}
          </p>
        </div>
      );
    }
  }

  return (
    <div className={styles.contentWrap}>
      {webform?.multipleRecords || (webform?.isOptional && optionalAddRecordEnabled) ? (
        <>
          <h3 className={styles.title}>
            <Button
              className={styles.addRecordButton}
              disabled={isViewMode || updatingField.isUpdating}
              icon={webformTableState.isAddingMultiple ? 'spinnerAnimate' : 'add'}
              label={resourcesContext.messages['addRecord']}
              onClick={() => onAddMultipleWebform(webformData.tableSchemaId, null, true)}
            />
          </h3>

          <div className={`${styles.wrapper} ${isSticky ? styles.stickyWrapper : styles.initialWrapper}`}>
            <h3 className={styles.title}>
              <div>
                {webformData.title
                  ? `${webformData.title}${webformData.subtitle ? `: ${webform.subtitle}` : ''}`
                  : webformData.name}
                {validationsTemplate(parseRecordsValidations(webformData.elementsRecords)[0])}
              </div>
            </h3>
          </div>
        </>
      ) : (
        <div className={`${styles.wrapper} ${isSticky ? styles.stickyWrapper : styles.initialWrapper}`}>
          <h3 className={styles.title}>
            <div>
              {webformData.title
                ? `${webformData.title}${webformData.subtitle ? `: ${webform.subtitle}` : ''}`
                : webformData.name}
              {validationsTemplate(parseRecordsValidations(webformData.elementsRecords)[0])}
            </div>
          </h3>
        </div>
      )}
      <div className={styles.overlay}>
        <div
          style={
            bigData && (isLoadingIceberg || !allManualCheck)
              ? { opacity: 0.5, pointerEvents: 'none' }
              : isEditor || isViewMode || updatingField.isUpdating
              ? { opacity: 1 }
              : { opacity: 0.5, pointerEvents: 'none' }
          }>
          {isNil(webformData.tableSchemaId) && (
            <span
              className={styles.nonExistTable}
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['tableIsNotCreated'], {
                  tableName: webformData.name
                })
              }}
            />
          )}
          {!isNil(webformData.elementsRecords) && renderWebform(webformData.multipleRecords)}
        </div>
      </div>
    </div>
  );
};
