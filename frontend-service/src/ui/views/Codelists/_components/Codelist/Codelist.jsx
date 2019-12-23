import React, { useContext, useEffect, useState } from 'react';

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

import styles from './Codelist.module.css';

const Codelist = ({ codelist, isDataCustodian = true }) => {
  const [initialItems, setInitialItems] = useState([]);
  const [items, setItems] = useState(codelist.items);
  const [isEditing, setIsEditing] = useState(false);
  const [isEditorVisible, setIsEditorVisible] = useState(false);

  const resources = useContext(ResourcesContext);

  const fieldTypes = [
    { fieldType: 'design', value: 'Design' },
    { fieldType: 'ready', value: 'Ready' },
    { fieldType: 'deprecated', value: 'Deprecated' }
  ];

  useEffect(() => {
    console.log(codelist.items);
    setInitialItems([...codelist.items]);
  }, []);

  useEffect(() => {
    console.log({ initialItems });
  }, [initialItems]);

  const onAddClick = () => {};

  const onCancelClick = () => {
    console.log({ initialItems });
    setItems([...initialItems]);
    setIsEditing(false);
  };

  const onEditClick = () => {
    setIsEditing(true);
  };

  const onEditorInputChange = (value, property) => {
    console.log({ value, property });
  };

  const onEditorItemsValueChange = (cells, value) => {
    let inmItems = [...cells.value];
    inmItems[cells.rowIndex][cells.field] = value;
    setItems(inmItems);
  };

  const cellDataEditor = (cells, field) => {
    return (
      <InputText
        // onBlur={e => onEditorSubmitValue(cells, e.target.value, field)}
        onChange={e => onEditorItemsValueChange(cells, e.target.value)}
        type="text"
        value={cells.rowData[field]}
      />
    );
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
          <InputText id="nameInput" onKeyDown={e => onEditorInputChange(e.target.value, 'name')} />
          <label htmlFor="nameInput">{resources.messages['codelistName']}</label>
        </span>
        <span className={`${styles.codelistInput} p-float-label`}>
          <InputText id="versionInput" onKeyDown={e => onEditorInputChange(e.target.value, 'version')} />
          <label htmlFor="versionInput">{resources.messages['codelistVersion']}</label>
        </span>
        <div className={styles.codelistInput}>
          <label>{resources.messages['codelistStatus']}</label>
          <Dropdown
            className={styles.dropdownFieldType}
            // onChange={e => onChangeFieldType(e.target.value)}
            optionLabel="fieldType"
            options={fieldTypes}
            required={true}
            // placeholder={resources.messages['codelistStatus']}
            // value={fieldTypeValue !== '' ? fieldTypeValue : getFieldTypeValue(fieldType)}
          />
        </div>
        <span className={`${styles.codelistInputTextarea} p-float-label`}>
          <InputTextarea
            collapsedHeight={40}
            expandableOnClick={true}
            id="descriptionInput"
            key="descriptionInput"
            onKeyDown={e => onEditorInputChange(e.target.value, 'description')}
            // onFocus={e => {
            //   setInitialTableDescription(e.target.value);
            // }}
            // onKeyDown={e => onKeyChange(e)}
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
        value={items}>
        <Column
          field="code"
          header="Code"
          sortable={true}
          editor={isEditing ? row => cellDataEditor(row, 'code') : null}
        />
        <Column
          field="label"
          header="Label"
          sortable={true}
          editor={isEditing ? row => cellDataEditor(row, 'label') : null}
        />
        <Column
          field="definition"
          header="Definition"
          sortable={true}
          editor={isEditing ? row => cellDataEditor(row, 'definition') : null}
        />
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
