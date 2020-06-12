import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ComparisonExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'primereact/dropdown';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ComparisonExpression = ({
  expressionValues,
  isDisabled,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  onGetFieldType,
  position,
  rawTableFields,
  showRequiredFields
}) => {
  const componentName = 'fieldComparison';
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [clickedFields, setClickedFields] = useState([]);
  const [fieldType, setFieldType] = useState(null);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [operatorValues, setOperatorValues] = useState([]);
  const [secondFieldOptions, setSecondFieldOptions] = useState();
  const [tableFields, setTableFields] = useState([]);
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType, fieldByOperatorType }
  } = config;
  const [disabledFields, setDisabledFields] = useState({});

  useEffect(() => {
    const newDisabledFields = {};

    if (isDisabled) {
      newDisabledFields.field1 = true;
    } else {
      newDisabledFields.field1 = false;
    }

    if (isEmpty(expressionValues.field1)) {
      newDisabledFields.operatorType = true;
    } else {
      newDisabledFields.operatorType = false;
    }

    if (isEmpty(expressionValues.operatorType)) {
      newDisabledFields.operatorValue = true;
    } else {
      newDisabledFields.operatorValue = false;
    }

    if (isEmpty(expressionValues.operatorValue)) {
      newDisabledFields.field2 = true;
    } else {
      newDisabledFields.field2 = false;
    }

    setDisabledFields({
      ...disabledFields,
      ...newDisabledFields
    });
  }, [
    expressionValues.field1,
    expressionValues.field2,
    expressionValues.operatorType,
    expressionValues.operatorValue,
    isDisabled
  ]);

  useEffect(() => {
    if (rawTableFields) {
      const parsedTableFields = rawTableFields.map(field => {
        return { label: field.label, value: field.code, type: field.type };
      });
      setTableFields(parsedTableFields);
      setSecondFieldOptions(parsedTableFields);
    }
  }, [rawTableFields]);

  useEffect(() => {
    if (!isEmpty(tableFields)) {
      setDisabledFields({
        ...disabledFields,
        field1: false
      });
    }
  }, [tableFields]);

  // useEffect(() => {
  //   ['operatorType', 'operatorValue', 'field2'].forEach(field => {
  //     onExpressionFieldUpdate(expressionId, {
  //       key: field,
  //       value: ''
  //     });
  //   });
  // }, [expressionValues.field1]);

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
    if (!isEmpty(expressionValues.field1) && !isEmpty(fieldType) && expressionValues.operatorType) {
      const compatibleFieldTypes = fieldByOperatorType[expressionValues.operatorType];
      const allFields = tableFields.filter(field => {
        const cFieldType = onGetFieldType(field.value);
        const result = compatibleFieldTypes.includes(cFieldType);
        return result;
      });

      setSecondFieldOptions(allFields.filter(cField => cField.value !== expressionValues.field1));
    }
  }, [expressionValues.field1, expressionValues.operatorType, fieldType, tableFields]);

  useEffect(() => {
    const options = [];
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

      ['union', 'field1', 'operatorType', 'operatorValue', 'field2'].forEach(field => {
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

    if (key === 'field1' && value !== expressionValues.field1) {
      ['operatorType', 'operatorValue', 'field2'].forEach(field => {
        onExpressionFieldUpdate(expressionId, {
          key: field,
          value: ''
        });
      });
    }

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
          disabled={disabledFields.union}
          isChecked={expressionValues.group}
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
        />
      </span>

      <span
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}
        onBlur={() => onAddToClickedFields('union')}>
        <Dropdown
          disabled={position === 0}
          onChange={e => onUpdateExpressionField('union', e.value)}
          optionLabel="label"
          options={config.validations.logicalOperators}
          placeholder={resourcesContext.messages.union}
          value={expressionValues.union}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('field1')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field1')}`}>
        <Dropdown
          disabled={disabledFields.field1}
          id={`${componentName}__field1`}
          onChange={e => {
            onUpdateExpressionField('field1', e.value);
          }}
          optionLabel={'label'}
          options={tableFields}
          placeholder={resourcesContext.messages.selectField}
          value={expressionValues.field1}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          disabled={disabledFields.operatorType}
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
          disabled={disabledFields.operatorValue}
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
          disabled={disabledFields.field2}
          filterPlaceholder={resourcesContext.messages.selectField}
          id={`${componentName}__field2`}
          onChange={e => onUpdateExpressionField('field2', e.value)}
          optionLabel="label"
          options={secondFieldOptions}
          placeholder={resourcesContext.messages.selectField}
          value={expressionValues.field2}
        />
      </span>

      <div className={styles.deleteBtnWrap}>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
          disabled={isDisabled}
          icon="trash"
          onClick={() => onExpressionDelete(expressionId)}
          type="button"
        />
      </div>
    </li>
  );
};
export { ComparisonExpression };
