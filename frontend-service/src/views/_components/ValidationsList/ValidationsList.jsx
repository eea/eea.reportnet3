import { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './ValidationsList.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

import { tabsValidationsReducer } from './Reducers/tabsValidationsReducer';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { getExpressionString } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getExpressionString';
import { TextUtils } from 'repositories/_utils/TextUtils';

const ValidationsList = withRouter(
  ({ dataset, datasetSchemaAllTables, datasetSchemaId, reporting = false, setHasValidations = () => {} }) => {
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);
    const validationContext = useContext(ValidationContext);

    const [tabsValidationsState, tabsValidationsDispatch] = useReducer(tabsValidationsReducer, {
      deletedRuleId: null,
      filtered: false,
      filteredData: [],
      isDataUpdated: false,
      isDeleteDialogVisible: false,
      isLoading: true,
      validationId: '',
      validationList: {}
    });

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
          ? `${resources.messages['filtered']} : ${tabsValidationsState.filteredData.length} | `
          : ''}
        {resources.messages['totalRecords']} {tabsValidationsState.validationList.validations.length}{' '}
        {resources.messages['records'].toLowerCase()}
        {tabsValidationsState.filtered &&
        tabsValidationsState.validationList.validations.length === tabsValidationsState.filteredData.length
          ? ` (${resources.messages['filtered'].toLowerCase()})`
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
        notificationContext.add({ type: 'DELETE_RULE_ERROR' });
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
        const validationsServiceList = await ValidationService.getAll(datasetSchemaId, reporting);
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

        tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_LIST', payload: { validationsServiceList } });
        validationContext.onSetRulesDescription(
          validationsServiceList?.validations?.map(validation => {
            return {
              automaticType: validation.automaticType,
              id: validation.id,
              description: validation.description,
              name: validation.name
            };
          })
        );
      } catch (error) {
        console.error('ValidationsList - onLoadValidationsList.', error);
        notificationContext.add({ type: 'VALIDATION_SERVICE_GET_ALL_ERROR' });
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
              tooltip={`${rowData.sqlError} <br />  <br />${resources.messages['sqlErrorMessageCopy']} `}
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
      if (!isNil(rowData.expressionText)) {
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
                    additionalInfo.tableName = !isUndefined(table.tableSchemaName)
                      ? table.tableSchemaName
                      : table.header;
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
          header = resources.messages['ruleLevelError'];
          break;
        case 'shortCode':
          header = resources.messages['ruleCode'];
          break;
        case 'isCorrect':
          header = resources.messages['valid'];
          break;
        default:
          header = resources.messages[fieldHeader];
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
        { id: 'entityType', index: 8 },
        { id: 'levelError', index: 9 },
        { id: 'automatic', index: 10 },
        { id: 'enabled', index: 11 },
        { id: 'referenceId', index: 12 },
        { id: 'activationGroup', index: 13 },
        { id: 'date', index: 14 },
        { id: 'actionButtons', index: 15 },
        { id: 'isCorrect', index: 16 }
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

    const editAndDeleteTemplate = row => {
      let rowType = 'field';

      if (row.entityType === 'RECORD') rowType = 'row';

      if (row.entityType === 'TABLE') rowType = 'dataset';

      const getDeleteBtnIcon = () => {
        if (row.id === tabsValidationsState.deletedRuleId && validationContext.isFetchingData) {
          return 'spinnerAnimate';
        }
        return 'trash';
      };

      return (
        <div className={styles.actionTemplate}>
          <Button
            className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
            disabled={
              (row.id === validationContext.updatedRuleId || row.id === tabsValidationsState.deletedRuleId) &&
              validationContext.isFetchingData
            }
            icon={getEditBtnIcon(row.id)}
            onClick={() => validationContext.onOpenToEdit(row, rowType)}
            type="button"
          />

          <Button
            className={`${`p-button-rounded p-button-secondary-transparent ${styles.deleteRowButton}`} p-button-animated-blink`}
            disabled={validationContext.isFetchingData}
            icon={getDeleteBtnIcon()}
            onClick={() => onShowDeleteDialog()}
            type="button"
          />
        </div>
      );
    };

    const editTemplate = row => {
      let rowType = 'field';

      if (row.entityType === 'RECORD') rowType = 'row';

      if (row.entityType === 'TABLE') rowType = 'dataset';

      return (
        <div className={styles.actionTemplate}>
          <Button
            className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} p-button-animated-blink`}
            disabled={
              (row.id === validationContext.updatedRuleId || row.id === tabsValidationsState.deletedRuleId) &&
              validationContext.isFetchingData
            }
            icon={getEditBtnIcon(row.id)}
            onClick={() => validationContext.onOpenToEdit(row, rowType)}
            type="button"
          />
        </div>
      );
    };

    const deleteValidationDialog = () => (
      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        disabledConfirm={tabsValidationsState.isDeletingRule}
        header={resources.messages['deleteValidationHeader']}
        iconConfirm={tabsValidationsState.isDeletingRule ? 'spinnerAnimate' : 'check'}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteValidation()}
        onHide={() => onHideDeleteDialog()}
        visible={tabsValidationsState.isDeleteDialogVisible}>
        {resources.messages['deleteValidationConfirm']}
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
        header={resources.messages['actions']}
        key="actions"
        sortable={false}
        style={{ width: '100px' }}
      />
    );

    const levelErrorTemplate = rowData => (
      <div className={styles.levelErrorTemplateWrapper}>
        <LevelError type={rowData.levelError.toLowerCase()} />
      </div>
    );

    const renderColumns = validations => {
      const fieldColumns = getOrderedValidations(Object.keys(validations[0])).map(field => {
        let template = null;
        if (field === 'automatic') template = automaticTemplate;
        if (field === 'enabled') template = enabledTemplate;
        if (field === 'isCorrect') template = correctTemplate;
        if (field === 'levelError') template = levelErrorTemplate;
        if (field === 'expressionText') template = expressionsTemplate;
        return (
          <Column
            body={template}
            columnResizeMode="expand"
            field={field}
            header={getHeader(field)}
            key={field}
            sortable={true}
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

    const filterOptions = [
      {
        type: 'multiselect',
        properties: [
          { name: 'table', showInput: true },
          { name: 'field', showInput: true },
          { name: 'entityType' },
          { name: 'levelError' },
          { name: 'automatic' },
          { name: 'enabled' },
          { name: 'isCorrect' }
        ]
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
            <div className={styles.noValidations}>{resources.messages['emptyValidations']}</div>
          </div>
        );
      }

      return (
        <div className={styles.validations}>
          <div className={styles.searchInput}>
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
              hasDefaultCurrentPage={true}
              loading={false}
              onRowClick={event => validationId(event.data.id)}
              paginator={true}
              paginatorRight={!isNil(tabsValidationsState.filteredData) && getPaginatorRecordsCount()}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={tabsValidationsState.validationList.validations.length}
              value={cloneDeep(tabsValidationsState.filteredData)}>
              {renderColumns(tabsValidationsState.validationList.validations)}
            </DataTable>
          ) : (
            <div className={styles.emptyFilteredData}>{resources.messages['noQCRulesWithSelectedParameters']}</div>
          )}
        </div>
      );
    };

    return (
      <Fragment>
        {validationList()}
        {tabsValidationsState.isDeleteDialogVisible && deleteValidationDialog()}
      </Fragment>
    );
  }
);

export { ValidationsList };
