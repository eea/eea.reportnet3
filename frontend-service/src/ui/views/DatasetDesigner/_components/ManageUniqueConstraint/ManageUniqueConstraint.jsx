import React, { Fragment, useContext, useState, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ManageUniqueConstraint.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from '../TabsDesigner/_components/FieldsDesigner/_components/FieldDesigner/_components/LinkSelector/_components/ListBox';

import { UniqueConstraintsService } from 'core/services/UniqueConstraints';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ManageUniqueConstraint = ({ allTables, designerState, isUpdate, isVisible, manageDialogs }) => {
  const resources = useContext(ResourcesContext);

  const [selectedFields, setSelectedFields] = useState([]);
  const [selectedTable, setSelectedTable] = useState(null);

  const {
    datasetSchemaAllTables,
    datasetSchemaId,
    isManageUniqueConstraintDialogVisible,
    manageUniqueConstraintData
  } = designerState;

  console.log('designerState', designerState);
  console.log('manageUniqueConstraintData', manageUniqueConstraintData);

  useEffect(() => {
    getTableOptions();
    getFieldOptions();
  }, [selectedTable]);

  const getTableOptions = () => {
    const tables = datasetSchemaAllTables.filter(table => table.index >= 0);
    return tables.map(table => {
      return {
        name: `${table.tableSchemaName}`,
        value: `${table.tableSchemaId}`
      };
    });
  };

  const getFieldOptions = () => {
    if (selectedTable) {
      const table = datasetSchemaAllTables.filter(table => table.tableSchemaId === selectedTable.value)[0];
      if (table.records) {
        if (!isEmpty(table.records[0].fields)) {
          return table.records[0].fields.map(field => {
            return {
              name: `${field.name}`,
              value: `${field.fieldId}`
            };
          });
        } else return [{ name: 'There are no fields to select', disabled: true }];
      }
    }
  };

  const onCreateConstraint = async () => {
    manageDialogs('isManageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true);
    try {
      await UniqueConstraintsService.create(
        datasetSchemaId,
        selectedFields.map(field => field.value),
        selectedTable.value
      );
    } catch (error) {
      console.log('error', error);
    } finally {
      onResetConstraintValues();
    }
  };

  const onUpdateConstraint = async () => {
    manageDialogs('isManageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true);
    try {
      await UniqueConstraintsService.update(
        datasetSchemaId,
        selectedFields.map(field => field.value),
        selectedTable.value,
        manageUniqueConstraintData.uniqueId
      );
    } catch (error) {
      console.log('error', error);
    } finally {
      onResetConstraintValues();
    }
  };

  const onResetConstraintValues = () => {
    setSelectedFields([]);
    setSelectedTable(null);
  };

  const renderDialogLayout = children =>
    isManageUniqueConstraintDialogVisible && (
      <Dialog
        className={styles.dialog}
        footer={renderFooter}
        header={
          !isNil(designerState.manageUniqueConstraintData)
            ? resources.messages['editUniqueConstraint']
            : resources.messages['createUniqueConstraint']
        }
        onHide={() =>
          manageDialogs('isManageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true)
        }
        style={{ width: '975px' }}
        visible={isManageUniqueConstraintDialogVisible}>
        {children}
      </Dialog>
    );

  const renderFooter = (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        icon={!isNil(designerState.manageUniqueConstraintData) ? 'check' : 'plus'}
        label={
          !isNil(designerState.manageUniqueConstraintData) ? resources.messages['edit'] : resources.messages['create']
        }
        onClick={() => (!isNil(designerState.manageUniqueConstraintData) ? onUpdateConstraint() : onCreateConstraint())}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => {
          manageDialogs('isManageUniqueConstraintDialogVisible', false, 'uniqueConstraintListDialogVisible', true);
          onResetConstraintValues();
        }}
      />
    </Fragment>
  );

  const renderListBox = () => {
    return (
      <ListBox
        onChange={event => !isNil(event.value) && setSelectedTable(event.value)}
        optionLabel="name"
        options={getTableOptions()}
        optionValue="value"
        title={'Select a table'}
        value={selectedTable}
      />
    );
  };

  const renderListBoxField = () => {
    return (
      <ListBox
        // filterProp={true}
        // filterPlaceholder="Search"
        disabled={isNil(selectedTable)}
        multiple={true}
        onChange={event => !isNil(event.value) && setSelectedFields(event.value)}
        optionLabel="name"
        options={getFieldOptions()}
        optionValue="value"
        title={'Select unique fields'}
        value={selectedFields}
      />
    );
  };

  return renderDialogLayout(
    <form>
      <div className={styles.schemaWrapper}>
        {renderListBox()}
        {renderListBoxField()}
      </div>
      <div className={styles.selected}>
        <span className={styles.title}>{`${resources.messages['selectedTable']}: `}</span>
        <span>
          {!isNil(manageUniqueConstraintData.uniqueId)
            ? !isNil(selectedTable)
              ? selectedTable.name
              : manageUniqueConstraintData.tableSchemaName
            : !isNil(selectedTable)
            ? selectedTable.name
            : ''}
        </span>
        {/* <span>{!isNil(selectedTable) ? selectedTable.name : ''}</span> */}
      </div>
      <div className={`${styles.selected} ${styles.fields}`}>
        <span className={styles.title}>{`${resources.messages['selectedFields']}: `}</span>
        <span>
          {!isNil(manageUniqueConstraintData.uniqueId)
            ? !isEmpty(selectedFields)
              ? selectedFields.map(field => field.name)
              : manageUniqueConstraintData.fieldData.map(field => field.name)
            : !isEmpty(selectedFields)
            ? selectedFields.map(field => field.name)
            : ''}
        </span>
        {/* <span>{!isEmpty(selectedFields) ? selectedFields.map(field => field.name) : ''}</span> */}
        {/* <textarea name="" id="" cols="30" readOnly rows="2" value={selectedFields.map(field => field.name)}></textarea> */}
      </div>
    </form>
  );
};
