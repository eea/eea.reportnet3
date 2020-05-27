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
import { getFieldType } from '../../_functions/utils/getFieldType';

const ComparisonExpression = ({
  expressionValues,
  isDisabled,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  position,
  showRequiredFields,
  rawTableFields,
  onGetFieldType
}) => {
  const componentName = 'fieldComparison';
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [clickedFields, setClickedFields] = useState([]);
  const [tableFields, setTableFields] = useState([]);
  const [fieldType, setFieldType] = useState(null);
  const [secondFieldOptions, setSecondFieldOptions] = useState();
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType, fieldByFieldType }
  } = config;

  useEffect(() => {
    if (rawTableFields) {
      const parsedTableFields = rawTableFields.map(field => {
        return { label: field.label, value: field.code };
      });
      setTableFields(parsedTableFields);
      setSecondFieldOptions(parsedTableFields);
    }
  }, [rawTableFields]);

  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

  useEffect(() => {
    if (!isEmpty(expressionValues.field1)) {
      setFieldType(onGetFieldType(expressionValues.field1));
    }
  }, [expressionValues.field1]);

  useEffect(() => {
    if (!isEmpty(expressionValues.field1) && !isEmpty(fieldType)) {
      const compatibleFieldTypes = fieldByFieldType[fieldType];

      setSecondFieldOptions(
        tableFields.filter(field => {
          const cFieldType = onGetFieldType(field.value);
          const result = compatibleFieldTypes.includes(cFieldType);
          return result;
        })
      );
    }
  }, [expressionValues.field1, fieldType]);

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
          disabled={isDisabled}
          isChecked={expressionValues.group}
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('union')}
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled || position === 0}
          onChange={e => onUpdateExpressionField('union', e.value)}
          optionLabel="label"
          options={config.validations.logicalOperators}
          placeholder={resourcesContext.messages.union}
          value={{ label: expressionValues.union, value: expressionValues.union }}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('field1')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field1')}`}>
        <Dropdown
          // appendTo={document.body}
          disabled={false}
          id={`${componentName}__field1`}
          onChange={e => onUpdateExpressionField('field1', e.value)}
          optionLabel={'label'}
          options={tableFields}
          placeholder={'Select first field'}
          value={expressionValues.field1}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          // appendTo={document.body}
          disabled={isDisabled}
          onChange={e => onUpdateExpressionField('operatorType', e.value)}
          optionLabel="label"
          options={operatorTypes}
          placeholder={resourcesContext.messages.operatorType}
          value={expressionValues.operatorType}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorValue')}
        className={`${styles.operatorValue} formField ${printRequiredFieldError('operatorValue')}`}>
        <Dropdown
          // appendTo={document.body}
          disabled={isDisabled}
          onChange={e => onUpdateExpressionField('operatorValue', e.value)}
          optionLabel="label"
          options={operatorValues}
          optionValue="value"
          placeholder={resourcesContext.messages.operator}
          value={expressionValues.operatorValue}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('field2')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field2')}`}>
        <Dropdown
          // appendTo={document.body}
          disabled={false}
          filterPlaceholder={'Select second field'}
          id={`${componentName}__field2`}
          onChange={e => onUpdateExpressionField('field2', e.value)}
          optionLabel="label"
          options={secondFieldOptions}
          placeholder={'Select second field'}
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
          icon="trash"
          onClick={() => onExpressionDelete(expressionId)}
          type="button"
        />
      </span>
    </li>
  );
};
export { ComparisonExpression };
