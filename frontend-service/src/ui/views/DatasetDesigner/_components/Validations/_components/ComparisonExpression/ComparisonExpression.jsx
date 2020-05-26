import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ComparisonExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputNumber } from 'primereact/inputnumber';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import isNil from 'lodash/isNil';

const ComparisonExpression = ({
  expressionValues,
  fieldType,
  isDisabled,
  layout,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  position,
  showRequiredFields,
  rawTableFields
}) => {
  const componentName = 'fieldComparison';
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [clickedFields, setClickedFields] = useState([]);
  const [valueInputProps, setValueInputProps] = useState();
  const [valueKeyFilter, setValueKeyFilter] = useState();
  const [tableFields, setTableFields] = useState([]);
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType }
  } = config;

  useEffect(() => {
    if (rawTableFields) {
      const parsedTableFields = rawTableFields.map(field => {
        return { label: field.label, value: field.code };
      });
      setTableFields(parsedTableFields);
    }
  }, [rawTableFields]);

  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

  useEffect(() => {
    const options = [];
    let operatorOfType = null;
    if (!isNil(fieldType)) {
      operatorByType[fieldType].forEach(key => {
        options.push(operatorTypesConf[key].option);
      });
    } else {
      for (let type in operatorTypesConf) {
        options.push(operatorTypesConf[type].option);
      }
    }
    setOperatorTypes(options);
  }, [fieldType]);

  useEffect(() => {
    if (showRequiredFields) {
      const fieldsToAdd = [];
      ['union', 'operatorType', 'operatorValue', 'expressionValue'].forEach(field => {
        if (!clickedFields.includes(field)) fieldsToAdd.push(field);
      });
      setClickedFields([...clickedFields, ...fieldsToAdd]);
    }
  }, [showRequiredFields]);

  useEffect(() => {
    let errors = false;
    clickedFields.forEach(clickedField => {
      if (printRequiredFieldError(clickedField) === 'error') {
        errors = true;
      }
    });
    if (errors) {
      onExpressionsErrors(expressionId, true);
    } else {
      onExpressionsErrors(expressionId, false);
    }
  }, [clickedFields, showRequiredFields]);

  const printRequiredFieldError = field => {
    let conditions = false;
    if (field === 'union') {
      conditions = clickedFields.includes(field) && position !== 0 && isEmpty(expressionValues[field]);
    } else if (field === 'expressionValue') {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field].toString());
    } else {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field]);
    }
    return conditions ? 'error' : '';
  };

  const onUpdateExpressionField = (key, value) => {
    checkField(key, value.value);
    onDeleteFromClickedFields(key);
    onExpressionFieldUpdate(expressionId, {
      key,
      value
    });
  };

  const onAddToClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (!cClickedFields.includes(field)) {
      cClickedFields.push(field);
      setClickedFields(cClickedFields);
    }
  };
  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };
  const checkField = (field, fieldValue) => {
    if (field === 'year') {
      const yearInt = parseInt(fieldValue);
      if (yearInt < 1000 || yearInt > 9999) {
        onUpdateExpressionField('expressionValue', 0);
      }
    }
    if (expressionValues.operatorType === 'number' && field === 'operatorValue' && fieldValue !== 'MATCH') {
      const number = Number(fieldValue);
      if (!number) {
        onUpdateExpressionField('expressionValue', '');
      }
    }
  };

  return (
    <li className={styles.expression}>
      <span className={styles.group}>
        <Checkbox
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
          isChecked={expressionValues.group}
          disabled={isDisabled}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('union')}
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}>
        <Dropdown
          disabled={isDisabled || position === 0}
          appendTo={document.body}
          placeholder={resourcesContext.messages.union}
          optionLabel="label"
          options={config.validations.logicalOperators}
          onChange={e => onUpdateExpressionField('union', e.target.value)}
          value={{ label: expressionValues.union, value: expressionValues.union }}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('field1')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field1')}`}>
        <Dropdown
          id={`${componentName}__field1`}
          disabled={false}
          // appendTo={document.body}
          placeholder={'Select first field'}
          optionLabel={'label'}
          options={tableFields}
          onChange={e => onUpdateExpressionField('field1', e.value)}
          value={expressionValues.field1}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          disabled={isDisabled}
          // appendTo={document.body}
          placeholder={resourcesContext.messages.operatorType}
          optionLabel="label"
          options={operatorTypes}
          onChange={e => onUpdateExpressionField('operatorType', e.value)}
          // value={!isEmpty(expressionValues.operatorType) ? operatorTypesConf[expressionValues.operatorType].option : ''}
          value={expressionValues.operatorType}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorValue')}
        className={`${styles.operatorValue} formField ${printRequiredFieldError('operatorValue')}`}>
        <Dropdown
          disabled={isDisabled}
          // appendTo={document.body}
          placeholder={resourcesContext.messages.operator}
          optionLabel="label"
          optionValue="value"
          options={operatorValues}
          onChange={e => onUpdateExpressionField('operatorValue', e.value)}
          /* value={
            !isEmpty(expressionValues.operatorValue)
              ? { label: expressionValues.operatorValue, value: expressionValues.operatorValue }
              : ''
          } */
          value={expressionValues.operatorValue}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('field2')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field2')}`}>
        {console.log('expressionValues', expressionValues)}
        <Dropdown
          id={`${componentName}__field2`}
          disabled={false}
          // appendTo={document.body}
          filterPlaceholder={'Select second field'}
          placeholder={'Select second field'}
          optionLabel="label"
          options={tableFields}
          onChange={e => onUpdateExpressionField('field2', e.value)}
          value={expressionValues.field2}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('expressionValue')}
        className={`${styles.expressionValue} formField ${printRequiredFieldError('expressionValue')}`}></span>
      <span>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
          disabled={isDisabled}
          type="button"
          icon="trash"
          onClick={() => {
            onExpressionDelete(expressionId);
          }}
        />
      </span>
    </li>
  );
};
export { ComparisonExpression };
