import React, { Fragment, useContext, useEffect, useState } from 'react';
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
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView'; // Do not delete
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel'; // Do not delete

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

const TabsValidations = withRouter(({ dataset, datasetSchemaAllTables, datasetSchemaId, onHideValidationsDialog }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [validationId, setValidationId] = useState();
  const [validationsList, setValidationsList] = useState();

  useEffect(() => {
    onLoadValidationsList(datasetSchemaId);
  }, [isDataUpdated]);

  useEffect(() => {
    const response = notificationContext.hidden.find(notification => notification === 'VALIDATED_QC_RULE_EVENT');
    if (response) onUpdateData();
  }, [notificationContext]);

  const onDeleteValidation = async () => {
    try {
      const response = await ValidationService.deleteById(dataset.datasetId, validationId);
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
    setIsDeleteDialogVisible(false);
    setValidationId('');
  };

  const onLoadValidationsList = async datasetSchemaId => {
    try {
      const validationsServiceList = await ValidationService.getAll(datasetSchemaId);

      if (!isNil(validationsServiceList) && !isNil(validationsServiceList.validations)) {
        validationsServiceList.validations.forEach(validation => {
          const aditionalInfo = getAditionalValidationInfo(validation.referenceId);
          validation.table = aditionalInfo.tableName;
          validation.field = aditionalInfo.fieldName;
        });
      }

      setValidationsList(validationsServiceList);
    } catch (error) {
      console.log(error);
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_ERROR'
      });
    } finally {
      setIsLoading(false);
    }
  };

  const onShowDeleteDialog = () => {
    setIsDeleteDialogVisible(true);
  };

  const onUpdateData = () => setIsDataUpdated(!isDataUpdated);

  useCheckNotifications(['INVALIDATED_QC_RULE_EVENT'], onUpdateData);

  const automaticTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.automatic ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const correctTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {!isNil(rowData.isCorrect) ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons(rowData.isCorrect ? 'check' : 'cross')} />
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
    if (fieldHeader === 'isCorrect') {
      header = 'Correct';
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
      { id: 'actionButtons', index: 13 },
      { id: 'isCorrect', index: 14 }
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
      visible={isDeleteDialogVisible}
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

  const renderColumns = validations => {
    const fieldColumns = getOrderedValidations(Object.keys(validations[0])).map(field => {
      let template = null;
      if (field === 'automatic') template = automaticTemplate;
      if (field === 'enabled') template = enabledTemplate;
      if (field === 'isCorrect') template = correctTemplate;
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

  const validationList = () => {
    if (isUndefined(validationsList) || isEmpty(validationsList)) {
      return (
        <div>
          <h3>{resources.messages['emptyValidations']}</h3>
        </div>
      );
    }
    const paginatorRightText = `${capitalize('FIELD')} records: ${validationsList.validations.length}`;
    return (
      <div className={null}>
        <DataTable
          autoLayout={true}
          className={styles.paginatorValidationViewer}
          loading={false}
          onRowClick={event => setValidationId(event.data.id)}
          paginator={true}
          paginatorRight={paginatorRightText}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={validationsList.validations.length}
          value={validationsList.validations}>
          {renderColumns(validationsList.validations)}
        </DataTable>
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

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  return (
    <Fragment>
      {validationList()}
      {isDeleteDialogVisible && deleteValidationDialog()}
    </Fragment>
  );
});

export { TabsValidations };
