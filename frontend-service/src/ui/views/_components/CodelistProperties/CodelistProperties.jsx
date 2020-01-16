import React, { useContext } from 'react';

import { isUndefined, isNull } from 'lodash';

import styles from './CodelistProperties.module.css';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistProperties = ({
  checkDuplicates,
  categoriesDropdown,
  isCloning = false,
  isEmbedded = true,
  onEditorPropertiesInputChange,
  onKeyChange,
  state
}) => {
  const resources = useContext(ResourcesContext);
  console.log({ categoriesDropdown });
  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  const getStatusValue = value => {
    if (!isUndefined(value.statusType)) {
      return statusTypes.filter(status => status.statusType.toUpperCase() === value.statusType.toUpperCase())[0];
    }
  };

  const getCategoryValue = value => {
    console.log({ value, categoriesDropdown });
    if (!isUndefined(value)) {
      return categoriesDropdown.filter(category => category.value === value)[0];
    }
  };

  return (
    <div className={styles.inputsWrapper}>
      {console.log({ state, isEmbedded })}
      {!isUndefined(categoriesDropdown) && ((isEmbedded && isCloning) || (!isEmbedded && state.isEditing)) ? (
        <div className={styles.codelistDropdown}>
          <label className={styles.codelistDropdownLabel}>{resources.messages['category']}</label>
          <Dropdown
            className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
            onChange={e => onEditorPropertiesInputChange(e.target.value.value, 'codelistCategoryId')}
            optionLabel="categoryType"
            options={categoriesDropdown}
            // required={true}
            placeholder={resources.messages['category']}
            value={getCategoryValue(!isCloning ? state.codelistCategoryId : state.clonedCodelist.codelistCategoryId)}
          />
        </div>
      ) : null}
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
        <label className={styles.codelistDropdownLabel}>{resources.messages['codelistStatus']}</label>
        <Dropdown
          className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
          disabled={!isEmbedded ? !state.isEditing : isUndefined(state.codelistId) ? true : false}
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
