import React, { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ManageUniqueConstraint.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';

import { UniqueConstraintsService } from 'core/services/UniqueConstraints';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ManageUniqueConstraint = ({ designerState, manageDialogs, resetUniques }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const {
    datasetSchemaAllTables,
    datasetSchemaId,
    isManageUniqueConstraintDialogVisible,
    manageUniqueConstraintData
  } = designerState;

  const { fieldData, isTableCreationMode, tableSchemaId, tableSchemaName, uniqueId } = manageUniqueConstraintData;

  const [selectedFields, setSelectedFields] = useState([]);
  const [selectedTable, setSelectedTable] = useState({ name: '', value: null });

  const uniqueListDialog = isTableCreationMode ? '' : 'isUniqueConstraintsListDialogVisible';

  useEffect(() => {
    if (isManageUniqueConstraintDialogVisible) {
      const fields = fieldData.map(field => ({ name: field.name, value: field.fieldId }));
      getTableOptions();
      setSelectedFields(fields);
      setSelectedTable({ name: tableSchemaName, value: tableSchemaId });
    }
  }, [isManageUniqueConstraintDialogVisible, manageUniqueConstraintData]);

  useEffect(() => {
    getFieldOptions();
  }, [selectedTable]);

  const getTableOptions = () => {
    const tables = datasetSchemaAllTables.filter(table => table.index >= 0);
    return tables.map(table => ({ name: table.tableSchemaName, value: table.tableSchemaId }));
  };

  const getFieldOptions = () => {
    if (selectedTable.value) {
      const table = datasetSchemaAllTables.filter(table => table.tableSchemaId === selectedTable.value)[0];
      if (table.records) {
        return !isEmpty(table.records[0].fields)
          ? table.records[0].fields.map(field => ({ name: field.name, value: field.fieldId }))
          : [{ name: resources.messages['noFieldsToSelect'], disabled: true }];
      }
    } else return [{ name: resources.messages['noTableSelected'], disabled: true }];
  };

  const onCreateConstraint = async () => {
    try {
      const response = await UniqueConstraintsService.create(
        datasetSchemaId,
        selectedFields.map(field => field.value),
        selectedTable.value
      );
      if (response.status >= 200 && response.status <= 299) {
        manageDialogs('isManageUniqueConstraintDialogVisible', false, uniqueListDialog, true);
      }
    } catch (error) {
      notificationContext.add({ type: 'CREATE_UNIQUE_CONSTRAINT_ERROR' });
    } finally {
      onResetValues();
    }
  };

  const onUpdateConstraint = async () => {
    try {
      const response = await UniqueConstraintsService.update(
        datasetSchemaId,
        selectedFields.map(field => field.value),
        selectedTable.value,
        manageUniqueConstraintData.uniqueId
      );
      if (response.status >= 200 && response.status <= 299) {
        manageDialogs('isManageUniqueConstraintDialogVisible', false, uniqueListDialog, true);
      }
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_UNIQUE_CONSTRAINT_ERROR' });
    } finally {
      onResetValues();
    }
  };

  const onResetValues = () => {
    resetUniques({
      fieldData: [],
      isTableCreationMode: false,
      tableSchemaId: null,
      tableSchemaName: '',
      uniqueId: null
    });
  };

  const renderDialogLayout = children =>
    isManageUniqueConstraintDialogVisible && (
      <Dialog
        className={styles.dialog}
        footer={renderFooter}
        header={
          !isNil(uniqueId) ? resources.messages['editUniqueConstraint'] : resources.messages['createUniqueConstraint']
        }
        onHide={() => manageDialogs('isManageUniqueConstraintDialogVisible', false, uniqueListDialog, true)}
        style={{ width: '975px' }}
        visible={isManageUniqueConstraintDialogVisible}>
        {children}
      </Dialog>
    );

  const renderFooter = (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        disabled={isEmpty(selectedFields)}
        icon={!isNil(uniqueId) ? 'check' : 'plus'}
        label={!isNil(uniqueId) ? resources.messages['edit'] : resources.messages['create']}
        onClick={() => (!isNil(uniqueId) ? onUpdateConstraint() : onCreateConstraint())}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => {
          manageDialogs('isManageUniqueConstraintDialogVisible', false, uniqueListDialog, true);
          onResetValues();
        }}
      />
    </Fragment>
  );

  const renderListBox = () => (
    <ListBox
      onChange={event => !isNil(event.value) && setSelectedTable(event.value)}
      optionLabel="name"
      options={getTableOptions()}
      optionValue="value"
      title={'Select a table'}
      value={selectedTable}
    />
  );

  const renderListBoxField = () => (
    <ListBox
      disabled={isNil(selectedTable)}
      listStyle={{ height: '200px' }}
      multiple={true}
      onChange={event => !isNil(event.value) && setSelectedFields(event.value)}
      optionLabel="name"
      options={getFieldOptions()}
      optionValue="value"
      title={'Select unique fields'}
      value={selectedFields}
    />
  );

  return renderDialogLayout(
    <Fragment>
      <div className={styles.listBoxWrap}>
        {renderListBox()}
        {renderListBoxField()}
      </div>
      <div className={styles.selected}>
        <span className={styles.title}>{`${resources.messages['selectedTable']}: `}</span>
        <span>{!isNil(selectedTable.value) ? selectedTable.name : ''}</span>
      </div>
      <div className={`${styles.selected} ${styles.fields}`}>
        <span className={styles.title}>{`${resources.messages['selectedFields']}: `}</span>
        <span>{!isEmpty(selectedFields) ? selectedFields.map(field => field.name).join(', ') : ''}</span>
      </div>
    </Fragment>
  );
};
