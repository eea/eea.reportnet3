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
  calculateSingle,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  getFieldSchemaId = () => ({ fieldSchema: undefined, fieldId: undefined }),
  isGroup,
  isIcebergCreated,
  isLoadingIceberg,
  isRefresh,
  isReporting,
  onTabChange,
  onUpdatePamsValue,
  onUpdateSinglesList,
  pamsRecords,
  selectedTable = { fieldSchemaId: null, pamsId: undefined, recordId: null, tableName: null },
  setIsLoading = () => {},
  webform,
  webformType
}) => {
  const {
    onParseWebformRecords,
    parseNewTableRecord,
    parseNewTableRecordTable,
    parseOtherObjectivesRecord,
    parseRecordsValidations
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

  // const checkIsIcebergCreated = async tableId => {
  //   setIsLoadingIceberg(true);
  //   let { data } = await DatasetService.getIsIcebergTableCreated({
  //     datasetId,
  //     tableSchemaId: tableId
  //   });
  //   setIsIcebergCreated(data);
  //   setIsLoadingIceberg(false);
  // };

  useEffect(() => {
    webformTableDispatch({ type: 'INITIAL_LOAD', payload: { webformData: { ...webform } } });
  }, [webform]);

  // useEffect(() => {
  //   checkIsIcebergCreated(webformData.tableSchemaId);
  // }, [webformData]);

  useEffect(() => {
    if (!isNil(webform) && isNil(webform.tableSchemaId)) isLoading(false);

    if (!isNil(webform) && webform.tableSchemaId) {
      if (webformType === 'PAMS' && !isNil(selectedTable.pamsId)) {
        isLoading(true);
        onLoadTableData();
      } else if (webformType === 'TABLES') {
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

  const onAddMultipleWebform = async (tableSchemaId, filteredRecordId = null, mainTable = false) => {
    webformTableDispatch({
      type: 'SET_IS_ADDING_MULTIPLE',
      payload: { isAddingMultiple: true, addingOnTableSchemaId: tableSchemaId }
    });

    let newEmptyRecord;

    if (!isEmpty(webformData.elementsRecords)) {
      if (webformType === 'PAMS') {
        let sectorObjectivesTable;
        const filteredTable = getTableElements(webformData.elementsRecords[0]).filter(element => {
          if (TextUtils.areEquals(element.name, 'SectorObjectives')) {
            sectorObjectivesTable = element;
          }
          return element.tableSchemaId === tableSchemaId;
        })[0];

        newEmptyRecord = TextUtils.areEquals(filteredTable.name, 'OtherObjectives')
          ? parseOtherObjectivesRecord(filteredTable, sectorObjectivesTable, selectedTable.pamsId, filteredRecordId)
          : parseNewTableRecord(filteredTable, selectedTable.pamsId, sectorObjectivesTable);
      } else {
        if (!mainTable) {
          const filteredTable = getTableElements(webformData.elementsRecords[0]).filter(
            element => element.tableSchemaId === tableSchemaId
          )[0];
          newEmptyRecord = parseNewTableRecordTable(filteredTable);
        }
      }
    }

    if (mainTable) {
      newEmptyRecord = parseNewTableRecordTable(webformData);
    }

    if (!isEmpty(newEmptyRecord)) {
      try {
        await DatasetService.createRecord(datasetId, tableSchemaId, [newEmptyRecord]);
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
      }
    }
  };

  const onLoadTableData = async () => {
    setIsLoading(true);
    try {
      const { fieldSchema, fieldId } = getFieldSchemaId([webform], webform.tableSchemaId);
      let data;
      if (bigData) {
        data = await DatasetService.getTableDataDL({
          datasetId,
          tableSchemaId: webform.tableSchemaId,
          pageNum: '',
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
          fieldSchemaId: fieldSchema || fieldId,
          value: selectedTable.pamsId
        });
      } else {
        data = await DatasetService.getTableData({
          datasetId,
          tableSchemaId: webform.tableSchemaId,
          pageNum: '',
          pageSize: 300,
          levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
          fieldSchemaId: fieldSchema || fieldId,
          value: selectedTable.pamsId
        });
      }

      if (!isNil(data.records)) {
        const tables = getTableElements(webform);
        const tableSchemaIds = tables.map(table => table.tableSchemaId);

        const tableData = {};

        for (let index = 0; index < tableSchemaIds.length; index++) {
          const tableSchemaId = tableSchemaIds[index];
          const { fieldSchema, fieldId } = getFieldSchemaId(tables, tableSchemaId);
          if (bigData) {
            tableData[tableSchemaId] = await DatasetService.getTableDataDL({
              datasetId,
              tableSchemaId,
              levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
              fieldSchemaId: fieldSchema || fieldId,
              value: selectedTable.pamsId
            });
          } else {
            tableData[tableSchemaId] = await DatasetService.getTableData({
              datasetId,
              tableSchemaId,
              levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER'],
              fieldSchemaId: fieldSchema || fieldId,
              value: selectedTable.pamsId
            });
          }
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
      calculateSingle={calculateSingle}
      columnsSchema={webformData.elementsRecords[0] ? webformData.elementsRecords[0].elements : []}
      dataflowId={dataflowId}
      dataProviderId={dataProviderId}
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

  const renderTableWebformRecords = isMultiple => {
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

  const renderPaMsWebformRecords = () => {
    return renderWebformRecord(webformData.elementsRecords[0], null);
  };

  const renderWebform = isMultiple => {
    switch (webformType) {
      case 'TABLES':
        return renderTableWebformRecords(isMultiple);
      case 'PAMS':
        return renderPaMsWebformRecords();
      default:
        return <div />;
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

  // const convertHelper = () => {
  //   isLoading(true);
  //   setIsLoadingIceberg(true);
  //   let tableArray = webformData?.elements?.filter(el => el?.type === 'TABLE');
  //   let tableSchemaIds = [];
  //   if (tableArray?.length < 1) {
  //     tableSchemaIds[0] = webformData?.tableSchemaId;
  //   } else {
  //     tableSchemaIds = tableArray.map(record => record.tableSchemaId);
  //   }

  //   convertTables(encodeURIComponent(tableSchemaIds));
  // };

  // const convertTables = async tableIds => {
  //   try {
  //     if (isIcebergCreated) {
  //       if (dataProviderId) {
  //         await DatasetService.convertIcebergsToParquets({
  //           datasetId,
  //           dataflowId,
  //           providerId: dataProviderId,
  //           tableSchemaIds: tableIds
  //         });
  //         setIsIcebergCreated(false)
  //       } else {
  //         await DatasetService.convertIcebergsToParquets({
  //           datasetId,
  //           dataflowId,
  //           tableSchemaIds: tableIds
  //         });
  //         setIsIcebergCreated(false)
  //       }
  //     } else {
  //       if (dataProviderId) {
  //         await DatasetService.convertParquetsToIcebergs({
  //           datasetId,
  //           dataflowId,
  //           providerId: dataProviderId,
  //           tableSchemaIds: tableIds
  //         });
  //         setIsIcebergCreated(true)
  //       } else {
  //         await DatasetService.convertParquetsToIcebergs({
  //           datasetId,
  //           dataflowId,
  //           tableSchemaIds: tableIds
  //         });
  //         setIsIcebergCreated(true)
  //       }
  //     }
  //   } catch (error) {
  //     console.error('ActionsToolbar - convertTable.', error);
  //     notificationContext.add({ type: 'CONVERT_TABLE_ERROR' }, true);
  //   }
  //   setIsLoadingIceberg(false);
  //   isLoading(false);
  // };

  return (
    <div className={styles.contentWrap}>
      {webform?.multipleRecords ? (
        <>
          <h3 className={styles.title}>
            <Button
              className={styles.addRecordButton}
              icon="plus"
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
            {/* <Button
              helpClassName={isIcebergCreated && 'p-button-reverse'}
              icon={isIcebergCreated ? 'unlock' : 'lock'}
              label={isIcebergCreated ? 'Close Webform' : 'Open Webform'}
              className={styles.openWebformButton}
              onClick={() => convertHelper()}
              isLoading={isLoadingIceberg}
            /> */}
          </h3>
        </div>
      )}
      <div className={styles.overlay}>
        <div
          style={
            isLoadingIceberg
              ? { opacity: 0.5, pointerEvents: 'none' }
              : !bigData || isIcebergCreated
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
