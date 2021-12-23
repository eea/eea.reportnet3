import { Fragment, useContext, useEffect, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import styles from './QCList.module.scss';

import { Dialog } from 'views/_components/Dialog';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { LevelError } from 'views/_components/LevelError';
import { QCFieldEditor } from './_components/QCFieldEditor';
import { Spinner } from 'views/_components/Spinner';
import { TrafficLight } from 'views/_components/TrafficLight';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { qcListReducer } from './Reducers/qcListReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { getExpressionString } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getExpressionString';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const QCList = ({
  dataflowId,
  dataset,
  datasetSchemaAllTables,
  datasetSchemaId,
  reporting = false,
  setHasValidations = () => {}
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [tabsValidationsState, tabsValidationsDispatch] = useReducer(qcListReducer, {
    deletedRuleId: null,
    filtered: false,
    filteredData: [],
    hasEmptyFields: false,
    initialFilteredData: [],
    initialValidationsList: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDeletingRule: false,
    editingRows: [],
    isLoading: true,
    isTableSorted: false,
    sortFieldValidations: null,
    sortOrderValidations: null,
    validationId: '',
    validationList: {},
    viewedQcHistoryId: null,
    qcHistoryData: null,
    isHistoryDialogVisible: false
  });

  const setIsHistoryDialogVisible = isHistoryDialogVisible =>
    tabsValidationsDispatch({ type: 'SET_IS_HISTORY_DIALOG_VISIBLE', payload: { isHistoryDialogVisible } });

  const setViewedQcHistoryId = viewedQcHistoryId =>
    tabsValidationsDispatch({ type: 'SET_VIEWED_QC_HISTORY', payload: { viewedQcHistoryId } });

  const getQcHistory = async () => {
    try {
      const { data } = await ValidationService.getQcHistory(tabsValidationsState.viewedQcHistoryId);

      // tabsValidationsDispatch({
      //   type: 'SET_QC_HISTORY_DATA', // TODO CREATE A REDUCER FOR THIS
      //   payload: { qcHistoryData: data }
      // });
    } catch (error) {
      console.error('ValidationsList - getQcHistory.', error);
      // notificationContext.add({ type: '________ERROR' }, true); // TODO: add correct error notification
    }
  };

  useEffect(() => {
    if (tabsValidationsState.isHistoryDialogVisible && !isNil(tabsValidationsState.viewedQcHistoryId)) {
      getQcHistory();
    }
  }, [tabsValidationsState.isHistoryDialogVisible]);

  useEffect(() => {
    setHasValidations(!checkIsEmptyValidations());
  }, [tabsValidationsState.validationList]);

  useEffect(() => {
    onLoadValidationsList(datasetSchemaId);
  }, [tabsValidationsState.isDataUpdated]);

  useEffect(() => {
    if (validationContext.isAutomaticRuleUpdated) {
      onUpdateData();
      validationContext.onAutomaticRuleIsUpdated(false);
    }
  }, [validationContext.isAutomaticRuleUpdated]);

  const getFilteredState = value => tabsValidationsDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {tabsValidationsState.filtered &&
      tabsValidationsState.validationList.validations.length !== tabsValidationsState.filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${tabsValidationsState.filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {tabsValidationsState.validationList.validations.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {tabsValidationsState.filtered &&
      tabsValidationsState.validationList.validations.length === tabsValidationsState.filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isDeleteDialogVisible = value =>
    tabsValidationsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => tabsValidationsDispatch({ type: 'IS_LOADING', payload: { value } });

  const isDataUpdated = value => tabsValidationsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const onDeleteValidation = async () => {
    tabsValidationsDispatch({
      type: 'IS_DELETING_RULE',
      payload: { isDeletingRule: true }
    });
    try {
      await ValidationService.delete(dataset.datasetId, tabsValidationsState.validationId);
      tabsValidationsDispatch({
        type: 'SET_DELETED_RULE_ID',
        payload: { deletedRuleId: tabsValidationsState.validationId }
      });
      onUpdateData();
    } catch (error) {
      console.error('ValidationsList - onDeleteValidation.', error);
      notificationContext.add({ type: 'DELETE_RULE_ERROR' }, true);
      validationId('');
    } finally {
      onHideDeleteDialog();
      tabsValidationsDispatch({
        type: 'IS_DELETING_RULE',
        payload: { isDeletingRule: false }
      });
    }
  };

  const onHideDeleteDialog = () => {
    isDeleteDialogVisible(false);
  };

  const onLoadFilteredData = data => tabsValidationsDispatch({ type: 'FILTER_DATA', payload: { data } });

  const onLoadValidationsList = async datasetSchemaId => {
    let updatedRuleId = validationContext.updatedRuleId;
    let isFetchingData = true;

    validationContext.onFetchingData(isFetchingData, updatedRuleId);
    try {
      const validationsServiceList = await ValidationService.getAll(dataflowId, datasetSchemaId, reporting);
      if (!isNil(validationsServiceList) && !isNil(validationsServiceList.validations)) {
        validationsServiceList.validations.forEach(validation => {
          const additionalInfo = getAdditionalValidationInfo(
            validation.referenceId,
            validation.entityType,
            validation.relations
          );
          validation.table = additionalInfo.tableName || '';
          validation.field = additionalInfo.fieldName || '';
          validation.fieldName = additionalInfo.fieldName || '';
        });
      }

      //If there are quickedit rows preserve their values on update
      if (tabsValidationsState.editingRows.length > 0) {
        validationsServiceList.validations.forEach((validation, i) => {
          const editingRow = tabsValidationsState.editingRows.find(row => row.id === validation.id);
          if (editingRow) {
            validationsServiceList.validations[i] = editingRow;
          }
        });
      }

      tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_LIST', payload: { validationsServiceList } });
      validationContext.onSetRulesDescription(
        validationsServiceList?.validations?.map(validation => {
          return {
            automaticType: validation.automaticType,
            id: validation.id,
            description: validation.description,
            message: validation.message,
            name: validation.name
          };
        })
      );
    } catch (error) {
      console.error('ValidationsList - onLoadValidationsList.', error);
      notificationContext.add({ type: 'VALIDATION_SERVICE_GET_ALL_ERROR' }, true);
    } finally {
      updatedRuleId = null;
      isFetchingData = false;
      isLoading(false);
      validationId('');
      validationContext.onFetchingData(isFetchingData, updatedRuleId);
    }
  };

  const onShowDeleteDialog = () => isDeleteDialogVisible(true);

  const onUpdateData = () => {
    isDataUpdated(!tabsValidationsState.isDataUpdated);
  };

  useCheckNotifications(
    ['INVALIDATED_QC_RULE_EVENT', 'VALIDATED_QC_RULE_EVENT', 'VALIDATE_RULES_ERROR_EVENT'],
    onUpdateData,
    true
  );

  const automaticTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.automatic ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const correctTemplate = rowData => <div className={styles.checkedValueColumn}>{getCorrectTemplate(rowData)}</div>;

  const getCorrectTemplate = rowData => {
    if (isNil(rowData.isCorrect)) {
      return <FontAwesomeIcon className={`${styles.icon} ${styles.spinner}`} icon={AwesomeIcons('spinner')} />;
    } else {
      if (rowData.isCorrect) {
        return <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />;
      } else if (!isNil(rowData.sqlError)) {
        return (
          <Button
            className={`${styles.invalidSqlIcon} p-button-secondary`}
            icon="warning"
            onClick={() => navigator.clipboard.writeText(rowData.sqlError)}
            tooltip={`${rowData.sqlError} <br />  <br />${resourcesContext.messages['sqlErrorMessageCopy']} `}
            tooltipOptions={{ position: 'left' }}
          />
        );
      }
    }
  };

  const enabledTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.enabled ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const expressionsTemplate = rowData => {
    if (!isEmpty(rowData.expressionText)) {
      return rowData.expressionText;
    }

    if (!isNil(rowData.sqlSentence)) {
      return rowData.sqlSentence;
    }

    return getExpressionString(rowData, datasetSchemaAllTables);
  };

  const getAdditionalValidationInfo = (referenceId, entityType, relations) => {
    const additionalInfo = {};

    datasetSchemaAllTables.forEach(table => {
      if (TextUtils.areEquals(entityType, 'TABLE')) {
        if (table.tableSchemaId === referenceId) {
          additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
        }
      } else if (TextUtils.areEquals(entityType, 'RECORD')) {
        if (table.recordSchemaId === referenceId) {
          additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
        }
      } else if (TextUtils.areEquals(entityType, 'FIELD') || TextUtils.areEquals(entityType, 'TABLE')) {
        table.records?.forEach(record =>
          record.fields.forEach(field => {
            if (!isNil(field)) {
              if (TextUtils.areEquals(entityType, 'FIELD')) {
                if (field.fieldId === referenceId) {
                  additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
                  additionalInfo.fieldName = field.name;
                }
              } else {
                if (!isEmpty(relations)) {
                  if (field.fieldId === relations.links[0].originField.code) {
                    additionalInfo.tableName = !isUndefined(table.tableSchemaName)
                      ? table.tableSchemaName
                      : table.header;
                    additionalInfo.fieldName = field.name;
                  }
                }
              }
            }
          })
        );
      }
    });
    return additionalInfo;
  };

  const getHeader = fieldHeader => {
    let header;
    switch (fieldHeader) {
      case 'levelError':
        header = resourcesContext.messages['ruleLevelError'];
        break;
      case 'shortCode':
        header = resourcesContext.messages['ruleCode'];
        break;
      case 'isCorrect':
        header = resourcesContext.messages['valid'];
        break;
      default:
        header = resourcesContext.messages[fieldHeader];
        break;
    }

    return header;
  };

  const getOrderedValidations = validations => {
    const validationsWithPriority = [
      { id: 'id', index: 0 },
      { id: 'table', index: 1 },
      { id: 'field', index: 2 },
      { id: 'shortCode', index: 3 },
      { id: 'name', index: 4 },
      { id: 'description', index: 5 },
      { id: 'message', index: 6 },
      { id: 'expressionText', index: 7 },
      { id: 'sqlSentenceCost', index: 8 },
      { id: 'entityType', index: 9 },
      { id: 'levelError', index: 10 },
      { id: 'automatic', index: 11 },
      { id: 'enabled', index: 12 },
      { id: 'referenceId', index: 13 },
      { id: 'activationGroup', index: 14 },
      { id: 'date', index: 15 },
      { id: 'actionButtons', index: 16 },
      { id: 'isCorrect', index: 17 }
    ];

    return validations
      .map(error => validationsWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const actionsTemplate = row => (row.automatic ? editTemplate(row) : editAndDeleteTemplate(row));

  const getEditBtnIcon = rowId => {
    if (rowId === validationContext.updatedRuleId && validationContext.isFetchingData) {
      return 'spinnerAnimate';
    }
    return 'edit';
  };

  const historyButton = rowId => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.editRowButton}`}
        disabled={validationContext.isFetchingData}
        icon="info"
        onClick={() => {
          setIsHistoryDialogVisible(true);
          setViewedQcHistoryId(rowId);
        }}
        tooltip={resourcesContext.messages['qcHistoryButtonTooltip']}
        tooltipOptions={{ position: 'top' }}
        type="button"
      />
    );
  };

  const editAndDeleteTemplate = row => {
    let rowType = 'field';

    if (row.entityType === 'RECORD') {
      rowType = 'row';
    } else if (row.entityType === 'TABLE') {
      rowType = 'dataset';
    }

    const getDeleteBtnIcon = () => {
      if (row.id === tabsValidationsState.deletedRuleId && validationContext.isFetchingData) {
        return 'spinnerAnimate';
      }
      return 'trash';
    };

    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.editRowButton}`}
          disabled={validationContext.isFetchingData}
          icon={getEditBtnIcon(row.id)}
          onClick={() => validationContext.onOpenToEdit(row, rowType)}
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.editRowButton}`}
          disabled={validationContext.isFetchingData}
          icon="clone"
          onClick={() => validationContext.onOpenToCopy(row, rowType)}
          tooltip={resourcesContext.messages['duplicate']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        {historyButton(row.id)}
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
          disabled={validationContext.isFetchingData}
          icon={getDeleteBtnIcon()}
          onClick={onShowDeleteDialog}
          tooltip={resourcesContext.messages['delete']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      </Fragment>
    );
  };

  const editTemplate = row => {
    let rowType = 'field';

    if (row.entityType === 'RECORD') rowType = 'row';

    if (row.entityType === 'TABLE') rowType = 'dataset';

    return (
      <Fragment>
        {historyButton(row.id)}
        <Button
          className={`p-button-rounded p-button-secondary-transparent  p-button-animated-blink ${styles.editRowButton}`}
          disabled={validationContext.isFetchingData}
          icon={getEditBtnIcon(row.id)}
          onClick={() => validationContext.onOpenToEdit(row, rowType)}
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
      </Fragment>
    );
  };

  const deleteValidationDialog = () => (
    <ConfirmDialog
      classNameConfirm={'p-button-danger'}
      disabledConfirm={tabsValidationsState.isDeletingRule}
      header={resourcesContext.messages['deleteValidationHeader']}
      iconConfirm={tabsValidationsState.isDeletingRule ? 'spinnerAnimate' : 'check'}
      labelCancel={resourcesContext.messages['no']}
      labelConfirm={resourcesContext.messages['yes']}
      onConfirm={() => onDeleteValidation()}
      onHide={() => onHideDeleteDialog()}
      visible={tabsValidationsState.isDeleteDialogVisible}>
      {resourcesContext.messages['deleteValidationConfirm']}
    </ConfirmDialog>
  );

  const columnStyles = field => {
    const style = {};
    const invisibleFields = ['id', 'referenceId', 'activationGroup', 'condition', 'date'];
    if (reporting) {
      invisibleFields.push('enabled', 'automatic', 'isCorrect');
    }
    if (field === 'description') {
      style.width = '23%';
    }

    if (field === 'entityType' || field === 'levelError') {
      style.minWidth = '6rem';
    }

    if (invisibleFields.includes(field)) {
      style.display = 'none';
    } else {
      style.display = 'auto';
    }
    return style;
  };

  const actionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resourcesContext.messages['actions']}
      key="actions"
      rowEditor={true}
      sortable={false}
      style={{ width: '100px' }}
    />
  );

  const getEditor = field => {
    switch (field) {
      case 'enabled':
        return row => checkboxEditor(row, 'enabled');
      case 'name':
      case 'description':
      case 'message':
      case 'shortCode':
        return row => textEditor(row, field);
      case 'levelError':
        return row => dropdownEditor(row, 'levelError');
      default:
        break;
    }
  };

  const checkboxEditor = (props, field) => {
    return (
      <div className={styles.checkboxEditorWrapper}>
        <Checkbox
          checked={props.rowData[field]}
          className={styles.checkboxEditor}
          id={props.rowData[field]?.toString()}
          inputId={props.rowData[field]?.toString()}
          onChange={e => onRowEditorValueChange(props, e.checked)}
          role="checkbox"
        />
      </div>
    );
  };

  const getCandidateRule = data => {
    const getExpressionType = () => {
      let expressionType = '';
      if (isNil(data.sqlSentence)) {
        if (data.expressionsIf && data.expressionsIf.length > 0) {
          expressionType = 'ifThenClause';
        }

        if (data.entityType === 'TABLE') {
          expressionType = 'fieldRelations';
        }

        if (data.entityType === 'RECORD' && data.expressions.length > 0) {
          expressionType = 'fieldComparison';
        }

        if (data.entityType === 'FIELD' && data.expressions.length > 0) {
          expressionType = 'fieldTab';
        }
      } else {
        expressionType = 'sqlSentence';
      }

      return expressionType;
    };

    const rule = {
      ...data,
      active: data.enabled,
      errorMessage: data.message,
      errorLevel: { value: data.levelError },
      ruleId: data.id,
      field: { code: data.referenceId },
      table: { code: data.referenceId },
      recordSchemaId: data.referenceId,
      expressionType: getExpressionType()
    };
    return rule;
  };

  const dropdownEditor = (props, field) => {
    return (
      <Dropdown
        appendTo={document.body}
        filterPlaceholder={resourcesContext.messages['errorTypePlaceholder']}
        id="errorType"
        itemTemplate={rowData => levelErrorTemplate(rowData, true)}
        onChange={e => onRowEditorValueChange(props, e.target.value.value)}
        optionLabel="label"
        options={config.validations.errorLevels}
        optionValue="value"
        placeholder={resourcesContext.messages['errorTypePlaceholder']}
        value={{ label: props.rowData[field], value: props.rowData[field] }}
      />
    );
  };

  const textEditor = (props, field) => (
    <QCFieldEditor
      initialValue={props.rowData[field]}
      keyfilter={['message', 'shortCode'].includes(field) ? 'noDoubleQuote' : ''}
      onSaveField={onRowEditorValueChange}
      qcs={props}
      required={['name', 'message', 'shortCode'].includes(field)}
    />
  );

  const levelErrorTemplate = (rowData, isDropdown = false) => (
    <div className={styles.levelErrorTemplateWrapper}>
      <LevelError type={isDropdown ? rowData.value : rowData.levelError.toLowerCase()} />
    </div>
  );

  const sqlSentenceCostTemplate = rowData => {
    if (rowData.sqlSentenceCost !== 0 && !isNil(rowData.sqlSentenceCost)) {
      return (
        <div className={styles.sqlSentenceCostTemplate}>
          <TrafficLight sqlSentenceCost={rowData.sqlSentenceCost} />
        </div>
      );
    }
  };

  const renderColumns = validations => {
    const fieldColumns = getOrderedValidations(Object.keys(validations[0]))
      .filter(key => !key.includes('Id') && !key.includes('filter'))
      .map(field => {
        let template = null;
        if (field === 'automatic') template = automaticTemplate;
        if (field === 'enabled') template = enabledTemplate;
        if (field === 'isCorrect') template = correctTemplate;
        if (field === 'levelError') template = rowData => levelErrorTemplate(rowData, false);
        if (field === 'expressionText') template = expressionsTemplate;
        if (field === 'sqlSentenceCost') template = sqlSentenceCostTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            editor={getEditor(field)}
            field={field}
            header={getHeader(field)}
            key={field}
            sortable={tabsValidationsState.editingRows.length === 0}
            style={columnStyles(field)}
          />
        );
      });
    if (!reporting) {
      fieldColumns.push(actionButtonsColumn);
    }
    return fieldColumns;
  };

  const validationId = value => tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_ID', payload: { value } });

  const checkIsEmptyValidations = () =>
    isUndefined(tabsValidationsState.validationList) || isEmpty(tabsValidationsState.validationList);

  const onRowEditorValueChange = (props, value, isText = false) => {
    const inmQCs = [...tabsValidationsState.validationList.validations];
    const inmEditingRows = [...tabsValidationsState.editingRows];
    const qcIdx = inmQCs.findIndex(qc => qc.id === props.rowData.id);
    const editIdx = inmEditingRows.findIndex(qc => qc.id === props.rowData.id);
    if (inmQCs[qcIdx][props.field] !== value && editIdx !== -1) {
      inmQCs[qcIdx][props.field] = isText ? value.trim() : value;
      inmEditingRows[editIdx][props.field] = isText ? value.trim() : value;

      tabsValidationsDispatch({
        type: 'UPDATE_FILTER_DATA_AND_VALIDATIONS',
        payload: { qcs: inmQCs, editRows: inmEditingRows }
      });
    }
  };

  const onRowEditInit = event => {
    validationContext.onOpenToQuickEdit(event.data.id);
    tabsValidationsDispatch({ type: 'SET_INITIAL_DATA', payload: event.data });
  };

  const onRowEditCancel = event => tabsValidationsDispatch({ type: 'RESET_FILTERED_DATA', payload: event.data });

  const onSort = event => {
    tabsValidationsDispatch({
      type: 'SET_IS_TABLE_SORTED',
      payload: {
        value: event.sortOrder === 1 || event.sortOrder === -1,
        sortFieldValidations: event.sortField,
        sortOrderValidations: event.sortOrder
      }
    });
  };

  const onUpdateValidationRule = async event => {
    try {
      tabsValidationsDispatch({
        type: 'UPDATE_VALIDATION_RULE',
        payload: event.data
      });

      if (TextUtils.areEquals(event.data.entityType, 'TABLE')) {
        await ValidationService.updateTableRule(dataset.datasetId, getCandidateRule(event.data));
      } else if (TextUtils.areEquals(event.data.entityType, 'RECORD')) {
        await ValidationService.updateRowRule(dataset.datasetId, getCandidateRule(event.data));
      } else {
        await ValidationService.updateFieldRule(dataset.datasetId, getCandidateRule(event.data));
      }
      onUpdateData();
    } catch (error) {
      console.error('FieldValidation - onUpdateValidationRule.', error);
      notificationContext.add({ type: 'QC_RULE_UPDATING_ERROR' }, true);
    }
  };

  const filterOptions = [
    {
      type: 'multiselect',
      properties: [
        { name: 'table', showInput: true },
        { name: 'field', showInput: true },
        { name: 'entityType' },
        { name: 'levelError' },
        { name: 'automatic', label: resourcesContext.messages['creationMode'] },
        { name: 'enabled', label: resourcesContext.messages['statusQC'] },
        { name: 'isCorrect' }
      ]
    }
  ];

  const mockArray = [1, 2, 3, 4];
  // const fields = tabsValidationsState.history.map(historicEvent => {
  const fields = mockArray.map(historicEvent => {
    return {
      id: 'id999999',
      user: 'qc.user@com.com',
      timestamp: '29/02/2019',
      metadata: 'false',
      expression: 'true',
      status: 'true'
    };
  });

  const getHistoryColumns = () => {
    const columnData = Object.keys(fields[0]).map(key => ({ field: key, header: key }));

    return columnData.map(col => <Column field={col.field} header={col.header.toUpperCase()} key={col.field} />);
  };

  const generateHistoryDialogContent = () => {
    const columns = getHistoryColumns();

    // todo add loading indicator if no data
    // if (!tabsValidationsState.qcHistoryData) {
    //   return (
    //     <div className={styles.loadingSpinner}>
    //       <Spinner className={styles.spinnerPosition} />
    //     </div>
    //   );
    // }

    return (
      <DataTable autoLayout value={fields}>
        {columns}
      </DataTable>
    );
  };

  const validationList = () => {
    if (tabsValidationsState.isLoading) {
      return (
        <div className={styles.validationsWithoutTable}>
          <div className={styles.loadingSpinner}>
            <Spinner className={styles.spinnerPosition} />
          </div>
        </div>
      );
    }

    if (checkIsEmptyValidations()) {
      return (
        <div className={styles.validationsWithoutTable}>
          <div className={styles.noValidations}>{resourcesContext.messages['noQCs']}</div>
        </div>
      );
    }

    return (
      <div className={styles.validations}>
        <div
          className={styles.searchInput}
          style={{
            pointerEvents: tabsValidationsState.editingRows.length > 0 ? 'none' : 'auto',
            opacity: tabsValidationsState.editingRows.length > 0 ? '0.5' : 1
          }}>
          <Filters
            className="filter-lines"
            data={tabsValidationsState.validationList.validations}
            getFilteredData={onLoadFilteredData}
            getFilteredSearched={getFilteredState}
            options={filterOptions}
            searchAll
            searchBy={['shortCode', 'name', 'description', 'message']}
          />
        </div>
        {!isEmpty(tabsValidationsState.filteredData) ? (
          <DataTable
            autoLayout={true}
            className={styles.paginatorValidationViewer}
            editMode="row"
            hasDefaultCurrentPage={true}
            loading={false}
            onRowClick={event => validationId(event.data.id)}
            onRowEditCancel={onRowEditCancel}
            onRowEditInit={onRowEditInit}
            onRowEditSave={onUpdateValidationRule}
            onSort={event => onSort(event)}
            paginator={true}
            paginatorDisabled={tabsValidationsState.editingRows.length > 0}
            paginatorRight={!isNil(tabsValidationsState.filteredData) && getPaginatorRecordsCount()}
            quickEditRowInfo={{
              updatedRow: validationContext.updatedRuleId,
              deletedRow: tabsValidationsState.deletedRuleId,
              property: 'id',
              condition:
                validationContext.isFetchingData ||
                tabsValidationsState.filtered ||
                tabsValidationsState.hasEmptyFields ||
                tabsValidationsState.isTableSorted,
              requiredFields: ['name', 'message', 'shortCode']
            }}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            sortField={tabsValidationsState.sortFieldValidations}
            sortOrder={tabsValidationsState.sortOrderValidations}
            totalRecords={tabsValidationsState.validationList.validations.length}
            value={cloneDeep(tabsValidationsState.filteredData)}>
            {renderColumns(tabsValidationsState.validationList.validations)}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>{resourcesContext.messages['noQCRulesWithSelectedParameters']}</div>
        )}
      </div>
    );
  };

  const footerQcHistory = (
    <Button
      className="p-button-secondary p-button-animated-blink p-button-right-aligned"
      icon="cancel"
      id="cancelHistoryQc"
      label={resourcesContext.messages['close']}
      onClick={() => setIsHistoryDialogVisible(false)}
    />
  );

  return (
    <Fragment>
      {validationList()}
      {tabsValidationsState.isDeleteDialogVisible && deleteValidationDialog()}

      {tabsValidationsState.isHistoryDialogVisible && (
        <Dialog
          className="responsiveDialog"
          footer={footerQcHistory}
          header={resourcesContext.messages['qcHistoryDialogHeader']}
          onHide={() => setIsHistoryDialogVisible(false)}
          visible={tabsValidationsState.isHistoryDialogVisible}>
          {generateHistoryDialogContent()}
        </Dialog>
      )}
    </Fragment>
  );
};
