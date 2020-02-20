import React, { useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './CodelistProperties.module.css';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CodelistProperties = ({
  categoriesDropdown,
  checkDuplicates,
  initialCategory,
  isCloning = false,
  isEmbedded = true,
  isIncorrect = false,
  onToggleIncorrect,
  onEditorPropertiesInputChange,
  onKeyChange,
  state,
  toggleCategoryChange
}) => {
  const [initialStatus, setInitialStatus] = useState();
  const resources = useContext(ResourcesContext);
  const statusTypes = [
    { statusType: 'Design', value: 'design' },
    { statusType: 'Ready', value: 'ready' },
    { statusType: 'Deprecated', value: 'deprecated' }
  ];

  useEffect(() => {
    setInitialStatus(
      !isCloning
        ? state.codelistStatus.value.toLocaleLowerCase()
        : state.clonedCodelist.codelistStatus.value.toLocaleLowerCase()
    );
  }, []);

  const getStatusValue = value => {
    if (!isUndefined(value.statusType)) {
      return statusTypes.filter(status => status.statusType.toUpperCase() === value.statusType.toUpperCase())[0];
    }
  };

  const getCategoryValue = value => {
    if (!isUndefined(value)) {
      return categoriesDropdown.filter(category => category.value === value)[0];
    }
  };

  return (
    <div className={styles.inputsWrapper}>
      {!isUndefined(categoriesDropdown) && ((isEmbedded && isCloning) || (!isEmbedded && state.isEditing)) ? (
        <div className={styles.codelistDropdown}>
          <label className={styles.codelistDropdownLabel}>{resources.messages['category']}</label>
          <Dropdown
            appendTo={document.body}
            className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
            disabled={initialStatus !== 'design'}
            onChange={e => {
              toggleCategoryChange(e.target.value.value !== initialCategory);
              onEditorPropertiesInputChange(e.target.value.value, 'codelistCategoryId');
            }}
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
          className={isIncorrect || state.codelistName.trim() === '' ? styles.codelistIncorrectInput : null}
          disabled={initialStatus !== 'design' ? true : !isEmbedded ? !state.isEditing : false}
          id="nameInput"
          onBlur={() =>
            onToggleIncorrect(
              checkDuplicates(
                !isCloning ? state.codelistName : state.clonedCodelist.codelistName,
                !isCloning ? state.codelistVersion : state.clonedCodelist.codelistVersion,
                !isCloning ? state.codelistId : state.clonedCodelist.codelistId,
                isCloning
              )
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
          className={isIncorrect || state.codelistVersion.trim() === '' ? styles.codelistIncorrectInput : null}
          disabled={initialStatus !== 'design' ? true : !isEmbedded ? !state.isEditing : false}
          id="versionInput"
          onBlur={() =>
            onToggleIncorrect(
              checkDuplicates(
                !isCloning ? state.codelistName : state.clonedCodelist.codelistName,
                !isCloning ? state.codelistVersion : state.clonedCodelist.codelistVersion,
                !isCloning ? state.codelistId : state.clonedCodelist.codelistId,
                isCloning
              )
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
          appendTo={document.body}
          className={!isEmbedded ? styles.dropdownFieldType : styles.dropdownFieldTypeDialog}
          disabled={!isEmbedded ? !state.isEditing : isUndefined(state.codelistId) ? true : false}
          onChange={e => onEditorPropertiesInputChange(e.target.value, 'codelistStatus')}
          optionLabel="statusType"
          options={
            initialStatus !== 'design'
              ? statusTypes.filter(status => status.statusType.toLocaleLowerCase() !== 'design')
              : statusTypes
          }
          // required={true}
          placeholder={resources.messages['codelistStatus']}
          value={getStatusValue(!isCloning ? state.codelistStatus : state.clonedCodelist.codelistStatus)}
        />
      </div>
      <span
        className={`${!isEmbedded ? styles.codelistInputTextarea : styles.codelistInputTextareaDialog} p-float-label`}>
        <InputTextarea
          collapsedHeight={40}
          disabled={initialStatus !== 'design' ? true : !isEmbedded ? !state.isEditing : false}
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
