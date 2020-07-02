import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './TabsValidations.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel'; // Do not delete
import { TabView } from 'ui/views/_components/TabView'; // Do not delete

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { tabsValidationsReducer } from './Reducers/tabsValidationsReducer';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

const TabsValidations = withRouter(
  ({ dataset, datasetSchemaAllTables, datasetSchemaId, onHideValidationsDialog, reporting = false }) => {
    const notificationContext = useContext(NotificationContext);
    const resources = useContext(ResourcesContext);
    const validationContext = useContext(ValidationContext);

    const [tabsValidationsState, tabsValidationsDispatch] = useReducer(tabsValidationsReducer, {
      filteredData: [],
      isDataUpdated: false,
      isDeleteDialogVisible: false,
      isLoading: true,
      validationId: '',
      validationList: {}
    });

    useEffect(() => {
      onLoadValidationsList(datasetSchemaId);
    }, [tabsValidationsState.isDataUpdated]);

    useEffect(() => {
      const response = notificationContext.hidden.find(notification => notification === 'VALIDATED_QC_RULE_EVENT');
      if (response) onUpdateData();
    }, [notificationContext]);

    const isDeleteDialogVisible = value =>
      tabsValidationsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

    const isLoading = value => tabsValidationsDispatch({ type: 'IS_LOADING', payload: { value } });

    const isDataUpdated = value => tabsValidationsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

    const onDeleteValidation = async () => {
      try {
        const response = await ValidationService.deleteById(dataset.datasetId, tabsValidationsState.validationId);
        if (response.status >= 200 && response.status <= 299) onUpdateData();
      } catch (error) {
        notificationContext.add({ type: 'DELETE_RULE_ERROR' });
      } finally {
        onHideDeleteDialog();
      }
    };

    const onHideDeleteDialog = () => {
      isDeleteDialogVisible(false);
      validationId('');
    };

    const onLoadFilteredData = data => tabsValidationsDispatch({ type: 'FILTER_DATA', payload: { data } });

    const onLoadValidationsList = async datasetSchemaId => {
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
          });
        }

        tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_LIST', payload: { validationsServiceList } });
      } catch (error) {
        console.error(error);
        notificationContext.add({ type: 'VALIDATION_SERVICE_GET_ALL_ERROR' });
      } finally {
        isLoading(false);
      }
    };

    const onShowDeleteDialog = () => isDeleteDialogVisible(true);

    const onUpdateData = () => isDataUpdated(!tabsValidationsState.isDataUpdated);

    useCheckNotifications(['INVALIDATED_QC_RULE_EVENT'], onUpdateData);

    const automaticTemplate = rowData => (
      <div className={styles.checkedValueColumn}>
        {rowData.automatic ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
      </div>
    );

    const correctTemplate = rowData => (
      <div className={styles.checkedValueColumn}>
        {!isNil(rowData.isCorrect) ? (
          rowData.isCorrect ? (
            <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} />
          ) : null
        ) : (
          <FontAwesomeIcon className={`${styles.icon} ${styles.spinner}`} icon={AwesomeIcons('spinner')} />
        )}
      </div>
    );

    const enabledTemplate = rowData => (
      <div className={styles.checkedValueColumn}>
        {rowData.enabled ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
      </div>
    );

    const getAdditionalValidationInfo = (referenceId, entityType, relations) => {
      const additionalInfo = {};
      datasetSchemaAllTables.forEach(table => {
        if (!isUndefined(table.records)) {
          if (entityType.toUpperCase() === 'TABLE' || entityType.toUpperCase() === 'RECORD') {
            additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
          } else if (entityType.toUpperCase() === 'FIELD' || entityType.toUpperCase() === 'DATASET') {
            table.records.forEach(record =>
              record.fields.forEach(field => {
                if (!isNil(field)) {
                  if (entityType.toUpperCase() === 'FIELD') {
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
        case 'entityType':
          header = resources.messages['entityType'];
          break;
        default:
          header = fieldHeader;
          break;
      }

      return capitalize(header);
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
        { id: 'entityType', index: 7 },
        { id: 'levelError', index: 8 },
        { id: 'automatic', index: 9 },
        { id: 'enabled', index: 10 },
        { id: 'referenceId', index: 11 },
        { id: 'activationGroup', index: 12 },
        { id: 'date', index: 13 },
        { id: 'actionButtons', index: 14 },
        { id: 'isCorrect', index: 15 }
      ];

      return validations
        .map(error => validationsWithPriority.filter(e => error === e.id))
        .flat()
        .sort((a, b) => a.index - b.index)
        .map(orderedError => orderedError.id);
    };

    const actionsTemplate = row => (row.automatic ? editTemplate(row) : editAndDeleteTemplate(row));

    const editAndDeleteTemplate = row => {
      let rowType = 'field';

      if (row.entityType === 'RECORD' || row.entityType === 'TABLE') {
        rowType = 'row';
      } else if (row.entityType === 'DATASET') {
        rowType = 'dataset';
      }

      return (
        <ActionsColumn
          onDeleteClick={() => onShowDeleteDialog()}
          onEditClick={() => {
            validationContext.onOpenToEdit(row, 'validationsListDialog', rowType);
            onHideValidationsDialog();
          }}
        />
      );
    };

    const editTemplate = row => {
      let rowType = 'field';
      if (row.entityType === 'RECORD' || row.entityType === 'TABLE') rowType = 'row';
      return (
        <ActionsColumn
          onEditClick={() => {
            validationContext.onOpenToEdit(row, 'validationsListDialog', rowType);
            onHideValidationsDialog();
          }}
        />
      );
    };

    const deleteValidationDialog = () => (
      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['deleteValidationHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteValidation()}
        onHide={() => onHideDeleteDialog()}
        visible={tabsValidationsState.isDeleteDialogVisible}
        maximizable={false}>
        {resources.messages['deleteValidationConfirm']}
      </ConfirmDialog>
    );

    const columnStyles = field => {
      const style = {};
      const invisibleFields = ['id', 'referenceId', 'activationGroup', 'condition', 'date'];
      if (reporting) {
        invisibleFields.push('enabled', 'automatic', 'isCorrect');
      }
      if (field.toUpperCase() === 'DESCRIPTION') {
        style.width = '23%';
      }
      // else {
      //   style.width = '20%';
      // }
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
      <span className={`${styles.levelError} ${styles[rowData.levelError.toLowerCase()]}`}>{rowData.levelError}</span>
    );

    const renderColumns = validations => {
      const fieldColumns = getOrderedValidations(Object.keys(validations[0])).map(field => {
        let template = null;
        if (field === 'automatic') template = automaticTemplate;
        if (field === 'enabled') template = enabledTemplate;
        if (field === 'isCorrect') template = correctTemplate;
        if (field === 'levelError') template = levelErrorTemplate;
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

    const validationList = () => {
      if (isUndefined(tabsValidationsState.validationList) || isEmpty(tabsValidationsState.validationList)) {
        return (
          <div>
            <h3>{resources.messages['emptyValidations']}</h3>
          </div>
        );
      }

      const paginatorRightText = `${resources.messages['fieldRecords']} ${tabsValidationsState.filteredData.length}`;

      return (
        <Fragment>
          <div className={styles.searchInput}>
            <Filters
              className="filter-lines"
              data={tabsValidationsState.validationList.validations}
              getFilteredData={onLoadFilteredData}
              searchAll
              searchBy={['name', 'description', 'message']}
              selectOptions={['table', 'field', 'entityType', 'levelError', 'automatic', 'enabled', 'isCorrect']}
            />
          </div>

          {!isEmpty(tabsValidationsState.filteredData) ? (
            <DataTable
              autoLayout={true}
              className={styles.paginatorValidationViewer}
              loading={false}
              onRowClick={event => validationId(event.data.id)}
              paginator={true}
              paginatorRight={paginatorRightText}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={tabsValidationsState.validationList.validations.length}
              value={tabsValidationsState.filteredData}>
              {renderColumns(tabsValidationsState.validationList.validations)}
            </DataTable>
          ) : (
            <div className={styles.noDataflows}>{resources.messages['noQCRulesWithSelectedParameters']}</div>
          )}
        </Fragment>

        // <TabPanel header={entityType} key={entityType} rightIcon={null}>
        //   <div className={null}>
        //     <DataTable
        //       autoLayout={true}
        //       className={null}
        //       loading={false}
        //       paginator={true}
        //       paginatorRight={paginatorRightText}
        //       rows={10}
        //       rowsPerPageOptions={[5, 10, 15]}
        //       totalRecords={validationsFilteredByEntityType.length}
        //       value={validationsFilteredByEntityType}>
        //       {columns}
        //     </DataTable>
        //   </div>
        // </TabPanel>
      );
      // });
    };

    if (tabsValidationsState.isLoading) return <Spinner className={styles.positioning} />;

    return (
      <div className={styles.validations}>
        {validationList()}
        {tabsValidationsState.isDeleteDialogVisible && deleteValidationDialog()}
      </div>
    );
  }
);

export { TabsValidations };
