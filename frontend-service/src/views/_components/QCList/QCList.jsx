import { Fragment, useContext, useEffect, useLayoutEffect, useReducer } from 'react';
import { useRecoilValue } from 'recoil';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import styles from './QCList.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dropdown } from 'views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { LevelError } from 'views/_components/LevelError';
import { MyFilters } from 'views/_components/MyFilters';
import { QCFieldEditor } from './_components/QCFieldEditor';
import { QCSpecificHistory } from 'views/_components/QCSpecificHistory';
import { Spinner } from 'views/_components/Spinner';
import { TrafficLight } from 'views/_components/TrafficLight';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { filterByState } from '../MyFilters/_functions/Stores/filtersStores';

import { qcListReducer } from './Reducers/qcListReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { getExpressionString } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getExpressionString';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const QCList = ({
  dataflowId,
  dataset,
  datasetSchemaAllTables,
  datasetSchemaId,
  isDataflowOpen = false,
  isDatasetDesigner = false,
  setHasValidations = () => {}
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const filterBy = useRecoilValue(filterByState(`qcList_${dataset.datasetId}`));
  const isDataFiltered = !isEmpty(filterBy);

  const [tabsValidationsState, tabsValidationsDispatch] = useReducer(qcListReducer, {
    deletedRuleId: null,
    editingRows: [],
    filteredData: [],
    hasEmptyFields: false,
    initialFilteredData: [],
    initialValidationsList: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDeletingRule: false,
    isHistoryDialogVisible: false,
    isLoading: true,
    isTableSorted: false,
    sortFieldValidations: null,
    sortOrderValidations: null,
    validationId: '',
    validationList: {}
  });

  useLayoutEffect(() => {
    onLoadValidationsList(datasetSchemaId);
  }, [tabsValidationsState.isDataUpdated]);

  useEffect(() => {
    setHasValidations(!checkIsEmptyValidations());
  }, [tabsValidationsState.validationList]);

  useEffect(() => {
    if (validationContext.isAutomaticRuleUpdated) {
      onUpdateData();
      validationContext.onAutomaticRuleIsUpdated(false);
    }
  }, [validationContext.isAutomaticRuleUpdated]);

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered &&
      tabsValidationsState.validationList.validations.length !== tabsValidationsState.filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${tabsValidationsState.filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {tabsValidationsState.validationList.validations.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isDataFiltered &&
      tabsValidationsState.validationList.validations.length === tabsValidationsState.filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const setIsHistoryDialogVisible = isHistoryDialogVisible => {
    tabsValidationsDispatch({
      type: 'SET_IS_HISTORY_DIALOG_VISIBLE',
      payload: isHistoryDialogVisible
    });
  };

  const setValidationId = validationId => tabsValidationsDispatch({ type: 'SET_VALIDATION_ID', payload: validationId });

  const onOpenHistoryDialog = validationId => {
    setIsHistoryDialogVisible(true);
    setValidationId(validationId);
  };

  const onCloseHistoryDialog = () => {
    setIsHistoryDialogVisible(false);
    setValidationId('');
  };

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
      setValidationId('');
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
      const validationsServiceList = await ValidationService.getAll(dataflowId, datasetSchemaId, !isDatasetDesigner);
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
            description: validation.description,
            id: validation.id,
            message: validation.message,
            name: validation.name,
            shortCode: validation.shortCode
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
      setValidationId('');
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

  const getAutomaticTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.automatic ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const getCorrectTemplate = rowData => (
    <div className={styles.checkedValueColumn}>{getCorrectTemplateContent(rowData)}</div>
  );

  const getCorrectTemplateContent = rowData => {
    if (isNil(rowData.isCorrect)) {
      return <FontAwesomeIcon className={`${styles.icon} ${styles.spinner}`} icon={AwesomeIcons('spinner')} />;
    }

    if (rowData.isCorrect) {
      return <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />;
    }

    if (!isNil(rowData.sqlError)) {
      return (
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.invalidSqlIcon}`}
          icon="warning"
          onClick={() => navigator.clipboard.writeText(rowData.sqlError)}
          tooltip={`${rowData.sqlError}<br/><br/><b><i>${resourcesContext.messages['sqlErrorMessageCopy']}</i></b>`}
          tooltipOptions={{ position: 'left' }}
        />
      );
    }
  };

  const getEnabledTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.enabled ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const getExpressionsTemplate = rowData => {
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

  const getTableColumns = () => {
    const columns = [
      { key: 'table', header: resourcesContext.messages['table'] },
      { key: 'field', header: resourcesContext.messages['field'] },
      { key: 'shortCode', header: resourcesContext.messages['ruleCode'], editor: textEditor },
      { key: 'name', header: resourcesContext.messages['name'], editor: textEditor },
      {
        key: 'description',
        header: resourcesContext.messages['description'],
        editor: textEditor,
        className: styles.descriptionColumn
      },
      { key: 'message', header: resourcesContext.messages['message'] },
      { key: 'expressionText', header: resourcesContext.messages['expressionText'], template: getExpressionsTemplate },
      {
        key: 'sqlSentenceCost',
        header: resourcesContext.messages['sqlSentenceCost'],
        template: getSqlSentenceCostTemplate
      },
      { key: 'entityType', header: resourcesContext.messages['entityType'], className: styles.entityTypeColumn },
      {
        key: 'levelError',
        header: resourcesContext.messages['ruleLevelError'],
        template: rowData => getLevelErrorTemplate(rowData, false),
        editor: dropdownEditor,
        className: styles.levelErrorColumn
      }
    ];

    if (isDatasetDesigner) {
      columns.push(
        { key: 'automatic', header: resourcesContext.messages['automatic'], template: getAutomaticTemplate },
        {
          key: 'enabled',
          header: resourcesContext.messages['enabled'],
          template: getEnabledTemplate,
          editor: checkboxEditor
        },
        { key: 'isCorrect', header: resourcesContext.messages['valid'], template: getCorrectTemplate },
        {
          key: 'actions',
          header: resourcesContext.messages['actions'],
          template: getActionsTemplate
        }
      );
    }

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        editor={column.editor}
        field={column.key}
        header={column.header}
        key={column.key}
        rowEditor={column.key === 'actions'}
        sortable={column.key !== 'actions' && tabsValidationsState.editingRows.length === 0}
      />
    ));
  };

  const getActionsTemplate = row => (row.automatic ? editTemplate(row) : editAndDeleteTemplate(row));

  const getEditBtnIcon = rowId => {
    if (rowId === validationContext.updatedRuleId && validationContext.isFetchingData) {
      return 'spinnerAnimate';
    }
    return 'edit';
  };

  const renderHistoricButton = id => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
        disabled={validationContext.isFetchingData}
        icon="info"
        onClick={() => onOpenHistoryDialog(id)}
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
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={validationContext.isFetchingData}
          icon={getEditBtnIcon(row.id)}
          onClick={() => validationContext.onOpenToEdit(row, rowType)}
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        <Button
          className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.actionButton}`}
          disabled={validationContext.isFetchingData}
          icon="clone"
          onClick={() => validationContext.onOpenToCopy(row, rowType)}
          tooltip={resourcesContext.messages['duplicate']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        {isDataflowOpen && row.hasHistoric && renderHistoricButton(row.id)}

        {!isDataflowOpen && (
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteRowButton}`}
            disabled={validationContext.isFetchingData}
            icon={getDeleteBtnIcon()}
            onClick={onShowDeleteDialog}
            tooltip={resourcesContext.messages['delete']}
            tooltipOptions={{ position: 'top' }}
            type="button"
          />
        )}
      </Fragment>
    );
  };

  const editTemplate = row => {
    let rowType = 'field';

    if (row.entityType === 'RECORD') rowType = 'row';

    if (row.entityType === 'TABLE') rowType = 'dataset';

    return (
      <Fragment>
        <Button
          className={`p-button-rounded p-button-secondary-transparent  p-button-animated-blink ${styles.actionButton}`}
          disabled={validationContext.isFetchingData}
          icon={getEditBtnIcon(row.id)}
          onClick={() => validationContext.onOpenToEdit(row, rowType)}
          tooltip={resourcesContext.messages['edit']}
          tooltipOptions={{ position: 'top' }}
          type="button"
        />
        {isDataflowOpen && row.hasHistoric && renderHistoricButton(row.id)}
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

  const checkboxEditor = props => {
    const { rowData, field } = props;

    return (
      <div className={styles.checkboxEditorWrapper}>
        <Checkbox
          checked={rowData[field]}
          className={styles.checkboxEditor}
          id={rowData[field]?.toString()}
          inputId={rowData[field]?.toString()}
          onChange={e => onRowEditorValueChange(rowData, field, e.checked)}
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

  const dropdownEditor = props => {
    const { rowData, field } = props;

    return (
      <Dropdown
        appendTo={document.body}
        filterPlaceholder={resourcesContext.messages['errorTypePlaceholder']}
        id="errorType"
        itemTemplate={rowData => getLevelErrorTemplate(rowData, true)}
        onChange={e => onRowEditorValueChange(rowData, field, e.target.value.value)}
        optionLabel="label"
        options={config.validations.errorLevels}
        optionValue="value"
        placeholder={resourcesContext.messages['errorTypePlaceholder']}
        value={{ label: rowData[field], value: rowData[field] }}
      />
    );
  };

  const textEditor = props => {
    const { rowData, field } = props;

    return (
      <QCFieldEditor
        field={field}
        keyfilter={['message', 'shortCode'].includes(field) ? 'noDoubleQuote' : ''}
        onSaveField={onRowEditorValueChange}
        required={['name', 'message', 'shortCode'].includes(field)}
        rowData={rowData}
      />
    );
  };

  const getLevelErrorTemplate = (rowData, isDropdown = false) => (
    <div className={styles.levelErrorTemplateWrapper}>
      <LevelError type={isDropdown ? rowData.value : rowData.levelError.toLowerCase()} />
    </div>
  );

  const getSqlSentenceCostTemplate = rowData => {
    if (rowData.sqlSentenceCost !== 0 && !isNil(rowData.sqlSentenceCost)) {
      return (
        <div className={styles.sqlSentenceCostTemplate}>
          <TrafficLight sqlSentenceCost={rowData.sqlSentenceCost} />
        </div>
      );
    }
  };

  const checkIsEmptyValidations = () =>
    isUndefined(tabsValidationsState.validationList) || isEmpty(tabsValidationsState.validationList);

  const onRowEditorValueChange = (rowData, field, value, isText = false) => {
    const inmQCs = cloneDeep(tabsValidationsState.validationList.validations);
    const inmEditingRows = cloneDeep(tabsValidationsState.editingRows);
    const qcIdx = inmQCs.findIndex(qc => qc.id === rowData.id);
    const editIdx = inmEditingRows.findIndex(qc => qc.id === rowData.id);

    if (inmQCs[qcIdx][field] !== value && editIdx !== -1) {
      inmQCs[qcIdx][field] = isText ? value.trim() : value;
      inmEditingRows[editIdx][field] = isText ? value.trim() : value;

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

  const FILTER_OPTIONS = [
    {
      key: 'search',
      label: resourcesContext.messages['searchByQcList'],
      searchBy: ['shortCode', 'name', 'description', 'message'],
      type: 'SEARCH'
    },
    {
      nestedOptions: [
        { key: 'table', label: resourcesContext.messages['table'] },
        { key: 'field', label: resourcesContext.messages['field'] },
        { key: 'entityType', label: resourcesContext.messages['entityType'] },
        { key: 'levelError', label: resourcesContext.messages['levelError'], template: 'LevelError' },
        {
          key: 'automatic',
          label: resourcesContext.messages['creationMode'],
          multiSelectOptions: [
            { type: resourcesContext.messages['automatic'].toUpperCase(), value: true },
            { type: resourcesContext.messages['manual'].toUpperCase(), value: false }
          ]
        },
        {
          key: 'enabled',
          label: resourcesContext.messages['statusQC'],
          multiSelectOptions: [
            { type: resourcesContext.messages['enabled'], value: true },
            { type: resourcesContext.messages['disabled'], value: false }
          ],
          template: 'LevelError'
        },
        {
          key: 'isCorrect',
          label: resourcesContext.messages['isCorrect'],
          multiSelectOptions: [
            { type: resourcesContext.messages['valid'], value: true },
            { type: resourcesContext.messages['invalid'], value: false }
          ],
          template: 'LevelError'
        }
      ],
      type: 'MULTI_SELECT'
    }
  ];

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
          <MyFilters
            className="qcList"
            data={tabsValidationsState.validationList.validations}
            getFilteredData={onLoadFilteredData}
            options={FILTER_OPTIONS}
            viewType={`qcList_${dataset.datasetId}`}
          />
        </div>
        {!isEmpty(tabsValidationsState.filteredData) ? (
          <DataTable
            autoLayout
            className={styles.paginatorValidationViewer}
            editMode="row"
            hasDefaultCurrentPage
            loading={false}
            onRowClick={event => setValidationId(event.data.id)}
            onRowEditCancel={onRowEditCancel}
            onRowEditInit={onRowEditInit}
            onRowEditSave={onUpdateValidationRule}
            onSort={event => onSort(event)}
            paginator
            paginatorDisabled={tabsValidationsState.editingRows.length > 0}
            paginatorRight={!isNil(tabsValidationsState.filteredData) && getPaginatorRecordsCount()}
            quickEditRowInfo={{
              updatedRow: validationContext.updatedRuleId,
              deletedRow: tabsValidationsState.deletedRuleId,
              property: 'id',
              condition:
                validationContext.isFetchingData ||
                isDataFiltered ||
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
            {getTableColumns()}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>{resourcesContext.messages['noQCRulesWithSelectedParameters']}</div>
        )}
      </div>
    );
  };

  return (
    <Fragment>
      {validationList()}
      {tabsValidationsState.isHistoryDialogVisible && (
        <QCSpecificHistory
          datasetId={dataset.datasetId}
          isDialogVisible={tabsValidationsState.isHistoryDialogVisible}
          onCloseDialog={onCloseHistoryDialog}
          validationId={tabsValidationsState.validationId}
        />
      )}
      {tabsValidationsState.isDeleteDialogVisible && deleteValidationDialog()}
    </Fragment>
  );
};
