import React, { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './TabsValidations.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { SearchAll } from 'ui/views/_components/SearchAll';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView'; // Do not delete
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel'; // Do not delete

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { tabsValidationsReducer } from './Reducers/tabsValidationsReducer';

const TabsValidations = withRouter(({ dataset, datasetSchemaAllTables, datasetSchemaId, onHideValidationsDialog }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [validationsList, setValidationsList] = useState();

  const [tabsValidationsState, tabsValidationsDispatch] = useReducer(tabsValidationsReducer, {
    filteredData: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: false,
    searchedData: [],
    // validationList: {},
    validationId: ''
  });

  useEffect(() => {
    onLoadValidationsList(datasetSchemaId);
  }, [tabsValidationsState.isDataUpdated]);

  const isDeleteDialogVisible = value =>
    tabsValidationsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => tabsValidationsDispatch({ type: 'IS_LOADING', payload: { value } });

  const isDataUpdated = value => tabsValidationsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const onDeleteValidation = async () => {
    try {
      const response = await ValidationService.deleteById(dataset.datasetId, tabsValidationsState.validationId);
      if (response.status >= 200 && response.status <= 299) {
        onUpdateData();
      }
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_RULE_ERROR'
      });
    } finally {
      onHideDeleteDialog();
    }
  };

  const onHideDeleteDialog = () => {
    isDeleteDialogVisible(false);
    validationId('');
  };

  const onLoadValidationsList = async datasetSchemaId => {
    try {
      isLoading(true);
      const validationsServiceList = await ValidationService.getAll(datasetSchemaId);

      if (!isNil(validationsServiceList) && !isNil(validationsServiceList.validations)) {
        validationsServiceList.validations.forEach(validation => {
          const aditionalInfo = getAditionalValidationInfo(validation.referenceId);
          validation.table = aditionalInfo.tableName;
          validation.field = aditionalInfo.fieldName;
        });
      }

      // tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_LIST', payload: { validationsServiceList } });
      setValidationsList(validationsServiceList);
    } catch (error) {
      console.log(error);
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_ERROR'
      });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => tabsValidationsDispatch({ type: 'FILTER_DATA', payload: { data } });

  const onLoadSearchedData = data => tabsValidationsDispatch({ type: 'SEARCHED_DATA', payload: { data } });

  const onShowDeleteDialog = () => {
    isDeleteDialogVisible(true);
  };

  const onUpdateData = () => {
    isDataUpdated(!tabsValidationsState.isDataUpdated);
  };

  const automaticTemplate = rowData => (
    <div className={styles.checkedValueColumn} style={{ textAlign: 'center' }}>
      {rowData.automatic ? (
        <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--main-color-font)' }} />
      ) : null}
    </div>
  );

  const enabledTemplate = rowData => (
    <div className={styles.checkedValueColumn} style={{ textAlign: 'center' }}>
      {rowData.enabled ? (
        <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--main-color-font)' }} />
      ) : null}
    </div>
  );

  const getAditionalValidationInfo = referenceId => {
    const aditionalInfo = {};
    datasetSchemaAllTables.forEach(table => {
      if (!isUndefined(table.records)) {
        table.records.forEach(record =>
          record.fields.forEach(field => {
            if (!isNil(field)) {
              if (field.fieldId === referenceId) {
                aditionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
                aditionalInfo.fieldName = field.name;
              }
            }
          })
        );
      }
    });
    return aditionalInfo;
  };

  const getHeader = fieldHeader => {
    let header;
    if (fieldHeader === 'levelError') {
      header = 'Level error';
      return header;
    }
    if (fieldHeader === 'shortCode') {
      header = 'Code';
      return header;
    }
    header = fieldHeader;
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
      { id: 'levelError', index: 6 },
      { id: 'enabled', index: 7 },
      { id: 'automatic', index: 8 },
      { id: 'referenceId', index: 9 },
      { id: 'activationGroup', index: 10 },
      { id: 'date', index: 11 },
      { id: 'entityType', index: 12 },
      { id: 'actionButtons', index: 13 }
    ];
    return validations
      .map(error => validationsWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const actionsTemplate = row => (
    <ActionsColumn
      onDeleteClick={() => onShowDeleteDialog()}
      onEditClick={() => {
        validationContext.onOpenToEdit(row, 'validationsListDialog');
        onHideValidationsDialog();
      }}
    />
  );

  const deleteTemplate = () => <ActionsColumn onDeleteClick={() => onShowDeleteDialog()} />;

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
    const invisibleFields = ['id', 'referenceId', 'activationGroup', 'condition', 'date', 'entityType'];
    if (field.toUpperCase() === 'DESCRIPTION') {
      style.width = '40%';
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
      body={row => (row.automatic ? deleteTemplate() : actionsTemplate(row))}
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
      if (field === 'automatic') {
        template = automaticTemplate;
      }
      if (field === 'enabled') {
        template = enabledTemplate;
      }
      if (field === 'levelError') {
        template = levelErrorTemplate;
      }
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
    fieldColumns.push(actionButtonsColumn);
    return fieldColumns;
  };

  const validationListConf = {
    filterItems: {
      input: ['table', 'field'],
      select: ['entityType', 'levelError', 'enabled']
    }
  };

  const validationId = value => tabsValidationsDispatch({ type: 'ON_LOAD_VALIDATION_ID', payload: { value } });

  console.log('validationList', validationsList);
  const validationList = () => {
    if (isUndefined(validationsList) || isEmpty(validationsList)) {
      // if (isUndefined(tabsValidationsState.validationsList) || isEmpty(tabsValidationsState.validationsList)) {
      return (
        <div>
          <h3>{resources.messages['emptyValidations']}</h3>
        </div>
      );
    }
    const paginatorRightText = `${capitalize('FIELD')} records: ${
      // tabsValidationsState.validationsList.validations.length
      validationsList.validations.length
    }`;

    return (
      <div className={null}>
        <div className={styles.searchInput}>
          <SearchAll
            data={tabsValidationsState.filteredData}
            getValues={onLoadSearchedData}
            searchBy={['name', 'description']}
          />
        </div>
        <Filters
          // data={tabsValidationsState.validationsList.validations}
          data={validationsList.validations}
          getFiltredData={onLoadFilteredData}
          inputOptions={validationListConf.filterItems['input']}
          selectOptions={validationListConf.filterItems['select']}
          sortable={false}
        />

        {!isEmpty(tabsValidationsState.searchedData) ? (
          <DataTable
            autoLayout={true}
            className={styles.paginatorValidationViewer}
            loading={false}
            onRowClick={event => validationId(event.data.id)}
            paginator={true}
            paginatorRight={paginatorRightText}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            // totalRecords={tabsValidationsState.validationsList.validations.length}
            totalRecords={validationsList.validations.length}
            value={tabsValidationsState.searchedData}>
            {/* {renderColumns(tabsValidationsState.validationsList.validations)} */}
            {renderColumns(validationsList.validations)}
          </DataTable>
        ) : (
          <div className={styles.noDataflows}>{resources.messages['noQCRulesWithSelectedParameters']}</div>
        )}
      </div>

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

  if (tabsValidationsState.isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  return (
    <Fragment>
      {validationList()}
      {tabsValidationsState.isDeleteDialogVisible && deleteValidationDialog()}
    </Fragment>
  );
});

export { TabsValidations };
