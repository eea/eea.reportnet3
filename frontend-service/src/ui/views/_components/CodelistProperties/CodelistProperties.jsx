import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './CodelistProperties.module.css';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistProperties = ({ state, isEmbedded, onEditorPropertiesInputChange, onKeyChange }) => {
  const resources = useContext(ResourcesContext);
  console.log({ state });
  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  const getStatusValue = value => {
    console.log({ value });
    if (!isUndefined(value.statusType)) {
      return statusTypes.filter(status => status.statusType.toUpperCase() === value.statusType.toUpperCase())[0];
    }
  };

  return (
    <div className={styles.inputsWrapper}>
      <span className={`${!isEmbedded ? styles.codelistInput : styles.codelistInputDialog} p-float-label`}>
        <InputText
          disabled={!isEmbedded ? !state.isEditing : false}
          id="nameInput"
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistName')}
          onKeyDown={e => onKeyChange(e, 'codelistName')}
          value={state.codelistName}
        />
        <label htmlFor="nameInput">{resources.messages['codelistName']}</label>
      </span>
      <span className={`${!isEmbedded ? styles.codelistInput : styles.codelistInputDialog} p-float-label`}>
        <InputText
          disabled={!isEmbedded ? !state.isEditing : false}
          id="versionInput"
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistVersion')}
          onKeyDown={e => onKeyChange(e, 'codelistVersion')}
          value={state.codelistVersion}
        />
        <label htmlFor="versionInput">{resources.messages['codelistVersion']}</label>
      </span>
      <div className={styles.codelistDropdown}>
        <label className={styles.codelistStatus}>{resources.messages['codelistStatus']}</label>
        <Dropdown
          className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
          disabled={!isEmbedded ? !state.isEditing : false}
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistStatus')}
          optionLabel="statusType"
          options={statusTypes}
          // required={true}
          placeholder={resources.messages['codelistStatus']}
          value={getStatusValue(state.codelistStatus)}
        />
      </div>
      <span
        className={`${!isEmbedded ? styles.codelistInputTextarea : styles.codelistInputTextareaDialog} p-float-label`}>
        <InputTextarea
          collapsedHeight={40}
          disabled={!isEmbedded ? !state.isEditing : false}
          expandableOnClick={true}
          id="descriptionInput"
          key="descriptionInput"
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistDescription')}
          onKeyDown={e => onKeyChange(e, 'codelistDescription')}
          style={{ marginBottom: isEmbedded ? '1.5rem' : 'auto' }}
          // onFocus={e => {
          //   setInitialTableDescription(e.target.value);
          // }}
          value={state.codelistDescription}
        />
        <label htmlFor="descriptionInput">{resources.messages['codelistDescription']}</label>
      </span>
    </div>
  );
};

export { CodelistProperties };
