import { Fragment, useContext, useEffect, useState } from 'react';
import ReactTooltip from 'react-tooltip';

import isEmpty from 'lodash/isEmpty';
import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';

import styles from './ManageUniqueConstraint.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { ListBox } from 'views/DatasetDesigner/_components/ListBox';

import { UniqueConstraintService } from 'services/UniqueConstraintService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageUniqueConstraint = ({
  dataflowId,
  designerState,
  manageDialogs,
  refreshList,
  resetUniques,
  setConstraintManagingId,
  setIsUniqueConstraintCreating,
  setIsUniqueConstraintUpdating
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const {
    datasetSchemaAllTables,
    datasetSchemaId,
    isDuplicatedToManageUnique,
    isManageUniqueConstraintDialogVisible,
    manageUniqueConstraintData,
    uniqueConstraintsList
  } = designerState;

  const { fieldData, isTableCreationMode, tableSchemaId, tableSchemaName, uniqueId } = manageUniqueConstraintData;

  const [duplicatedList, setDuplicatedList] = useState([]);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [selectedFields, setSelectedFields] = useState([]);
  const [selectedTable, setSelectedTable] = useState({ name: '', value: null });
  const [isCreating, setIsCreating] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);

  useEffect(() => {
    if (isManageUniqueConstraintDialogVisible) {
      const fields = fieldData.map(field => ({ name: field.name, value: field.fieldId }));
      getTableOptions();
      setSelectedFields(fields);
      setSelectedTable({ name: tableSchemaName, value: tableSchemaId });
    }

    if (isTableCreationMode) {
      onLoadUniquesList();
    }
  }, [isManageUniqueConstraintDialogVisible, manageUniqueConstraintData]);

  useEffect(() => {
    getFieldOptions();
  }, [selectedTable]);

  useEffect(() => {
    setIsDuplicated(isDuplicatedToManageUnique);
  }, [uniqueConstraintsList]);

  useEffect(() => {
    if (!isEmpty(uniqueConstraintsList) || !isEmpty(duplicatedList)) {
      setIsDuplicated(checkDuplicates());
    }
  }, [selectedFields, duplicatedList]);

  const checkDuplicates = () => {
    const updatedFields = fieldData.map(field => field.fieldId);
    const creationFields = duplicatedList.map(unique => unique.fieldSchemaIds);
    const totalFields = uniqueConstraintsList.map(unique => unique.fieldData.map(item => item.fieldId));
    const filteredFields = (isTableCreationMode ? creationFields : totalFields).filter(
      field => !isEqual(field.sort(), updatedFields.sort())
    );
    const fields = selectedFields.map(field => field.value);
    const duplicated = [];
    for (let index = 0; index < filteredFields.length; index++) {
      duplicated.push(isEqual(filteredFields[index].sort(), fields.sort()));
    }

    return duplicated.includes(true);
  };

  const getTableOptions = () => {
    const tables = datasetSchemaAllTables.filter(table => table.index >= 0);
    return isEmpty(tables)
      ? [{ name: resourcesContext.messages['noTablesToSelect'], disabled: true }]
      : tables.map(table => ({ name: table.tableSchemaName, value: table.tableSchemaId }));
  };

  const getFieldOptions = () => {
    if (selectedTable.value) {
      const table = datasetSchemaAllTables.filter(table => table.tableSchemaId === selectedTable.value)[0];
      if (table && table.records) {
        return !isEmpty(table.records[0].fields)
          ? table.records[0].fields.map(field => ({ name: field.name, value: field.fieldId }))
          : [{ name: resourcesContext.messages['noFieldsToSelect'], disabled: true }];
      }
    } else return [{ name: resourcesContext.messages['noTableSelected'], disabled: true }];
  };

  const pkTemplate = option => {
    let isPk = false;
    const table = datasetSchemaAllTables.filter(table => table.tableSchemaId === selectedTable.value)[0];
    if (table && table.records) {
      const filteredField = table.records[0].fields.filter(field => field.fieldId === option.value);
      if (filteredField[0]) {
        if (filteredField[0].pk) {
          isPk = true;
        }
      }
    }

    return (
      <div className={styles.pkFieldWrapper}>
        {`${option.name}`}
        {isPk ? <span className={styles.pkField}>{'PK'}</span> : ''}
      </div>
    );
  };

  const onCreateConstraint = async () => {
    try {
      setIsUniqueConstraintCreating(true);
      setIsCreating(true);
      await UniqueConstraintService.create(
        dataflowId,
        datasetSchemaId,
        selectedFields.map(field => field.value),
        selectedTable.value
      );
      manageDialogs('isManageUniqueConstraintDialogVisible', false);
      onResetValues();
      refreshList(true);
    } catch (error) {
      console.error('ManageUniqueConstraint - onCreateConstraint.', error);
      notificationContext.add({ type: 'CREATE_UNIQUE_CONSTRAINT_ERROR' }, true);
    } finally {
      setIsCreating(false);
    }
  };

  const onLoadUniquesList = async () => {
    try {
      const uniqueConstraintList = await UniqueConstraintService.getAll(dataflowId, datasetSchemaId);
      setDuplicatedList(uniqueConstraintList);
    } catch (error) {
      console.error('ManageUniqueConstraint - onLoadUniquesList.', error);
    }
  };

  const onUpdateConstraint = async () => {
    setConstraintManagingId(uniqueId);
    const fieldsInUniqueConstraint = fieldData.map(field => field.fieldId);
    const selectedFieldsInUniqueConstraint = selectedFields.map(field => field.value);

    const noChangedConstraint = isEqual(fieldsInUniqueConstraint.sort(), selectedFieldsInUniqueConstraint.sort());

    if (noChangedConstraint) {
      manageDialogs('isManageUniqueConstraintDialogVisible', false);
      onResetValues();
    } else {
      try {
        setIsUpdating(true);
        setIsUniqueConstraintUpdating(true);
        await UniqueConstraintService.update(
          dataflowId,
          datasetSchemaId,
          selectedFields.map(field => field.value),
          selectedTable.value,
          uniqueId
        );
        manageDialogs('isManageUniqueConstraintDialogVisible', false);
        onResetValues();
        refreshList(true);
      } catch (error) {
        console.error('ManageUniqueConstraint - onUpdateConstraint.', error);
        notificationContext.add({ type: 'UPDATE_UNIQUE_CONSTRAINT_ERROR' }, true);
      } finally {
        setIsUpdating(false);
      }
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
          !isNil(uniqueId)
            ? resourcesContext.messages['editUniqueConstraint']
            : resourcesContext.messages['createUniqueConstraint']
        }
        onHide={() => manageDialogs('isManageUniqueConstraintDialogVisible', false)}
        style={{ width: '975px' }}
        visible={isManageUniqueConstraintDialogVisible}>
        {children}
      </Dialog>
    );

  const renderFooter = (
    <Fragment>
      <span data-for="createTooltip" data-tip>
        <Button
          className={`p-button-primary ${!isEmpty(selectedFields) && !isDuplicated ? 'p-button-animated-blink' : ''}`}
          disabled={isEmpty(selectedFields) || isDuplicated || isCreating || isUpdating}
          icon={isCreating || isUpdating ? 'spinnerAnimate' : 'check'}
          label={!isNil(uniqueId) ? resourcesContext.messages['update'] : resourcesContext.messages['create']}
          onClick={() => (!isNil(uniqueId) ? onUpdateConstraint() : onCreateConstraint())}
        />
      </span>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resourcesContext.messages['close']}
        onClick={() => {
          manageDialogs('isManageUniqueConstraintDialogVisible', false);
          onResetValues();
        }}
      />
      {isDuplicated && (
        <ReactTooltip border={true} effect="solid" id="createTooltip" place="top">
          {resourcesContext.messages['duplicatedUniqueConstraint']}
        </ReactTooltip>
      )}
    </Fragment>
  );

  const renderListBox = () => (
    <ListBox
      listStyle={{ height: '200px' }}
      onChange={event => {
        !isNil(event.value) && setSelectedTable(event.value);
        setSelectedFields([]);
      }}
      optionLabel="name"
      options={getTableOptions()}
      optionValue="value"
      title={resourcesContext.messages['selectUniqueTableTitle']}
      value={selectedTable}
    />
  );

  const renderListBoxField = () => (
    <ListBox
      disabled={isNil(selectedTable)}
      itemTemplate={pkTemplate}
      listStyle={{ height: '200px' }}
      multiple={true}
      onChange={event => !isNil(event.value) && setSelectedFields(event.value)}
      optionLabel="name"
      options={getFieldOptions()}
      optionValue="value"
      title={resourcesContext.messages['selectUniqueFieldsTitle']}
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
        <span className={styles.title}>{`${resourcesContext.messages['selectedTable']}: `}</span>
        <span>{!isNil(selectedTable.value) ? selectedTable.name : ''}</span>
      </div>
      <div className={`${styles.selected} ${styles.fields}`}>
        <span className={styles.title}>{`${resourcesContext.messages['selectedFields']}: `}</span>
        <span>{!isEmpty(selectedFields) ? selectedFields.map(field => field.name).join(', ') : ''}</span>
      </div>
    </Fragment>
  );
};
