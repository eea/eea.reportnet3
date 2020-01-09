import React, { useContext, useReducer } from 'react';

import { capitalize, isUndefined } from 'lodash';

import styles from './Codelist.module.css';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { CodelistForm } from './_components/CodelistForm/CodelistForm';
import { CodelistProperties } from 'ui/views/_components/CodelistProperties/CodelistProperties';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'ui/views/_components/InputText';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { codelistReducer } from './_functions/Reducers/codelistReducer';

import { CodelistUtils } from './_functions/Utils/CodelistUtils';

const Codelist = ({ checkDuplicates, codelist, isDataCustodian = true }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialCodelistState = {
    clonedCodelist: {
      codelistName: codelist.name,
      codelistVersion: codelist.version,
      codelistStatus: { statusType: 'design', value: 'DESIGN' },
      codelistDescription: codelist.description
    },
    codelistName: codelist.name,
    codelistVersion: codelist.version,
    codelistStatus: { statusType: codelist.status, value: codelist.status.toString().toLowerCase() },
    codelistDescription: codelist.description,
    editedItem: { itemId: '', code: '', label: '', definition: '' },
    formType: undefined,
    items: JSON.parse(JSON.stringify(codelist)).items,
    initialCellValue: undefined,
    initialItem: { itemId: '', code: '', label: '', definition: '' },
    isAddEditCodelistVisible: false,
    isDeleteCodelistItemVisible: false,
    isCloneCodelistVisible: false,
    isEditing: false,
    newItem: { itemId: `-${codelist.items.length}`, code: '', label: '', definition: '' },
    selectedItem: {}
  };

  const [codelistState, dispatchCodelist] = useReducer(codelistReducer, initialCodelistState);

  const onAddCodelistItemClick = () => {
    toggleDialogWithFormType('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', true, 'ADD');
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
    dispatchCodelist({
      type: formType === 'EDIT' ? 'SET_EDITED_CODELIST_ITEM' : 'SET_NEW_CODELIST_ITEM',
      payload: { property, value }
    });
  };

  const onSaveCloneCodelist = () => {
    try {
      //await CodelistService.cloneById(dataflowId, codelistId, codelist);
    } catch (error) {
      notificationContext.add({
        type: 'CLONE_CODELIST_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      dispatchCodelist({ type: 'RESET_INITIAL_CLONED_CODELIST' });
      toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', false);
    }
  };

  const onConfirmDeleteItem = async () => {
    try {
      //await CodelistService.deleteById(datasetId, records.selectedRecord.recordId);
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_CODELIST_ITEM_BY_ID_ERROR',
        content: {
          // dataflowId,
          // datasetId
        }
      });
    } finally {
      toggleDialog('TOGGLE_DELETE_CODELIST_ITEM_VISIBLE', false);
    }
  };

  const onEditorPropertiesInputChange = (value, property) => {
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property, value } });
  };

  const onEditorPropertiesClonedInputChange = (value, property) => {
    dispatchCodelist({ type: 'EDIT_CLONED_CODELIST_PROPERTIES', payload: { property, value } });
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
      toggleDialog('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', false);
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
      onDeleteClick={() => toggleDialog('TOGGLE_DELETE_CODELIST_ITEM_VISIBLE', true)}
      onEditClick={() => toggleDialogWithFormType('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', true, 'EDIT')}
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

  const cloneCodelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['save']} icon="save" onClick={() => onSaveCloneCodelist()} />
      <Button
        label={resources.messages['cancel']}
        icon="cancel"
        onClick={() => toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', false)}
      />
    </div>
  );

  const cloneCodelistForm = (
    <CodelistProperties
      checkDuplicates={checkDuplicates}
      isCloning={true}
      onEditorPropertiesInputChange={onEditorPropertiesClonedInputChange}
      onKeyChange={() => {}}
      state={codelistState}
    />
  );

  const renderButtons = () => (
    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
      <Button
        label={resources.messages['clone']}
        icon="clone"
        onClick={() => toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', true)}
      />
    </div>
  );

  const renderCloneCodelistDialog = () => {
    return codelistState.isCloneCodelistVisible ? (
      <Dialog
        className="edit-table"
        blockScroll={false}
        contentStyle={{ height: '80%', maxHeight: '80%', overflow: 'auto' }}
        closeOnEscape={false}
        footer={cloneCodelistDialogFooter}
        header={resources.messages['cloneCodelist']}
        modal={true}
        onHide={() => toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', false)}
        style={{ width: '60%' }}
        visible={codelistState.isCloneCodelistVisible}>
        <div className="p-grid p-fluid">{cloneCodelistForm}</div>
      </Dialog>
    ) : null;
  };

  const renderDeleteDialog = () => {
    return codelistState.isDeleteCodelistItemVisible ? (
      <ConfirmDialog
        onConfirm={onConfirmDeleteItem}
        onHide={() => toggleDialog('TOGGLE_DELETE_CODELIST_ITEM_VISIBLE', false)}
        visible={codelistState.isDeleteCodelistItemVisible}
        header={resources.messages['deleteRow']}
        labelConfirm={resources.messages['yes']}
        labelCancel={resources.messages['no']}>
        {resources.messages['confirmDeleteRow']}
      </ConfirmDialog>
    ) : null;
  };

  const renderEditItemsDialog = () => {
    return codelistState.isAddEditCodelistVisible ? (
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
        onHideDialog={() => toggleDialogWithFormType('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', true, '')}
        onSaveItem={onSaveItem}
        visible={codelistState.isAddEditCodelistVisible}
      />
    ) : null;
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
          onClick={() => toggleDialog('TOGGLE_EDITING_CODELIST_ITEM', true)}
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
      <CodelistProperties
        checkDuplicates={checkDuplicates}
        isEmbedded={false}
        onEditorPropertiesInputChange={onEditorPropertiesInputChange}
        onKeyChange={onKeyChange}
        state={codelistState}
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

  const toggleDialog = (action, isVisible) => {
    dispatchCodelist({
      type: action,
      payload: isVisible
    });
  };

  const toggleDialogWithFormType = (action, isVisible, formType) => {
    dispatchCodelist({
      type: action,
      payload: { visible: isVisible, formType }
    });
  };

  return (
    <React.Fragment>
      <TreeViewExpandableItem
        className={styles.codelistItem}
        expanded={false}
        items={[codelist.name, codelist.version, codelist.status, codelist.description]}>
        {renderButtons()}
        {renderInputs()}
        {renderTable()}
        {renderEditItemsDialog()}
        {renderDeleteDialog()}
        {renderCloneCodelistDialog()}
      </TreeViewExpandableItem>
    </React.Fragment>
  );
};

export { Codelist };
