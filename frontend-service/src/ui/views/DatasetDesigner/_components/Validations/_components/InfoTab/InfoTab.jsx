import React, { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './InfoTab.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
// import { Dropdown } from 'ui/views/_components/Dropdown';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';

export const InfoTab = ({
  componentName,
  creationFormState,
  onAddToClickedFields,
  onDeleteFromClickedFields,
  onInfoFieldChange,
  printError
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const validationContext = useContext(ValidationContext);

  const [fieldsDropdown, setFieldsDropdown] = useState();
  const [tableFieldOptions, setTableFieldOptions] = useState({
    disabled: true,
    placeholder: resourcesContext.messages.fieldConstraintTableFieldNoOptions
  });

  useEffect(() => {
    if (creationFormState.schemaTables.length > 0) {
      setTableFieldOptions({
        disabled: false,
        placeholder: resourcesContext.messages.table
      });
    } else {
      setTableFieldOptions({
        disabled: true,
        placeholder: resourcesContext.messages.fieldConstraintTableFieldNoOptions
      });
    }
  }, [creationFormState.schemaTables]);

  useEffect(() => {
    if (validationContext.level === 'field') {
      const { tableFields } = creationFormState;

      const fieldDropdownOptions = {
        disabled: true,
        placeholder: resourcesContext.messages.field,
        options: [],
        onChange: () => {},
        value: null
      };

      if (isNil(tableFields)) {
        fieldDropdownOptions.value = null;
      }

      if (!isNil(tableFields) && tableFields.length === 0) {
        fieldDropdownOptions.placeholder = resourcesContext.messages.designSchemaTabNoFields;
        fieldDropdownOptions.value = null;
      }

      if (!isNil(tableFields) && tableFields.length > 0) {
        console.log('creationFormState.candidateRule.field', creationFormState.candidateRule.field);
        fieldDropdownOptions.options = tableFields;
        fieldDropdownOptions.disabled = false;
        fieldDropdownOptions.onChange = e => onInfoFieldChange('field', e.value);
        fieldDropdownOptions.value = creationFormState.candidateRule.field;
      }

      setFieldsDropdown(
        <Dropdown
          id={`${componentName}__field`}
          disabled={fieldDropdownOptions.disabled}
          // appendTo={document.body}
          filterPlaceholder={fieldDropdownOptions.placeholder}
          placeholder={fieldDropdownOptions.placeholder}
          optionLabel="label"
          options={fieldDropdownOptions.options}
          onChange={fieldDropdownOptions.onChange}
          value={fieldDropdownOptions.value}
        />
      );
    }
  }, [
    creationFormState.candidateRule.field,
    creationFormState.candidateRule.table,
    creationFormState.tableFields,
    validationContext.isVisible
  ]);

  useEffect(() => {
    console.log('creationFormState.candidateRule.field,', creationFormState.candidateRule.field);
  }, [creationFormState.candidateRule.field]);

  return (
    <div className={styles.section}>
      <div className={styles.fieldsGroup}>
        <div
          onBlur={() => onAddToClickedFields('table')}
          onFocus={() => onDeleteFromClickedFields('table')}
          className={`${styles.field} ${styles.qcTable} formField ${printError('table')}`}>
          <label htmlFor="table">{resourcesContext.messages.table}</label>
          <Dropdown
            id={`${componentName}__table`}
            disabled={tableFieldOptions.disabled}
            // appendTo={document.body}
            filterPlaceholder={resourcesContext.messages.table}
            placeholder={tableFieldOptions.placeholder}
            optionLabel="label"
            options={creationFormState.schemaTables}
            value={creationFormState.candidateRule.table}
            onChange={e => onInfoFieldChange('table', e.value)}
          />
        </div>

        {validationContext.level === 'field' && (
          <div
            onBlur={() => onAddToClickedFields('field')}
            onFocus={() => onDeleteFromClickedFields('field')}
            className={`${styles.field} ${styles.qcField} formField ${printError('field')}`}>
            <label htmlFor="field">{resourcesContext.messages.field}</label>
            {fieldsDropdown}
          </div>
        )}

        <div
          onBlur={() => onAddToClickedFields('shortCode')}
          onFocus={() => onDeleteFromClickedFields('shortCode')}
          className={`${styles.field} ${styles.qcShortCode} formField ${printError('shortCode')}`}>
          <label htmlFor="shortCode">{resourcesContext.messages.ruleShortCode}</label>
          <InputText
            id={`${componentName}__shortCode`}
            placeholder={resourcesContext.messages.ruleShortCode}
            value={creationFormState.candidateRule.shortCode}
            onChange={e => onInfoFieldChange('shortCode', e.target.value)}
          />
        </div>

        <div className={`${styles.field} ${styles.qcEnabled} formField `}>
          <label htmlFor="QcActive">{resourcesContext.messages.qcEnabled}</label>
          <Checkbox
            id={`${componentName}__active`}
            onChange={e => onInfoFieldChange('active', e.checked)}
            isChecked={creationFormState.candidateRule.active}
          />
        </div>
      </div>

      <div className={styles.fieldsGroup}>
        <div
          onBlur={e => onAddToClickedFields('name')}
          onFocus={e => onDeleteFromClickedFields('name')}
          className={`${styles.field} ${styles.qcName} formField ${printError('name')}`}>
          <label htmlFor="name">{resourcesContext.messages.ruleName}</label>
          <InputText
            id={`${componentName}__name`}
            placeholder={resourcesContext.messages.ruleName}
            value={creationFormState.candidateRule.name}
            onChange={e => onInfoFieldChange('name', e.target.value)}
          />
        </div>

        <div className={`${styles.field} ${styles.qcDescription} formField`}>
          <label htmlFor="description">{resourcesContext.messages.description}</label>
          <InputText
            id={`${componentName}__description`}
            placeholder={resourcesContext.messages.description}
            value={creationFormState.candidateRule.description}
            onChange={e => onInfoFieldChange('description', e.target.value)}
          />
        </div>
      </div>

      <div className={styles.fieldsGroup}>
        <div
          onBlur={e => onAddToClickedFields('errorLevel')}
          onFocus={e => onDeleteFromClickedFields('errorLevel')}
          className={`${styles.field} ${styles.qcErrorType} formField ${printError('errorLevel')}`}>
          <label htmlFor="errorType">{resourcesContext.messages.errorType}</label>
          <Dropdown
            id={`${componentName}__errorType`}
            filterPlaceholder={resourcesContext.messages.errorTypePlaceholder}
            placeholder={resourcesContext.messages.errorTypePlaceholder}
            // appendTo={document.body}
            optionLabel="label"
            options={config.validations.errorLevels}
            onChange={e => onInfoFieldChange('errorLevel', e.target.value)}
            value={creationFormState.candidateRule.errorLevel}
          />
        </div>

        <div
          onBlur={e => onAddToClickedFields('errorMessage')}
          onFocus={e => onDeleteFromClickedFields('errorMessage')}
          className={`${styles.field} ${styles.qcErrorMessage} formField ${printError('errorMessage')}`}>
          <label htmlFor="errorMessage">{resourcesContext.messages.ruleErrorMessage}</label>
          <InputText
            id={`${componentName}__errorMessage`}
            placeholder={resourcesContext.messages.ruleErrorMessage}
            value={creationFormState.candidateRule.errorMessage}
            onChange={e => onInfoFieldChange('errorMessage', e.target.value)}
          />
        </div>
      </div>
    </div>
  );
};
