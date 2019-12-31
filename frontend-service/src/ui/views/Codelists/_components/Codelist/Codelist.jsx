import React, { useContext, useEffect, useState, useReducer } from 'react';

import { capitalize, isUndefined } from 'lodash';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { CodelistForm } from './_components/CodelistForm/CodelistForm';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { codelistReducer } from './_functions/Reducers/codelistReducer';

import styles from './Codelist.module.css';

const Codelist = ({ codelist, isDataCustodian = true }) => {
  const resources = useContext(ResourcesContext);

  const initialCodelistState = {
    name: codelist.name,
    version: codelist.version,
    status: { statusType: codelist.status, value: codelist.status.toString().toLowerCase() },
    description: codelist.description,
    formType: undefined,
    items: JSON.parse(JSON.stringify(codelist)).items,
    initialCellValue: undefined,
    initialItem: { code: '', label: '', definition: '' },
    isEditing: false,
    isNewCodelistVisible: false,
    newItem: { code: '', label: '', definition: '' }
  };

  const [codelistState, dispatchCodelist] = useReducer(codelistReducer, initialCodelistState);

  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  const onAddCodelistItemClick = () => {
    dispatchCodelist({
      type: 'TOGGLE_NEW_CODELIST_VISIBLE',
      payload: true
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
    dispatchCodelist({
      type: formType === 'EDIT' ? 'SET_EDITED_CODELIST_ITEM' : 'SET_NEW_CODELIST_ITEM',
      payload: { property, value }
    });
  };

  const onEditCodelistClick = () => {
    dispatchCodelist({
      type: 'TOGGLE_EDITING_CODELIST',
      payload: true
    });
  };

  const onEditorPropertiesInputChange = (value, property) => {
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property, value } });
  };

  const onEditorItemsValueChange = (cells, value) => {
    let inmItems = [...cells.value];
    inmItems[cells.rowIndex][cells.field] = value;
    console.log(initialCodelistState.items[0], inmItems[0], value);
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property: 'items', value: inmItems } });
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

  const onSaveItem = () => {
    //API CALL
    //Meanwhile...
    const inmItems = [...codelistState.items];
    inmItems.push(codelistState.newItem);
    console.log({ inmItems });
    dispatchCodelist({ type: 'SAVE_NEW_ITEM', payload: inmItems });
  };

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
          icon={codelistState.isEditing ? 'save' : 'add'}
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

  const renderEditDialog = formType => {
    return (
      <CodelistForm
        columns={['code', 'label', 'definition']}
        formType={formType}
        items={codelistState.items}
        onCancelAddEditItem={onCancelAddEditItem}
        onChangeItemForm={onChangeItemForm}
        onHideDialog={() => {
          dispatchCodelist({
            type: 'TOGGLE_NEW_CODELIST_VISIBLE',
            payload: false
          });
        }}
        onSaveItem={onSaveItem}
        visible={codelistState.isNewCodelistVisible}
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
        value={codelistState.items}>
        {['code', 'label', 'definition'].map(column => (
          <Column
            field={column}
            header={capitalize(column)}
            sortable={true}
            editor={codelistState.isEditing ? row => cellItemDataEditor(row, column) : null}
          />
        ))}
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
        {codelistState.isNewCodelistVisible ? renderEditDialog('ADD') : null}
      </TreeViewExpandableItem>
    </React.Fragment>
  );
};

export { Codelist };
