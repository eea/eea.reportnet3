import React, { useContext, useEffect, useState, useReducer } from 'react';

import { capitalize, isUndefined } from 'lodash';

import styles from './Codelist.module.css';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { CodelistForm } from './_components/CodelistForm/CodelistForm';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { codelistReducer } from './_functions/Reducers/codelistReducer';

import { CodelistUtils } from './_functions/Utils/CodelistUtils';

const Codelist = ({ codelist, isDataCustodian = true }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialCodelistState = {
    name: codelist.name,
    version: codelist.version,
    status: { statusType: codelist.status, value: codelist.status.toString().toLowerCase() },
    description: codelist.description,
    editedItem: { itemId: '', code: '', label: '', definition: '' },
    formType: undefined,
    items: JSON.parse(JSON.stringify(codelist)).items,
    initialCellValue: undefined,
    initialItem: { itemId: '', code: '', label: '', definition: '' },
    isAddEditCodelistVisible: false,
    isDeleteCodelistItemVisible: false,
    isEditing: false,
    newItem: { itemId: `-${codelist.items.length}`, code: '', label: '', definition: '' },
    selectedItem: {}
  };

  const [codelistState, dispatchCodelist] = useReducer(codelistReducer, initialCodelistState);

  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  const onAddCodelistItemClick = () => {
    dispatchCodelist({
      type: 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE',
      payload: { visible: true, formType: 'ADD' }
    });
  };

  const onCancelEditCodelistClick = () => {
    dispatchCodelist({
      type: 'RESET_INITIAL_VALUES',
      payload: initialCodelistState
    });
  };

  const onCancelAddEditItem = formType => {
    if (formType !== 'EDIT') {
      dispatchCodelist({
        type: 'RESET_INITIAL_NEW_ITEM'
      });
    } else {
    }
  };

  const onChangeItemForm = (property, value, formType) => {
    console.log({ property, value, formType });
    console.log(codelistState.newItem);
    dispatchCodelist({
      type: formType === 'EDIT' ? 'SET_EDITED_CODELIST_ITEM' : 'SET_NEW_CODELIST_ITEM',
      payload: { property, value }
    });
  };

  const onConfirmDeleteItem = async () => {
    try {
      //await DatasetService.deleteRecordById(datasetId, records.selectedRecord.recordId);
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_CODELIST_ITEM_BY_ID_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      dispatchCodelist({
        type: 'TOGGLE_DELETE_CODELIST_ITEM_VISIBLE',
        payload: false
      });
    }
  };

  const onEditCodelistClick = () => {
    dispatchCodelist({
      type: 'TOGGLE_EDITING_CODELIST_ITEM',
      payload: true
    });
  };

  const onEditorPropertiesInputChange = (value, property) => {
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property, value } });
  };

  const onEditorItemsValueChange = (cells, value) => {
    const inmItems = [...cells.value];
    inmItems[cells.rowIndex][cells.field] = value;
    console.log(initialCodelistState.items[0], inmItems[0], value);
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property: 'items', value: inmItems } });
  };

  const onFormLoaded = () => {
    dispatchCodelist({ type: 'SET_INITIAL_EDITED_CODELIST_ITEM', payload: {} });
  };

  const onKeyChange = (event, property, isItem) => {
    if (event.key === 'Escape') {
      console.log(event.target.value);
      console.log({ event, property, value: initialCodelistState[property] });
      if (isItem) {
      } else {
        dispatchCodelist({
          type: 'EDIT_CODELIST_PROPERTIES',
          payload: { property, value: initialCodelistState[property] }
        });
      }
    } else if (event.key == 'Enter') {
    }
  };

  const onSaveItem = formType => {
    try {
      console.log({ formType });
      const inmItems = [...codelistState.items];
      if (formType === 'ADD') {
        inmItems.push(codelistState.newItem);
      } else {
      }
      console.log({ inmItems });
      dispatchCodelist({ type: 'SAVE_ADDED_EDITED_ITEM', payload: inmItems });
    } catch (error) {
      notificationContext.add({
        type: 'SAVE_CODELIST_ITEM_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      dispatchCodelist({
        type: 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE',
        payload: false
      });
      dispatchCodelist({
        type: 'RESET_INITIAL_NEW_ITEM'
      });
    }
  };

  const onSelectItem = val => {
    dispatchCodelist({ type: 'SET_SELECTED_ITEM', payload: { ...val } });
  };

  const actionTemplate = () => (
    <ActionsColumn
      onDeleteClick={() => {
        dispatchCodelist({
          type: 'TOGGLE_DELETE_CODELIST_ITEM_VISIBLE',
          payload: true
        });
      }}
      onEditClick={() => {
        dispatchCodelist({
          type: 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE',
          payload: { visible: true, formType: 'EDIT' }
        });
      }}
    />
  );

  const cellItemDataEditor = (cells, field) => {
    return (
      <InputText
        // onBlur={e => onEditorSubmitValue(cells, e.target.value, field)}
        onFocus={e => {
          e.preventDefault();
          dispatchCodelist({
            type: 'SAVE_INITIAL_CELL_VALUE',
            payload: e.target.value
          });
        }}
        onKeyDown={e => onKeyChange(e, field)}
        onChange={e => onEditorItemsValueChange(cells, e.target.value)}
        type="text"
        value={cells.rowData[field]}
      />
    );
  };

  const getStatusValue = value => {
    if (!isUndefined(value.statusType)) {
      return statusTypes.filter(status => status.statusType.toUpperCase() === value.statusType.toUpperCase())[0];
    }
  };

  const renderFooter = () => {
    return (
      <div className={styles.footerWrap} style={{ width: '100%' }}>
        {codelistState.isEditing ? (
          <Button label={resources.messages['add']} icon="add" onClick={() => onAddCodelistItemClick()} />
        ) : null}
        <Button
          label={codelistState.isEditing ? resources.messages['save'] : resources.messages['edit']}
          icon={codelistState.isEditing ? 'save' : 'pencil'}
          onClick={() => onEditCodelistClick()}
          style={{ marginLeft: 'auto', marginRight: '0.5rem' }}
        />
        {codelistState.isEditing ? (
          <Button
            className={`p-button-secondary`}
            label={resources.messages['cancel']}
            icon="cancel"
            onClick={() => onCancelEditCodelistClick()}
          />
        ) : null}
      </div>
    );
  };

  const renderInputs = () => {
    return (
      <div className={styles.inputsWrapper}>
        <span className={`${styles.codelistInput} p-float-label`}>
          <InputText
            disabled={!codelistState.isEditing}
            id="nameInput"
            onChange={e => onEditorPropertiesInputChange(e.target.value, 'name')}
            onKeyDown={e => onKeyChange(e, 'name')}
            value={codelistState.name}
          />
          <label htmlFor="nameInput">{resources.messages['codelistName']}</label>
        </span>
        <span className={`${styles.codelistInput} p-float-label`}>
          <InputText
            disabled={!codelistState.isEditing}
            id="versionInput"
            onChange={e => onEditorPropertiesInputChange(e.target.value, 'version')}
            onKeyDown={e => onKeyChange(e, 'version')}
            value={codelistState.version}
          />
          <label htmlFor="versionInput">{resources.messages['codelistVersion']}</label>
        </span>
        <div className={styles.codelistDropdown}>
          <label className={styles.codelistStatus}>{resources.messages['codelistStatus']}</label>
          <Dropdown
            className={styles.dropdownFieldType}
            disabled={!codelistState.isEditing}
            onChange={e => onEditorPropertiesInputChange(e.target.value, 'status')}
            optionLabel="statusType"
            options={statusTypes}
            // required={true}
            placeholder={resources.messages['codelistStatus']}
            value={getStatusValue(codelistState.status)}
          />
        </div>
        <span className={`${styles.codelistInputTextarea} p-float-label`}>
          <InputTextarea
            collapsedHeight={40}
            disabled={!codelistState.isEditing}
            expandableOnClick={true}
            id="descriptionInput"
            key="descriptionInput"
            onChange={e => onEditorPropertiesInputChange(e.target.value, 'description')}
            onKeyDown={e => onKeyChange(e, 'description')}
            // onFocus={e => {
            //   setInitialTableDescription(e.target.value);
            // }}
            value={codelistState.description}
          />
          <label htmlFor="descriptionInput">{resources.messages['codelistDescription']}</label>
        </span>
      </div>
    );
  };

  const renderDeleteDialog = () => {
    return (
      <ConfirmDialog
        onConfirm={onConfirmDeleteItem}
        onHide={() =>
          dispatchCodelist({
            type: 'TOGGLE_DELETE_CODELIST_ITEM_VISIBLE',
            payload: false
          })
        }
        visible={codelistState.isDeleteCodelistItemVisible}
        header={resources.messages['deleteRow']}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {resources.messages['confirmDeleteRow']}
      </ConfirmDialog>
    );
  };

  const renderEditDialog = () => {
    console.log(codelistState.editedItem);
    return (
      <CodelistForm
        columns={['code', 'label', 'definition']}
        formType={codelistState.formType}
        item={
          codelistState.formType === 'EDIT'
            ? codelistState.editedItem.itemId === ''
              ? CodelistUtils.getItem(codelistState.items, codelistState.selectedItem)
              : codelistState.editedItem
            : codelistState.newItem
        }
        onCancelAddEditItem={onCancelAddEditItem}
        onChangeItemForm={onChangeItemForm}
        onFormLoaded={onFormLoaded}
        onHideDialog={() => {
          dispatchCodelist({
            type: 'TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE',
            payload: { visible: false, formType: '' }
          });
        }}
        onSaveItem={onSaveItem}
        visible={codelistState.isAddEditCodelistVisible}
      />
    );
  };

  const renderTable = () => {
    return (
      <DataTable
        autoLayout={true}
        className={styles.itemTable}
        editable={codelistState.isEditing}
        footer={isDataCustodian ? renderFooter() : null}
        onRowSelect={e => onSelectItem(e.data)}
        selectionMode="single"
        value={codelistState.items}>
        {['itemId', 'code', 'label', 'definition'].map(column => (
          <Column
            editor={codelistState.isEditing ? row => cellItemDataEditor(row, column) : null}
            field={column}
            header={capitalize(column)}
            sortable={true}
            style={{ display: column === 'itemId' ? 'none' : 'auto' }}
          />
        ))}
        {codelistState.isEditing ? (
          <Column
            header={resources.messages['actions']}
            key="actions"
            body={row => actionTemplate(row)}
            sortable={false}
            style={{ width: '100px' }}
          />
        ) : null}
      </DataTable>
    );
  };

  return (
    <React.Fragment>
      <TreeViewExpandableItem
        className={styles.codelistItem}
        expanded={false}
        items={[codelist.name, codelist.version, codelist.status, codelist.description]}>
        {renderInputs()}
        {renderTable()}
        {codelistState.isAddEditCodelistVisible ? renderEditDialog() : null}
        {codelistState.isDeleteCodelistItemVisible ? renderDeleteDialog() : null}
      </TreeViewExpandableItem>
    </React.Fragment>
  );
};

export { Codelist };
