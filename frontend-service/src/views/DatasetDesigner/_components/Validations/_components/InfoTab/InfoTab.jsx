import { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './InfoTab.module.scss';

import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'views/_functions/Contexts/ValidationContext';

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
    placeholder: resourcesContext.messages['fieldConstraintTableFieldNoOptions']
  });

  useEffect(() => {
    if (creationFormState.schemaTables.length > 0) {
      setTableFieldOptions({
        disabled: false,
        placeholder: resourcesContext.messages['table']
      });
    } else {
      setTableFieldOptions({
        disabled: true,
        placeholder: resourcesContext.messages['fieldConstraintTableFieldNoOptions']
      });
    }
  }, [creationFormState.schemaTables]);

  useEffect(() => {
    if (validationContext.level === 'field') {
      const { tableSqlFields } = creationFormState;

      const fieldDropdownOptions = {
        disabled: true,
        placeholder: resourcesContext.messages['field'],
        options: [],
        onChange: () => {},
        value: null
      };

      if (isNil(tableSqlFields)) {
        fieldDropdownOptions.value = null;
      }

      if (!isNil(tableSqlFields) && tableSqlFields.length === 0) {
        fieldDropdownOptions.placeholder = resourcesContext.messages['designSchemaTabNoFields'];
        fieldDropdownOptions.value = null;
      }

      if (!isNil(tableSqlFields) && tableSqlFields.length > 0) {
        fieldDropdownOptions.options = tableSqlFields;
        fieldDropdownOptions.disabled = false;
        fieldDropdownOptions.onChange = e => onInfoFieldChange('field', e.value);
        fieldDropdownOptions.value = creationFormState.candidateRule.field;
      }

      setFieldsDropdown(
        <Dropdown
          appendTo={document.body}
          disabled={
            creationFormState.candidateRule.automatic || validationContext.ruleEdit
              ? true
              : fieldDropdownOptions.disabled
          }
          filterPlaceholder={fieldDropdownOptions.placeholder}
          id={`${componentName}__field`}
          onChange={fieldDropdownOptions.onChange}
          optionLabel="label"
          options={fieldDropdownOptions.options}
          placeholder={fieldDropdownOptions.placeholder}
          value={fieldDropdownOptions.value}
        />
      );
    }
  }, [
    creationFormState.candidateRule.field,
    creationFormState.candidateRule.table,
    creationFormState.tableSqlFields,
    validationContext.isVisible
  ]);

  return (
    <div className={styles.section}>
      <div className={styles.fieldsGroup}>
        <div
          className={`${styles.field} ${styles.qcTable} formField ${printError('table')}`}
          onBlur={() => onAddToClickedFields('table')}
          onFocus={() => onDeleteFromClickedFields('table')}>
          <label htmlFor="table">{resourcesContext.messages['table']}</label>
          <Dropdown
            appendTo={document.body}
            disabled={
              creationFormState.candidateRule.automatic || validationContext.ruleEdit
                ? true
                : tableFieldOptions.disabled
            }
            filterPlaceholder={resourcesContext.messages['table']}
            id={`${componentName}__table`}
            onChange={e => onInfoFieldChange('table', e.value)}
            optionLabel="label"
            options={creationFormState.schemaTables}
            placeholder={tableFieldOptions.placeholder}
            value={creationFormState.candidateRule.table || ''}
          />
        </div>

        {validationContext.level === 'field' && (
          <div
            className={`${styles.field} ${styles.qcField} formField ${printError('field')}`}
            onBlur={() => onAddToClickedFields('field')}
            onFocus={() => onDeleteFromClickedFields('field')}>
            <label htmlFor="field">{resourcesContext.messages['field']}</label>
            {fieldsDropdown}
          </div>
        )}

        <div
          className={`${styles.field} ${styles.qcShortCode} formField ${printError('shortCode')}`}
          onBlur={() => onAddToClickedFields('shortCode')}
          onFocus={() => onDeleteFromClickedFields('shortCode')}>
          <label htmlFor={`${componentName}__shortCode`}>
            {resourcesContext.messages['ruleShortCode']}
            <TooltipButton message={resourcesContext.messages['noQuotesQCTooltip']} uniqueIdentifier="shortCode" />
          </label>
          <InputText
            id={`${componentName}__shortCode`}
            keyfilter="noDoubleQuote"
            maxLength={config.INPUT_MAX_LENGTH}
            onChange={e => onInfoFieldChange('shortCode', e.target.value)}
            placeholder={resourcesContext.messages['ruleShortCode']}
            value={creationFormState.candidateRule.shortCode || ''}
          />
        </div>

        <div className={`${styles.field} ${styles.qcEnabled} formField `}>
          <label htmlFor="QcActive">{resourcesContext.messages['qcEnabled']}</label>
          <Checkbox
            checked={creationFormState.candidateRule.active}
            id={`${componentName}__active`}
            onChange={e => onInfoFieldChange('active', e.checked)}
          />
        </div>
      </div>

      <div className={styles.fieldsGroup}>
        <div
          className={`${styles.field} ${styles.qcName} formField ${printError('name')}`}
          onBlur={() => onAddToClickedFields('name')}
          onFocus={() => onDeleteFromClickedFields('name')}>
          <label htmlFor={`${componentName}__name`}>{resourcesContext.messages['ruleName']}</label>
          <InputText
            id={`${componentName}__name`}
            maxLength={config.INPUT_MAX_LENGTH}
            onChange={e => onInfoFieldChange('name', e.target.value)}
            placeholder={resourcesContext.messages['ruleName']}
            value={creationFormState.candidateRule.name || ''}
          />
        </div>

        <div className={`${styles.field} ${styles.qcDescription} formField`}>
          <label htmlFor={`${componentName}__description`}>{resourcesContext.messages['description']}</label>
          <InputText
            id={`${componentName}__description`}
            maxLength={config.INPUT_MAX_LENGTH}
            onChange={e => onInfoFieldChange('description', e.target.value)}
            placeholder={resourcesContext.messages['description']}
            value={creationFormState.candidateRule.description}
          />
        </div>
      </div>

      <div className={styles.fieldsGroup}>
        <div
          className={`${styles.field} ${styles.qcErrorType} formField ${printError('errorLevel')}`}
          onBlur={() => onAddToClickedFields('errorLevel')}
          onFocus={() => onDeleteFromClickedFields('errorLevel')}>
          <label htmlFor="errorType">{resourcesContext.messages['errorType']}</label>
          <Dropdown
            appendTo={document.body}
            filterPlaceholder={resourcesContext.messages['errorTypePlaceholder']}
            id={`${componentName}__errorType`}
            onChange={e => onInfoFieldChange('errorLevel', e.target.value)}
            optionLabel="label"
            optionValue="value"
            options={config.validations.errorLevels}
            placeholder={resourcesContext.messages['errorTypePlaceholder']}
            value={creationFormState.candidateRule.errorLevel}
          />
        </div>
        <div
          className={`${styles.field} ${styles.qcErrorMessage} formField ${printError('errorMessage')}`}
          onBlur={() => onAddToClickedFields('errorMessage')}
          onFocus={() => onDeleteFromClickedFields('errorMessage')}>
          <label htmlFor={`${componentName}__errorMessage`}>
            {resourcesContext.messages['ruleErrorMessage']}
            <TooltipButton message={resourcesContext.messages['noQuotesQCTooltip']} parentName="errorMessage" />
          </label>
          <InputText
            id={`${componentName}__errorMessage`}
            keyfilter="noDoubleQuote"
            maxLength={config.INPUT_MAX_LENGTH}
            onChange={e => onInfoFieldChange('errorMessage', e.target.value)}
            placeholder={resourcesContext.messages['ruleErrorMessage']}
            value={creationFormState.candidateRule.errorMessage}
          />
        </div>
      </div>
    </div>
  );
};
