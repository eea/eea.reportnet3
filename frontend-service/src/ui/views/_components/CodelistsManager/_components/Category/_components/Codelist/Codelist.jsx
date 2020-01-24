import React, { useContext, useReducer } from 'react';

import { capitalize, isUndefined, cloneDeep } from 'lodash';

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

import { CodelistService } from 'core/services/Codelist';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { codelistReducer } from './_functions/Reducers/codelistReducer';

import { CodelistUtils } from './_functions/Utils/CodelistUtils';

const Codelist = ({
  categoriesDropdown,
  categoryId,
  checkDuplicates,
  codelist,
  isDataCustodian = true,
  isInDesign,
  onCodelistError,
  onCodelistSelected,
  onRefreshCodelist,
  updateEditingCodelists
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const initialCodelistState = {
    clonedCodelist: {
      codelistId: codelist.id,
      codelistCategoryId: categoryId,
      codelistName: codelist.name,
      codelistVersion: codelist.version,
      codelistStatus: { statusType: 'design', value: 'DESIGN' },
      codelistDescription: codelist.description
    },
    codelistId: codelist.id,
    codelistCategoryId: categoryId,
    codelistName: codelist.name,
    codelistVersion: codelist.version,
    codelistStatus: { statusType: codelist.status, value: codelist.status.toString().toLowerCase() },
    codelistDescription: codelist.description,
    editedItem: { id: '', shortCode: '', label: '', definition: '', codelistId: '' },
    formType: undefined,
    items: JSON.parse(JSON.stringify(codelist)).items,
    initialCellValue: undefined,
    initialItem: { id: '', shortCode: '', label: '', definition: '', codelistId: '' },
    isAddEditCodelistVisible: false,
    isDeleteCodelistItemVisible: false,
    isCloneCodelistVisible: false,
    isEditing: false,
    newItem: { id: `-${codelist.items.length}`, shortCode: '', label: '', definition: '', codelistId: '' },
    selectedItem: {}
  };
  const [codelistState, dispatchCodelist] = useReducer(codelistReducer, initialCodelistState);

  React.useEffect(() => {
    dispatchCodelist({
      type: 'RESET_INITIAL_VALUES',
      payload: initialCodelistState
    });
  }, [codelist]);

  const onAddCodelistItemClick = () => {
    toggleDialogWithFormType('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', true, 'ADD');
  };

  const onCancelEditCodelistClick = () => {
    dispatchCodelist({
      type: 'RESET_INITIAL_VALUES',
      payload: initialCodelistState
    });
  };

  const onCancelAddEditItem = () => {
    if (codelistState.formType !== 'EDIT') {
      dispatchCodelist({
        type: 'RESET_INITIAL_NEW_ITEM'
      });
    } else {
    }
    toggleDialog('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', false);
  };

  const onChangeItemForm = (property, value, formType) => {
    dispatchCodelist({
      type: formType === 'EDIT' ? 'SET_EDITED_CODELIST_ITEM' : 'SET_NEW_CODELIST_ITEM',
      payload: { property, value }
    });
  };

  const onConfirmDeleteItem = () => {
    try {
      const inmItems = [...codelistState.items];
      dispatchCodelist({
        type: 'SET_ITEMS',
        payload: inmItems.filter(item => item.id !== codelistState.selectedItem.id)
      });
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_CODELIST_ITEM_BY_ID_ERROR'
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
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property: 'items', value: inmItems } });
  };

  const onFormLoaded = () => dispatchCodelist({ type: 'SET_INITIAL_EDITED_CODELIST_ITEM', payload: {} });

  const onKeyChange = (event, property, isItem) => {
    if (event.key === 'Escape') {
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

  const onLoadCodelist = async () => {
    try {
      const response = await CodelistService.getById(codelistState.codelistId);
      dispatchCodelist({ type: 'SET_CODELIST_DATA', payload: response });
      onRefreshCodelist(codelistState.codelistId, response);
    } catch (error) {
      notificationContext.add({
        type: 'CODELIST_SERVICE_GET_BY_ID_ERROR'
      });
    } finally {
      dispatchCodelist({ type: 'RESET_INITIAL_CLONED_CODELIST' });
      toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', false);
    }
  };

  const onSaveCloneCodelist = async () => {
    try {
      await CodelistService.cloneById(
        codelistState.codelistId,
        codelistState.clonedCodelist.codelistDescription,
        codelistState.items,
        codelistState.clonedCodelist.codelistName,
        codelistState.clonedCodelist.codelistVersion,
        codelistState.clonedCodelist.codelistCategoryId
      );
    } catch (error) {
      notificationContext.add({
        type: 'CODELIST_SERVICE_CLONE_BY_ID_ERROR'
      });
    } finally {
      dispatchCodelist({ type: 'RESET_INITIAL_CLONED_CODELIST' });
      toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', false);
    }
  };

  const onSaveCodelist = async () => {
    if (codelistState.codelistStatus.value.toUpperCase() === 'READY' && codelistState.items.length === 0) {
      if (!isUndefined(onCodelistError)) {
        onEditorPropertiesInputChange({ statusType: 'design', value: 'DESIGN' }, 'codelistStatus');
        onCodelistError(resources.messages['noItemsInCodelistTitle'], resources.messages['noItemsInCodelistMessage']);
      }
    } else {
      try {
        const response = await CodelistService.updateById(
          codelistState.codelistId,
          codelistState.codelistDescription,
          codelistState.items,
          codelistState.codelistName,
          codelistState.codelistStatus.value.toUpperCase(),
          codelistState.codelistVersion,
          codelistState.codelistCategoryId
        );
        if (response.status >= 200 && response.status <= 299) {
          toggleDialog('TOGGLE_EDITING_CODELIST_ITEM', false);
        }
      } catch (error) {
        notificationContext.add({
          type: 'CODELIST_SERVICE_UPDATE_BY_ID_ERROR'
        });
      } finally {
        if (!isUndefined(updateEditingCodelists)) {
          updateEditingCodelists(false);
        }
        onLoadCodelist();
      }
    }
  };

  const onSaveItem = formType => {
    try {
      const inmItems = [...codelistState.items];
      if (formType === 'ADD') {
        inmItems.push(codelistState.newItem);
        inmItems.forEach((item, i) => {
          item.id = `-${i}`;
        });
      } else {
      }
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
      categoriesDropdown={categoriesDropdown}
      checkDuplicates={checkDuplicates}
      isCloning={true}
      onEditorPropertiesInputChange={onEditorPropertiesClonedInputChange}
      onKeyChange={() => {}}
      state={cloneDeep(codelistState)}
    />
  );

  const getStatusStyle = status => {
    if (status.toLowerCase() === 'design') return styles.designBox;
    if (status.toLowerCase() === 'deprecated') return styles.deprecatedBox;
    if (status.toLowerCase() === 'ready') return styles.readyBox;
  };

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
        columns={['shortCode', 'label', 'definition']}
        formType={codelistState.formType}
        item={
          codelistState.formType === 'EDIT'
            ? codelistState.editedItem.id === ''
              ? CodelistUtils.getItem(codelistState.items, codelistState.selectedItem)
              : codelistState.editedItem
            : codelistState.newItem
        }
        onCancelAddEditItem={onCancelAddEditItem}
        onChangeItemForm={onChangeItemForm}
        onFormLoaded={onFormLoaded}
        onHideDialog={() => toggleDialogWithFormType('TOGGLE_ADD_EDIT_CODELIST_ITEM_VISIBLE', false, '')}
        onSaveItem={onSaveItem}
        visible={codelistState.isAddEditCodelistVisible}
      />
    ) : null;
  };

  const renderFooter = () => {
    return (
      <div className={styles.footerWrap} style={{ width: '100%' }}>
        {codelistState.isEditing && codelistState.codelistStatus.value.toLowerCase() === 'design' ? (
          <Button label={resources.messages['add']} icon="add" onClick={() => onAddCodelistItemClick()} />
        ) : null}
        <Button
          label={codelistState.isEditing ? resources.messages['save'] : resources.messages['edit']}
          icon={codelistState.isEditing ? 'save' : 'pencil'}
          onClick={
            !codelistState.isEditing
              ? () => {
                  toggleDialog('TOGGLE_EDITING_CODELIST_ITEM', true);
                  if (!isUndefined(updateEditingCodelists)) {
                    updateEditingCodelists(true);
                  }
                }
              : onSaveCodelist
          }
          style={{ marginLeft: 'auto', marginRight: '0.5rem' }}
        />
        {codelistState.isEditing ? (
          <Button
            className={`p-button-danger`}
            label={resources.messages['cancel']}
            icon="cancel"
            onClick={() => {
              onCancelEditCodelistClick();
              if (!isUndefined(updateEditingCodelists)) {
                updateEditingCodelists(false);
              }
            }}
          />
        ) : null}
      </div>
    );
  };

  const renderInputs = () => {
    return (
      <CodelistProperties
        categoriesDropdown={categoriesDropdown}
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
        editable={codelistState.isEditing && codelistState.codelistStatus.value.toLowerCase() === 'design'}
        footer={isDataCustodian ? renderFooter() : null}
        onRowSelect={e => onSelectItem(e.data)}
        selectionMode="single"
        value={codelistState.items}>
        {['id', 'shortCode', 'label', 'definition'].map((column, i) => (
          <Column
            editor={
              codelistState.isEditing && codelistState.codelistStatus.value.toLowerCase() === 'design'
                ? row => cellItemDataEditor(row, column)
                : null
            }
            field={column}
            header={column === 'shortCode' ? resources.messages['categoryShortCode'] : capitalize(column)}
            key={i}
            sortable={true}
            style={{ display: column === 'id' ? 'none' : 'auto' }}
          />
        ))}
        {codelistState.isEditing && codelistState.codelistStatus.value.toLowerCase() === 'design' ? (
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
        buttons={[
          {
            icon: 'clone',
            disabled: codelistState.isEditing,
            onClick: () => toggleDialog('TOGGLE_CLONE_CODELIST_DIALOG_VISIBLE', true),
            tooltip: resources.messages['clone']
          },
          {
            disabled: codelist.status.toLowerCase() !== 'ready',
            icon: 'checkSquare',
            onClick: () =>
              onCodelistSelected(
                codelistState.codelistId,
                codelistState.codelistName,
                codelistState.codelistVersion,
                codelistState.items
              ),
            tooltip: resources.messages['selectCodelist'],
            visible: isInDesign
          }
        ]}
        className={`${styles.codelistItem} ${styles.codelistExpandable}`}
        expanded={false}
        infoButtons={
          codelistState.isEditing
            ? [
                {
                  icon: 'save',
                  className: 'p-button-danger',
                  tooltip: resources.messages['unsavedChanges']
                }
              ]
            : undefined
        }
        items={[
          { label: codelistState.codelistName },
          { label: codelistState.codelistVersion },
          {
            className: [getStatusStyle(codelistState.codelistStatus.value), styles.statusBox].join(' '),
            type: 'box',
            label: capitalize(codelistState.codelistStatus.value)
          },
          { label: codelistState.codelistDescription }
        ]}
        onExpandTree={() =>
          dispatchCodelist({
            type: 'RESET_INITIAL_VALUES',
            payload: initialCodelistState
          })
        }>
        {renderInputs()}
        {renderTable()}
        {renderEditItemsDialog()}
        {renderDeleteDialog()}
      </TreeViewExpandableItem>
      {renderCloneCodelistDialog()}
    </React.Fragment>
  );
};

export { Codelist };
