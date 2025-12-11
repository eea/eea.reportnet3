import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';

import styles from './EntitiesWebform.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { InputText } from 'views/_components/InputText';
import { TableManagement } from './_components/TableManagement';
import { WebformView } from './_components/WebformView';

import { DatasetService } from 'services/DatasetService';
import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { entitiesWebformReducer } from './_functions/Reducers/entitiesWebformReducer';

import { MetadataUtils } from 'views/_functions/Utils';
import { EntitiesWebformUtils } from './_functions/Utils/EntitiesWebformUtils';
import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const EntitiesWebform = ({
  bigData,
  dataflowId,
  dataProviderId,
  datasetId,
  hideEntities,
  isIcebergCreated,
  isReleasing,
  isReporting,
  overview,
  rootPkFieldId,
  rootTableId,
  rootTableName,
  state,
  tables = []
}) => {
  const { checkErrors, getFieldSchemaId, getTypeList, hasErrors } = EntitiesWebformUtils;
  const { datasetSchema, datasetStatistics } = state;
  const { onParseWebformData, onParseWebformRecords, parseNewEntitiesTableRecord } = WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const [refreshTableTrigger, setRefreshTableTrigger] = useState(0);
  const [hasLoadedEntities, setHasLoadedEntities] = useState(false);

  const [entitiesWebformState, entitiesWebformDispatch] = useReducer(entitiesWebformReducer, {
    data: [],
    hasErrors: true,
    isAddEntityIdDialogVisible: false,
    isAddingGroupRecord: false,
    isAddingEntityRecord: false,
    isCheckingDuplicate: false,
    isDataUpdated: false,
    isDuplicatePkDialogVisible: false,
    isLoading: true,
    isRefresh: false,
    updatingField: { field: null, isUpdating: false },
    isViewMode: false,
    entitiesRecords: [],
    selectedTable: { fieldSchemaId: null, rootTableId: null, recordId: null, tableName: null },
    rootPkInput: '',
    selectedTableSchemaId: null,
    entitiesList: [],
    view: 'overview'
  });
  const { isDataUpdated, isLoading, entitiesRecords, selectedTable, entitiesList, view } = entitiesWebformState;

  const addEntityInputRef = useRef(null);

  useEffect(() => initialLoad(), [tables]);

  useEffect(() => {
    const matchedNotifications = notificationContext.hidden.filter(
      ({ key }) => key === 'INSERT_RECORDS_MULTI_TABLES_COMPLETED' || key === 'INSERT_RECORDS_MULTI_TABLES_FAILED'
    );

    if (isEmpty(matchedNotifications)) return;

    const matchedWithDatasetId = matchedNotifications.find(
      matchedNotification => String(matchedNotification.content?.datasetId) === String(datasetId)
    );

    if (!matchedWithDatasetId) return;

    const resetAddEntityState = () => {
      setIsAddingEntityRecord(false);
      manageDialogs('isAddEntityIdDialogVisible', false);
      setRefreshTableTrigger(prev => prev + 1);
    };

    if (matchedWithDatasetId?.key === 'INSERT_RECORDS_MULTI_TABLES_COMPLETED') {
      onUpdateData();
      resetAddEntityState();
    } else if (matchedWithDatasetId?.key === 'INSERT_RECORDS_MULTI_TABLES_FAILED') {
      resetAddEntityState();
    }
  }, [notificationContext.hidden]);

  useEffect(() => {
    if (!isEmpty(entitiesWebformState.data) && !hasLoadedEntities) {
      if (!hideEntities) {
        onLoadEntitiesData();
        setHasLoadedEntities(true);
      }
      entitiesWebformDispatch({
        type: 'HAS_ERRORS',
        payload: { value: hasErrors(entitiesWebformState.data, rootPkFieldId) }
      });
    }
  }, [entitiesWebformState.data, isDataUpdated, hasLoadedEntities]);

  useEffect(() => {
    setIsAddingEntityRecord(false);
  }, [entitiesList]);

  useEffect(() => {
    const { fieldId, fieldSchema } = getFieldSchemaId(
      entitiesWebformState.data,
      entitiesWebformState.selectedTableSchemaId,
      rootPkFieldId,
      rootTableName
    );

    onSelectFieldSchemaId(fieldSchema || fieldId);
  }, [entitiesWebformState.data, entitiesWebformState.selectedTableSchemaId]);

  useEffect(() => {
    if (isDataUpdated)
      entitiesWebformDispatch({
        type: 'UPDATE_DATA',
        payload: { data: onLoadData() }
      });
  }, [isDataUpdated]);

  const checkInvalidCharacters = () => {
    const invalidCharsRegex = new RegExp(/^[^a-zA-Z0-9:/._-]|[^a-zA-Z0-9:/._-]/);
    return isEmpty(entitiesWebformState.rootPkInput) ? true : invalidCharsRegex.test(entitiesWebformState.rootPkInput);
  };

  const checkDuplicatePk = async rawValue => {
    if (isEmpty(rawValue)) return false;

    // Find the root table
    const rootTable = entitiesWebformState.data.find(table => String(table.tableSchemaId) === String(rootTableId));

    if (!rootTable) {
      console.error('EntitiesWebform - checkDuplicatePk: Root table not found');
      return false;
    }

    // Find the root PK field name from datasetSchema
    const schemaRootTable = datasetSchema.tables.find(table => String(table.tableSchemaId) === String(rootTableId));

    if (!schemaRootTable?.records?.[0]?.fields) {
      console.error('EntitiesWebform - checkDuplicatePk: Schema root table records not found');
      return false;
    }

    const rootPkField = schemaRootTable.records[0].fields.find(
      field => String(field.fieldId ?? field.fieldSchemaId) === String(rootPkFieldId)
    );

    if (!rootPkField?.name) {
      console.error('EntitiesWebform - checkDuplicatePk: Root PK field name not found');
      return false;
    }

    try {
      const exists = await DatasetService.checkDuplicateValues({
        datasetId,
        tableSchemaId: rootTable.tableSchemaId,
        fieldName: rootPkField.name,
        value: rawValue,
        fieldSchemaId: rootPkFieldId
      });
      return Boolean(exists.data);
    } catch (error) {
      console.error('EntitiesWebform - checkDuplicatePk.', error);
      return true;
    }
  };

  const initialLoad = () => {
    entitiesWebformDispatch({
      type: 'INITIAL_LOAD',
      payload: { data: onLoadData() }
    });
  };

  const manageDialogs = (dialog, value) => {
    entitiesWebformDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, rootPkInput: '' }
    });
  };

  const setIsLoading = value => entitiesWebformDispatch({ type: 'IS_LOADING', payload: { value } });

  const setTableSchemaId = tableSchemaId => {
    entitiesWebformDispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const onAddEntitiesRecord = async manualRootPk => {
    setIsAddingEntityRecord(true);

    /*Filters the webform main tables that are not optional and leaves out the subtables*/
    const autoIncrementFields = [];

    // Add fields from table elements that have autoIncrement = true
    tables.forEach(table => {
      if (table.elements) {
        const incrementFields = table.elements.filter(element => element.autoIncrement).map(element => element.name);
        autoIncrementFields.push(...incrementFields);
      }

      // Add root table primary key
      if (table.isRootTable) {
        autoIncrementFields.push('Id');
      }
    });

    // Add foreign key fields that reference the root table
    const rootTableFKFields = [];

    // Find FK fields in datasetSchema that reference the root table
    datasetSchema.tables.forEach(table => {
      if (table.records && table.records[0] && table.records[0].fields) {
        table.records[0].fields.forEach(field => {
          // Check if this field references the root table's primary key
          if (field.referencedField && field.referencedField.idPk === rootPkFieldId) {
            rootTableFKFields.push(field.name);
          }
        });
      }
    });
    // Add FK field names to autoIncrementFields
    autoIncrementFields.push(...rootTableFKFields);
    // Remove duplicates
    const uniqueAutoIncrementFields = [...new Set(autoIncrementFields)];

    const filteredMainTables = datasetSchema.tables.filter(
      table =>
        table.tableSchemaNotEmpty &&
        tables.some(webformTable => webformTable?.name === table?.tableSchemaName && !webformTable?.isOptional)
    );

    try {
      await WebformService.addEntityRecord(
        datasetId,
        filteredMainTables,
        manualRootPk ? entitiesWebformState.rootPkInput : undefined,
        rootPkFieldId,
        !isEmpty(uniqueAutoIncrementFields) ? uniqueAutoIncrementFields : undefined
      );

      if (!bigData) {
        onUpdateData();
      }
    } catch (error) {
      if (!bigData) {
        if (error?.response?.status === 423) {
          notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
        } else {
          console.error('EntitiesWebform - onAddEntitiesRecord.', error);
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
                customContent: { tableName: '' }
              }
            },
            true
          );
        }
      }
    } finally {
      if (!bigData) {
        setIsAddingEntityRecord(false);
        manageDialogs('isAddEntityIdDialogVisible', false);
        setRefreshTableTrigger(prev => prev + 1);
      }
    }
  };

  const onAddTableRecord = async (table, entityNumber) => {
    const newEmptyRecord = parseNewEntitiesTableRecord(table, entityNumber, rootPkFieldId);

    try {
      await DatasetService.createRecord(datasetId, table.tableSchemaId, [newEmptyRecord]);
      onUpdateData();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' }, true);
      } else {
        console.error('EntitiesWebform - onAddTableRecord.', error);
        const {
          dataflow: { name: dataflowName },
          dataset: { name: datasetName }
        } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
        notificationContext.add(
          {
            type: 'ADD_RECORDS_ERROR',
            content: { dataflowId, datasetId, dataflowName, datasetName, customContent: { tableName: '' } }
          },
          true
        );
      }
    }
  };

  const onAddEntityInputChange = value =>
    entitiesWebformDispatch({ type: 'ON_ADD_ENTITY_INPUT_CHANGE', payload: { rootPkInput: value } });

  const onLoadData = () => {
    if (!isEmpty(datasetSchema)) {
      const data = onParseWebformData(datasetSchema, tables, datasetSchema.tables, datasetStatistics);
      return data;
    }
  };

  const onLoadEntitiesData = async () => {
    const tableSchemaId = entitiesWebformState.data.map(table => table.tableSchemaId).filter(table => !isNil(table));
    try {
      let data;
      if (!isNil(tableSchemaId[0])) {
        if (bigData) {
          data = await DatasetService.getTableDataDL({
            datasetId,
            tableSchemaId: tableSchemaId[0],
            pageSize: 300,
            levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
          });
        } else {
          data = await DatasetService.getTableData({
            datasetId,
            tableSchemaId: tableSchemaId[0],
            pageSize: 300,
            levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
          });
        }

        if (!isNil(data.records)) {
          const tableData = {};

          const records = onParseWebformRecords(
            data.records,
            entitiesWebformState.data[0],
            tableData,
            data.totalRecords
          );
          const list = getTypeList(records);

          entitiesWebformDispatch({
            type: 'ON_LOAD_ENTITIES_DATA',
            payload: { records, list }
          });
        }
      }
    } catch (error) {
      console.error('EntitiesWebform - onLoadEntitiesData.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getFirstVisibleTable = () => {
    const visibleTables = entitiesWebformState.data.filter(table =>
      tables.some(webformTable => webformTable.name === table.name && webformTable.isVisible)
    );
    return visibleTables[0] || null;
  };

  const changeViewMode = isViewMode => {
    entitiesWebformDispatch({ type: 'SET_IS_VIEW_MODE', payload: { value: isViewMode } });
  };

  const onFieldUpdate = (isFieldUpdating, field) => {
    entitiesWebformDispatch({
      type: 'SET_IS_UPDATING_FIELD',
      payload: {
        value: isFieldUpdating,
        field
      }
    });
  };

  const onSelectEditTable = (entityNumberId, tableName, recordId, isViewMode = false) => {
    // const filteredTable = entitiesWebformState.data.filter(table => TextUtils.areEquals(table.name, tableName))[0];

    const filteredTable = getFirstVisibleTable();

    if (!filteredTable) {
      console.error('No visible tables found for editing');
      return;
    }
    setTableSchemaId(filteredTable.tableSchemaId);
    onSelectRecord(recordId, entityNumberId);
    onSelectTableName(filteredTable.name);
    onToggleView('details');
    changeViewMode(isViewMode);
  };

  const onSelectViewTable = (entityNumberId, tableName, recordId) => {
    onSelectEditTable(entityNumberId, tableName, recordId, true);
  };

  const onSelectFieldSchemaId = fieldSchemaId => {
    entitiesWebformDispatch({ type: 'ON_SELECT_SCHEMA_ID', payload: { fieldSchemaId } });
  };

  const onSelectRecord = (recordId, rootTableId) => {
    entitiesWebformDispatch({ type: 'ON_SELECT_RECORD', payload: { recordId, rootTableId } });
  };

  const onSelectTableName = name => entitiesWebformDispatch({ type: 'ON_SELECT_TABLE', payload: { name } });

  const onToggleView = view => entitiesWebformDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => {
    setHasLoadedEntities(false);
    entitiesWebformDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });
  };
  const setIsAddingEntityRecord = value =>
    entitiesWebformDispatch({ type: 'SET_IS_ADDING_ENTITY_RECORD', payload: { value } });

  const handleAddEntity = async () => {
    entitiesWebformDispatch({
      type: 'SET_IS_CHECKING_DUPLICATE',
      payload: { value: true }
    });

    const exists = await checkDuplicatePk(entitiesWebformState.rootPkInput);

    entitiesWebformDispatch({
      type: 'SET_IS_CHECKING_DUPLICATE',
      payload: { value: false }
    });

    if (exists) {
      manageDialogs('isDuplicatePkDialogVisible', true);
    } else if (!checkInvalidCharacters()) {
      onAddEntitiesRecord(true);
    }
  };

  const renderOverviewButton = () => {
    if (view !== 'details') {
      return <div />;
    }

    return (
      <div className={styles.overviewButton}>
        <Button
          label={resourcesContext.messages['overview']}
          onClick={() => {
            onToggleView('overview');
            onSelectRecord(null, null);
            changeViewMode(false);
          }}
        />
      </div>
    );
  };

  const renderView = () => {
    if (view === 'details') {
      return (
        <WebformView
          bigData={bigData}
          data={entitiesWebformState.data}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          datasetSchema={datasetSchema}
          datasetSchemaId={datasetSchema.datasetSchemaId}
          getFieldSchemaId={getFieldSchemaId}
          isAddingRootTableId={entitiesWebformState.isAddingEntityRecord}
          isIcebergCreated={isIcebergCreated}
          isRefresh={entitiesWebformState.isRefresh}
          isReporting={isReporting}
          isViewMode={entitiesWebformState.isViewMode}
          onFieldUpdate={onFieldUpdate}
          rootPkFieldId={rootPkFieldId}
          rootTableName={rootTableName}
          selectedTable={selectedTable}
          setTableSchemaId={setTableSchemaId}
          state={state}
          tables={tables.filter(table => table.isVisible)}
          updatingField={entitiesWebformState.updatingField}
        />
      );
    }

    return (
      <TableManagement
        bigData={bigData}
        dataflowId={dataflowId}
        datasetId={datasetId}
        disableActionButtons={bigData && !isIcebergCreated}
        isAddingRootTableId={entitiesWebformState.isAddingEntityRecord}
        isIcebergCreated={isIcebergCreated}
        loading={isLoading}
        onAddTableRecord={onAddTableRecord}
        onRefresh={onUpdateData}
        onSelectEditTable={onSelectEditTable}
        onSelectViewTable={onSelectViewTable}
        overview={overview}
        records={entitiesRecords}
        refreshTrigger={refreshTableTrigger}
        rootPkFieldId={rootPkFieldId}
        rootTableId={rootTableId}
        rootTableName={rootTableName}
        schemaTables={datasetSchema.tables}
        tables={tables}
        view={view}
      />
    );
  };

  const renderErrorMessages = () => {
    const missingElements = checkErrors(entitiesWebformState.data, rootPkFieldId);

    return (
      <Fragment>
        <h4 className={styles.title}>{resourcesContext.messages['missingWebformTablesOrFieldsMissing']}</h4>
        <div className={styles.missingElements}>
          {Object.keys(missingElements).map(key => {
            const { fields, table } = missingElements[key];

            return (
              fields.some(field => field.isMissing) && (
                <div key={uniqueId()}>
                  <span className={styles.tableTitle}>
                    <FontAwesomeIcon icon={AwesomeIcons('table')} /> {table.name}
                  </span>
                  <ul>{fields.map(field => field.isMissing && <li key={uniqueId()}> {field.name}</li>)}</ul>
                </div>
              )
            );
          })}
        </div>
      </Fragment>
    );
  };

  const renderLayout = children => (
    <Fragment>
      <h2 className={styles.title}>
        <FontAwesomeIcon icon={AwesomeIcons('exclamationTriangle')} />
        <strong> {resourcesContext.messages['webformEntitiesTitle']}</strong>
      </h2>
      {children}
      {entitiesWebformState.isAddEntityIdDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-primary'}
          disabledConfirm={
            checkInvalidCharacters() ||
            entitiesWebformState.isAddingEntityRecord ||
            entitiesWebformState.isCheckingDuplicate
          }
          header={resourcesContext.messages['addEntityId']}
          iconConfirm={
            entitiesWebformState.isAddingEntityRecord || entitiesWebformState.isCheckingDuplicate
              ? 'spinnerAnimate'
              : 'add'
          }
          labelConfirm={
            entitiesWebformState.isCheckingDuplicate
              ? resourcesContext.messages['checkingDuplicate']
              : resourcesContext.messages['addEntity']
          }
          onConfirm={handleAddEntity}
          onHide={() => manageDialogs('isAddEntityIdDialogVisible', false)}
          showCancelButton={false}
          visible={entitiesWebformState.isAddEntityIdDialogVisible}>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resourcesContext.messages['addEntityIdMessage'])
            }}></p>
          <InputText
            autoFocus={true}
            className={styles.inputText}
            disabled={entitiesWebformState.isViewMode}
            id={'addEntity'}
            maxLength={config.INPUT_MAX_LENGTH}
            onChange={event => onAddEntityInputChange(event.target.value)}
            ref={addEntityInputRef}
            value={entitiesWebformState.rootPkInput}
          />
        </ConfirmDialog>
      )}
      {entitiesWebformState.isDuplicatePkDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-primary'}
          disabledCancel={true}
          header={resourcesContext.messages['duplicateEntityId']}
          labelConfirm={resourcesContext.messages['ok']}
          onConfirm={() => manageDialogs('isDuplicatePkDialogVisible', false)}
          onHide={() => manageDialogs('isDuplicatePkDialogVisible', false)}
          showCancelButton={false}
          visible={entitiesWebformState.isDuplicatePkDialogVisible}>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resourcesContext.messages['duplicateEntityIdMessage'])
            }}></p>
        </ConfirmDialog>
      )}
    </Fragment>
  );

  if (entitiesWebformState.hasErrors) {
    return renderLayout(renderErrorMessages());
  }

  return renderLayout(
    <Fragment>
      {hideEntities && view === 'overview' ? (
        <div className={styles.hiddenEntitiesAddButton}>
          <Button
            className={styles.addButton}
            disabled={
              (bigData && !isIcebergCreated) + entitiesWebformState.isAddingEntityRecord ||
              isReleasing ||
              entitiesWebformState.isViewMode
            }
            icon={entitiesWebformState.isAddingEntityRecord ? 'spinnerAnimate' : 'add'}
            label={resourcesContext.messages['addEntity']}
            onClick={() => {
              const manualRootId = tables
                .filter(table => table?.isRootTable === true)[0]
                .elements.some(element => element?.autoIncrement === false && element?.isPrimary === true);

              if (manualRootId) {
                manageDialogs('isAddEntityIdDialogVisible', true);
              } else {
                onAddEntitiesRecord();
              }
            }}
          />
        </div>
      ) : (
        !hideEntities && (
          <ul className={styles.tableList}>
            <li className={styles.tableListItem} key={uniqueId()}>
              <div className={styles.tableListTitleWrapper}>
                <span className={styles.tableListTitle}>{resourcesContext.messages['entitiesLabel']}:</span>
              </div>
              <div className={styles.tableListContentWrapper}>
                {entitiesList.map(items => (
                  <span
                    className={`${styles.tableListId} ${
                      items.recordId === selectedTable.recordId ? styles.selected : null
                    }`}
                    key={uniqueId()}
                    onClick={() => {
                      if (!(bigData && !isIcebergCreated)) {
                        entitiesWebformDispatch({
                          type: 'ON_REFRESH',
                          payload: { value: !entitiesWebformState.isRefresh }
                        });
                        onSelectRecord(items.recordId, items.id);
                        onToggleView('details');
                      } else if (bigData && !isIcebergCreated) {
                        entitiesWebformDispatch({
                          type: 'ON_REFRESH',
                          payload: { value: !entitiesWebformState.isRefresh }
                        });
                        onSelectRecord(items.recordId, items.id);
                        onToggleView('details');
                        changeViewMode(true);
                      }
                    }}>
                    {items.id || '-'}
                  </span>
                ))}
              </div>
              <div className={styles.addButtonWrapper}>
                <Button
                  className={styles.addButton}
                  disabled={
                    (bigData && !isIcebergCreated) + entitiesWebformState.isAddingEntityRecord ||
                    isReleasing ||
                    entitiesWebformState.isViewMode
                  }
                  icon={entitiesWebformState.isAddingEntityRecord ? 'spinnerAnimate' : 'add'}
                  label={resourcesContext.messages['addEntity']}
                  onClick={() => {
                    const manualRootId = tables
                      .filter(table => table?.isRootTable === true)[0]
                      .elements.some(element => element?.autoIncrement === false && element?.isPrimary === true);

                    if (manualRootId) {
                      manageDialogs('isAddEntityIdDialogVisible', true);
                    } else {
                      onAddEntitiesRecord();
                    }
                  }}
                />
              </div>
            </li>
          </ul>
        )
      )}

      {renderOverviewButton()}

      {renderView()}
    </Fragment>
  );
};
