import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

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
  const { onParseWebformData, onParseWebformRecords, parseNewEntitiesTableRecord, parseEntitiesRecords } =
    WebformsUtils;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [entitiesWebformState, entitiesWebformDispatch] = useReducer(entitiesWebformReducer, {
    data: [],
    hasErrors: true,
    isAddEntityIdDialogVisible: false,
    isAddingGroupRecord: false,
    isAddingEntityRecord: false,
    isDataUpdated: false,
    isDuplicatePkDialogVisible: false,
    isLoading: true,
    isRefresh: false,
    entitiesRecords: [],
    selectedTable: { fieldSchemaId: null, rootTableId: null, recordId: null, tableName: null },
    rootPkInput: '',
    selectedTableName: null,
    selectedTableSchemaId: null,
    entitiesList: [],
    view: 'overview'
  });
  const { isDataUpdated, isLoading, entitiesRecords, selectedTable, selectedTableName, entitiesList, view } =
    entitiesWebformState;

  const addEntityInputRef = useRef(null);

  useEffect(() => initialLoad(), [tables]);

  useEffect(() => {
    if (!isEmpty(entitiesWebformState.data)) {
      onLoadEntitiesData();
      entitiesWebformDispatch({
        type: 'HAS_ERRORS',
        payload: { value: hasErrors(entitiesWebformState.data, rootPkFieldId) }
      });
    }
  }, [entitiesWebformState.data, isDataUpdated]);

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

  const checkDuplicatePk = () => {
    const entityPkValuesList = entitiesWebformState.entitiesRecords
      .map(entityRecord =>
        entityRecord.elements
          .filter(element => element.fieldSchema === rootPkFieldId || element.fieldSchemaId === rootPkFieldId)
          .map(element => element.value)
      )
      .filter(entityRecord => !isEmpty(entityRecord))
      .flat();

    return entityPkValuesList.some(entityPk => entityPk === entitiesWebformState.rootPkInput);
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

  const generateEntityId = entitiesTableRecords => {
    if (isEmpty(entitiesTableRecords)) return 1;

    const recordIds = parseEntitiesRecords(entitiesTableRecords)
      .map(record => parseInt(record.Id) || parseInt(record.id))
      .filter(id => !Number.isNaN(id));

    return Math.max(...recordIds) + 1;
  };

  const setTableSchemaId = tableSchemaId => {
    entitiesWebformDispatch({ type: 'GET_TABLE_SCHEMA_ID', payload: { tableSchemaId } });
  };

  const getEntitiesTableRecords = async tableSchemaId => {
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
      return onParseWebformRecords(data.records, entitiesWebformState.data[0], {}, data.totalRecords) || [];
    }

    return [];
  };

  const onAddEntitiesRecord = async manualRootPk => {
    setIsAddingEntityRecord(true);

    const autoIncrementFields = tables
      .map(table => table.elements.filter(element => element.autoIncrement).map(element => element.name))
      .filter(table => !isEmpty(table))
      .flat();

    /*Filters the Root table and the tables that have only foreign keys linked
    to the Root table primary key*/

    const filteredTables = datasetSchema.tables.filter(
      table =>
        table.tableSchemaNotEmpty &&
        (table.tableSchemaName === rootTableName ||
          !table.records[0].fields.some(
            field => !isNil(field?.referencedField?.idPk) && field?.referencedField?.idPk !== rootPkFieldId
          ))
    );

    let filteredOptionalTables;
    const optionalTables = tables.filter(table => table?.isOptional);

    if (!isEmpty(optionalTables)) {
      filteredOptionalTables = filteredTables.filter(tab =>
        isEmpty(optionalTables.find(optionalTable => optionalTable.name === tab.tableSchemaName))
      );
    }

    const tableSchemaId = entitiesWebformState.data.map(table => table.tableSchemaId).filter(table => !isNil(table));

    try {
      const entitiesTableRecords = await getEntitiesTableRecords(tableSchemaId);
      await WebformService.addEntityRecord(
        datasetId,
        !isEmpty(filteredOptionalTables) ? filteredOptionalTables : filteredTables,
        manualRootPk ? entitiesWebformState.rootPkInput : generateEntityId(entitiesTableRecords),
        rootPkFieldId,
        !isEmpty(autoIncrementFields) ? autoIncrementFields : undefined
      );

      onUpdateData();
      setIsAddingEntityRecord(false);
      setIsAddEntityIdDialogVisible(false);
    } catch (error) {
      if (error.response.status === 423) {
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
            content: { dataflowId, dataflowName, datasetId, datasetName, customContent: { tableName: '' } }
          },
          true
        );
      }
      setIsAddingEntityRecord(false);
      setIsAddEntityIdDialogVisible(false);
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

  const onSelectEditTable = (entityNumberId, tableName) => {
    const filteredTable = entitiesWebformState.data.filter(table => TextUtils.areEquals(table.name, tableName))[0];

    let recordId = '';

    entitiesRecords.forEach(entitiesRecord => {
      entitiesRecord.fields.forEach(field => {
        if (field.fieldSchemaId === rootPkFieldId && parseInt(field.value) === parseInt(entityNumberId)) {
          recordId = entitiesRecord.recordId;
        }
      });
    });

    setTableSchemaId(filteredTable.tableSchemaId);
    onSelectRecord(recordId, entityNumberId);
    onSelectTableName(tableName);
    onToggleView('details');
  };

  const onSelectFieldSchemaId = fieldSchemaId => {
    entitiesWebformDispatch({ type: 'ON_SELECT_SCHEMA_ID', payload: { fieldSchemaId } });
  };

  const onSelectRecord = (recordId, rootTableId) => {
    entitiesWebformDispatch({ type: 'ON_SELECT_RECORD', payload: { recordId, rootTableId } });
  };

  const onSelectTableName = name => entitiesWebformDispatch({ type: 'ON_SELECT_TABLE', payload: { name } });

  const onToggleView = view => entitiesWebformDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view } });

  const onUpdateData = () => entitiesWebformDispatch({ type: 'ON_UPDATE_DATA', payload: { value: !isDataUpdated } });

  const setIsAddingEntityRecord = value =>
    entitiesWebformDispatch({ type: 'SET_IS_ADDING_ENTITY_RECORD', payload: { value } });

  const setIsAddEntityIdDialogVisible = value =>
    entitiesWebformDispatch({ type: 'SET_IS_ADD_ENTITY_ID_DIALOG_VISIBLE', payload: { value } });

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
          rootPkFieldId={rootPkFieldId}
          rootTableName={rootTableName}
          selectedTable={selectedTable}
          selectedTableName={selectedTableName}
          setTableSchemaId={setTableSchemaId}
          state={state}
          tables={tables.filter(table => table.isVisible)}
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
        overview={overview}
        records={entitiesRecords}
        rootPkFieldId={rootPkFieldId}
        rootTableId={rootTableId}
        rootTableName={rootTableName}
        schemaTables={datasetSchema.tables}
        tables={tables}
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
          disabledConfirm={checkInvalidCharacters() || entitiesWebformState.isAddingEntityRecord}
          header={resourcesContext.messages['addEntityId']}
          iconConfirm={entitiesWebformState.isAddingEntityRecord ? 'spinnerAnimate' : 'add'}
          labelConfirm={resourcesContext.messages['addEntity']}
          onConfirm={() =>
            checkDuplicatePk()
              ? manageDialogs('isDuplicatePkDialogVisible', true)
              : !checkInvalidCharacters() && onAddEntitiesRecord(true)
          }
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
            disabled={(bigData && !isIcebergCreated) + entitiesWebformState.isAddingEntityRecord || isReleasing}
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
                      }
                    }}>
                    {items.id || '-'}
                  </span>
                ))}
              </div>
              <div className={styles.addButtonWrapper}>
                <Button
                  className={styles.addButton}
                  disabled={(bigData && !isIcebergCreated) + entitiesWebformState.isAddingEntityRecord || isReleasing}
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
