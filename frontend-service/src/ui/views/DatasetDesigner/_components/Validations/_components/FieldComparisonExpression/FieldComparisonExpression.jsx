import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './FieldComparisonExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputNumber } from 'primereact/inputnumber';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import isNil from 'lodash/isNil';

const FieldComparisonExpression = ({
  expressionValues,
  isDisabled,
  layout,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  position,
  showRequiredFields,
  fieldType
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [clickedFields, setClickedFields] = useState([]);
  const [valueInputProps, setValueInputProps] = useState();
  const [valueKeyFilter, setValueKeyFilter] = useState();
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType }
  } = config;

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
    const { operatorType } = expressionValues;
    const cValueProps = { steps: 0, format: false, useGrouping: false };
    if (operatorType === 'number' || operatorType === 'LEN') {
      setValueKeyFilter('num');
    }

    if (fieldType === 'DATE') {
      if (operatorType === 'year') {
        cValueProps.min = 1900;
        cValueProps.max = 2500;
      }
      if (operatorType === 'month') {
        cValueProps.min = 1;
        cValueProps.max = 12;
      }
      if (operatorType === 'day') {
        cValueProps.min = 1;
        cValueProps.max = 31;
      }
      setValueInputProps(cValueProps);
    }
  }, [expressionValues.operatorType]);

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
      conditions = clickedFields.includes(field) && position != 0 && isEmpty(expressionValues[field]);
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
  const buildValueInput = () => {
    const { operatorType, operatorValue } = expressionValues;
    if (operatorType === 'date') {
      return (
        <Calendar
          appendTo={document.body}
          baseZIndex={6000}
          dateFormat="yy-mm-dd"
          placeholder="YYYY-MM-DD"
          monthNavigator={true}
          readOnlyInput={false}
          onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          value={expressionValues.expressionValue}
          yearNavigator={true}
          yearRange="1900:2500"></Calendar>
      );
    }
    if (operatorType === 'day') {
      return (
        <InputNumber
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expressionValues.expressionValue}
          onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          steps={0}
          format={false}
          useGrouping={false}
          min={0}
          max={32}
          mode="decimal"
        />
      );
    }
    if (operatorType === 'number') {
      if (operatorValue === 'MATCH') {
        return (
          <InputText
            disabled={isDisabled}
            placeholder={resourcesContext.messages.value}
            value={expressionValues.expressionValue}
            onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          />
        );
      }
      return (
        <InputNumber
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expressionValues.expressionValue}
          onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          steps={0}
          format={false}
          useGrouping={false}
          mode="decimal"
          onBlur={e => {
            checkField('number', e.target.value);
          }}
        />
      );
    }
    if (operatorType === 'year') {
      return (
        <InputNumber
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expressionValues.expressionValue}
          onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          onBlur={e => {
            checkField('year', e.target.value);
          }}
          steps={0}
          useGrouping={false}
          mode="decimal"
        />
      );
    }
    if (operatorType === 'month') {
      return (
        <InputNumber
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expressionValues.expressionValue}
          onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          steps={0}
          format={false}
          useGrouping={false}
          min={0}
          max={13}
          mode="decimal"
        />
      );
    }
    return (
      <InputText
        disabled={isDisabled}
        placeholder={resourcesContext.messages.value}
        value={expressionValues.expressionValue}
        onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
        keyfilter={valueKeyFilter}
      />
    );
  };

  // layouts
  const defaultLayout = (
    <li className={styles.expression}>
      <span className={styles.group}>
        <Checkbox
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
          isChecked={expressionValues.group}
          disabled={isDisabled}
        />
      </span>
      <span
        onBlur={e => onAddToClickedFields('union')}
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}>
        <Dropdown
          disabled={isDisabled || position == 0}
          appendTo={document.body}
          placeholder={resourcesContext.messages.union}
          optionLabel="label"
          options={config.validations.logicalOperators}
          onChange={e => onUpdateExpressionField('union', e.target.value)}
          value={{ label: expressionValues.union, value: expressionValues.union }}
        />
      </span>
      <span
        onBlur={e => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder={resourcesContext.messages.operatorType}
          optionLabel="label"
          options={operatorTypes}
          onChange={e => onUpdateExpressionField('operatorType', e.target.value)}
          value={!isEmpty(expressionValues.operatorType) ? operatorTypesConf[expressionValues.operatorType].option : ''}
        />
      </span>
      <span
        onBlur={e => onAddToClickedFields('operatorValue')}
        className={`${styles.operatorValue} formField ${printRequiredFieldError('operatorValue')}`}>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder={resourcesContext.messages.operator}
          optionLabel="label"
          options={operatorValues}
          onChange={e => onUpdateExpressionField('operatorValue', e.target.value)}
          value={
            !isEmpty(expressionValues.operatorValue)
              ? { label: expressionValues.operatorValue, value: expressionValues.operatorValue }
              : ''
          }
        />
      </span>
      <span
        onBlur={e => onAddToClickedFields('expressionValue')}
        className={`${styles.expressionValue} formField ${printRequiredFieldError('expressionValue')}`}>
        {buildValueInput()}
      </span>
      <span>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
          disabled={isDisabled}
          type="button"
          icon="trash"
          onClick={e => {
            onExpressionDelete(expressionId);
          }}
        />
      </span>
    </li>
  );
  const layouts = {
    default: defaultLayout
  };

  return layout ? layouts[layout] : layouts['default'];
};
export { ValidationExpression };
