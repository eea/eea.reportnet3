import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './CodelistProperties.module.css';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistProperties = ({
  checkDuplicates,
  isCloning = false,
  isEmbedded = true,
  onEditorPropertiesInputChange,
  onKeyChange,
  state
}) => {
  const resources = useContext(ResourcesContext);

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
          onBlur={() =>
            checkDuplicates(
              !isCloning ? state.codelistName : state.clonedCodelist.codelistName,
              !isCloning ? state.codelistVersion : state.clonedCodelist.codelistVersion
            )
          }
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistName')}
          onKeyDown={e => onKeyChange(e, 'codelistName')}
          value={!isCloning ? state.codelistName : state.clonedCodelist.codelistName}
        />
        <label htmlFor="nameInput">{resources.messages['codelistName']}</label>
      </span>
      <span className={`${!isEmbedded ? styles.codelistInput : styles.codelistInputDialog} p-float-label`}>
        <InputText
          disabled={!isEmbedded ? !state.isEditing : false}
          id="versionInput"
          onBlur={() =>
            checkDuplicates(
              !isCloning ? state.codelistName : state.clonedCodelist.codelistName,
              !isCloning ? state.codelistVersion : state.clonedCodelist.codelistVersion
            )
          }
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistVersion')}
          onKeyDown={e => onKeyChange(e, 'codelistVersion')}
          value={!isCloning ? state.codelistVersion : state.clonedCodelist.codelistVersion}
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
          value={getStatusValue(!isCloning ? state.codelistStatus : state.clonedCodelist.codelistStatus)}
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
          value={!isCloning ? state.codelistDescription : state.clonedCodelist.codelistDescription}
        />
        <label htmlFor="descriptionInput">{resources.messages['codelistDescription']}</label>
      </span>
    </div>
  );
};

export { CodelistProperties };
