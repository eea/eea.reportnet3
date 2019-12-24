import React, { useContext, useEffect, useState, useReducer } from 'react';

import { capitalize, isUndefined } from 'lodash';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { codelistReducer } from './_functions/Reducers/codelistReducer';

import styles from './Codelist.module.css';

const Codelist = ({ codelist, isDataCustodian = true }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [isEditorVisible, setIsEditorVisible] = useState(false);
  const [initialCellValue, setInitialCellValue] = useState();

  const resources = useContext(ResourcesContext);

  const initialCodelistState = {
    name: codelist.name,
    version: codelist.version,
    status: { statusType: codelist.status, value: codelist.status.toString().toLowerCase() },
    description: codelist.description,
    items: [...codelist.items]
  };
  console.log(codelist.items[0].label, codelist.version, initialCodelistState.items[0]);

  const [codelistState, dispatchCodelist] = useReducer(codelistReducer, initialCodelistState);

  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  const onAddClick = () => {};

  const onCancelClick = () => {
    console.log({ initialCodelistState });
    dispatchCodelist({
      type: 'RESET_INITIAL_VALUES',
      payload: initialCodelistState
    });
    setIsEditing(false);
  };

  const onEditClick = () => {
    setIsEditing(true);
  };

  const onEditorInputChange = (value, property) => {
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property, value } });
  };

  const onEditorItemsValueChange = (cells, value) => {
    let inmItems = [...cells.value];
    inmItems[cells.rowIndex][cells.field] = value;
    console.log(initialCodelistState.items[0], inmItems[0], value);
    dispatchCodelist({ type: 'EDIT_CODELIST_PROPERTIES', payload: { property: 'items', value: inmItems } });
  };

  const onKeyChange = (event, property) => {
    if (event.key === 'Escape') {
      console.log(event.target.value);
      console.log({ event, property, value: initialCodelistState[property] });
      dispatchCodelist({
        type: 'EDIT_CODELIST_PROPERTIES',
        payload: { property, value: initialCodelistState[property] }
      });
    } else if (event.key == 'Enter') {
    }
  };

  const cellDataEditor = (cells, field) => {
    return (
      <InputText
        // onBlur={e => onEditorSubmitValue(cells, e.target.value, field)}
        onFocus={e => {
          e.preventDefault();
          setInitialCellValue(e.target.value);
        }}
        onKeyDown={e => console.log(initialCellValue, initialCodelistState.items)}
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
        <Button label={resources.messages['add']} icon="add" onClick={() => onAddClick()} />
        <Button
          label={isEditing ? resources.messages['save'] : resources.messages['edit']}
          icon={isEditing ? 'save' : 'add'}
          onClick={() => onEditClick()}
          style={{ marginLeft: 'auto', marginRight: '0.5rem' }}
        />
        {isEditing ? (
          <Button
            className={`p-button-secondary`}
            label={resources.messages['cancel']}
            icon="cancel"
            onClick={() => onCancelClick()}
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
            disabled={!isEditing}
            id="nameInput"
            onChange={e => onEditorInputChange(e.target.value, 'name')}
            onKeyDown={e => onKeyChange(e, 'name')}
            value={codelistState.name}
          />
          <label htmlFor="nameInput">{resources.messages['codelistName']}</label>
        </span>
        <span className={`${styles.codelistInput} p-float-label`}>
          <InputText
            disabled={!isEditing}
            id="versionInput"
            onChange={e => onEditorInputChange(e.target.value, 'version')}
            onKeyDown={e => onKeyChange(e, 'version')}
            value={codelistState.version}
          />
          <label htmlFor="versionInput">{resources.messages['codelistVersion']}</label>
        </span>
        <div className={styles.codelistDropdown}>
          <label className={styles.codelistStatus}>{resources.messages['codelistStatus']}</label>
          <Dropdown
            className={styles.dropdownFieldType}
            disabled={!isEditing}
            onChange={e => onEditorInputChange(e.target.value, 'status')}
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
            disabled={!isEditing}
            expandableOnClick={true}
            id="descriptionInput"
            key="descriptionInput"
            onChange={e => onEditorInputChange(e.target.value, 'description')}
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

  const renderTable = () => {
    return (
      <DataTable
        autoLayout={true}
        className={styles.itemTable}
        editable={isEditing}
        footer={isDataCustodian ? renderFooter() : null}
        value={codelistState.items}>
        {['code', 'label', 'definition'].map(column => (
          <Column
            field={column}
            header={capitalize(column)}
            sortable={true}
            editor={isEditing ? row => cellDataEditor(row, column) : null}
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
      </TreeViewExpandableItem>
    </React.Fragment>
  );
};

export { Codelist };
