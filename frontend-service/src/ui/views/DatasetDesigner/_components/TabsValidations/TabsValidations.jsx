import React, { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { capitalize, isEmpty, isUndefined } from 'lodash';

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

const TabsValidations = withRouter(({ datasetSchemaId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [isDataUpdated, setIsDataUpdated] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [validationId, setValidationId] = useState();
  const [validationsList, setValidationsList] = useState();

  useEffect(() => {
    onLoadValidationsList(datasetSchemaId);
  }, [isDataUpdated]);

  const onDeleteValidation = async () => {
    try {
      const response = await ValidationService.deleteById(datasetSchemaId, validationId);
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
      setIsLoading(true);
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      setValidationsList(validationsList);
    } catch (error) {
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

  const onUpdateData = () => {
    setIsDataUpdated(!isDataUpdated);
  };

  const automaticTemplate = rowData => (
    <div className={styles.checkedValueColumn} style={{ textAlign: 'center' }}>
      {rowData.automatic || rowData.enabled ? (
        <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ float: 'center', color: 'var(--main-color-font)' }} />
      ) : null}
    </div>
  );

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
      { id: 'shortCode', index: 1 },
      { id: 'name', index: 2 },
      { id: 'description', index: 3 },
      { id: 'levelError', index: 4 },
      { id: 'enabled', index: 5 },
      { id: 'automatic', index: 6 },
      { id: 'referenceId', index: 7 },
      { id: 'activationGroup', index: 8 },
      { id: 'condition', index: 9 },
      { id: 'date', index: 10 },
      { id: 'entityType', index: 11 },
      { id: 'actionButtons', index: 12 }
    ];
    return validations
      .map(error => validationsWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const actionTemplate = () => <ActionsColumn onDeleteClick={() => onShowDeleteDialog()} onEditClick={() => ''} />;

  const columnStyles = field => {
    const style = {};
    const invisibleFields = ['id', 'referenceId', 'activationGroup', 'condition', 'date', 'entityType'];
    if (field.toUpperCase() === 'DESCRIPTION') {
      style.width = '40%';
    } else {
      style.width = '20%';
    }
    if (invisibleFields.includes(field)) {
      style.display = 'none';
    } else {
      style.display = 'auto';
    }
    return style;
  };

  const renderColumns = validations => {
    const fieldColumns = getOrderedValidations(Object.keys(validations[0])).map(field => {
      return (
        <Column
          body={field === 'automatic' || field === 'enabled' ? automaticTemplate : null}
          key={field}
          columnResizeMode="expand"
          field={field}
          header={getHeader(field)}
          sortable={true}
          style={columnStyles(field)}
        />
      );
    });
    fieldColumns.push(
      <Column
        body={row => actionTemplate(row)}
        className={styles.validationCol}
        header={resources.messages['actions']}
        key="actions"
        sortable={false}
        style={{ width: '100px' }}
      />
    );
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

    return validationsList.entityTypes.map(entityType => {
      const validationsFilteredByEntityType = validationsList.validations.filter(
        validation => validation.entityType === entityType
      );
      const paginatorRightText = `${capitalize(entityType)} records: ${validationsFilteredByEntityType.length}`;
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
            totalRecords={validationsFilteredByEntityType.length}
            value={validationsFilteredByEntityType}>
            {renderColumns(validationsFilteredByEntityType)}
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
    });
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  return (
    <Fragment>
      {validationList()}

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['deleteValidationHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteValidation()}
        onHide={() => onHideDeleteDialog()}
        visible={isDeleteDialogVisible}>
        {resources.messages['deleteValidationConfirm']}
      </ConfirmDialog>
    </Fragment>
  );
});

export { TabsValidations };
