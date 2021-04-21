import React, { useContext, useEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ValidationExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import isNil from 'lodash/isNil';

const ValidationExpression = ({
  expressionValues,
  fieldType,
  isDisabled,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  position,
  showRequiredFields
}) => {
  const { expressionId } = expressionValues;
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType }
  } = config;
  const inputStringMatchRef = useRef(null);
  const resourcesContext = useContext(ResourcesContext);
  const [clickedFields, setClickedFields] = useState([]);
  const [isActiveStringMatchInput, setIsActiveStringMatchInput] = useState(false);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [operatorValues, setOperatorValues] = useState([]);
  const [valueInputProps, setValueInputProps] = useState();
  const [valueKeyFilter, setValueKeyFilter] = useState();

  useEffect(() => {
    if (inputStringMatchRef.current && isActiveStringMatchInput) {
      inputStringMatchRef.current.element.focus();
    }
    return () => {
      setIsActiveStringMatchInput(false);
    };
  }, [inputStringMatchRef.current, isActiveStringMatchInput]);

  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

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
    const { operatorType } = expressionValues;
    const cValueProps = { steps: 0, format: false, useGrouping: false };

    if (operatorType === 'number' || operatorType === 'LEN') {
      setValueKeyFilter('num');
    }

    if (operatorType === 'string') {
      setValueKeyFilter('');
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
      conditions = clickedFields.includes(field) && position !== 0 && isEmpty(expressionValues[field]);
    } else if (field === 'expressionValue') {
      conditions =
        clickedFields.includes(field) && !isNil(expressionValues[field]) && isEmpty(expressionValues[field].toString());
    } else {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field]);
    }
    return conditions ? 'error' : '';
  };

  const onUpdateExpressionField = (key, value) => {
    checkField(key, value);
    onDeleteFromClickedFields(key);
    onExpressionFieldUpdate(expressionId, {
      key,
      value
    });
  };

  const onAddToClickedFields = field => {
    setTimeout(() => {
      const cClickedFields = [...clickedFields];
      if (!cClickedFields.includes(field)) {
        cClickedFields.push(field);
        setClickedFields(cClickedFields);
      }
    }, 250);
  };

  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };

  const onCCButtonClick = ccButtonValue => {
    onUpdateExpressionField('expressionValue', ccButtonValue);
    setIsActiveStringMatchInput(true);
  };

  const checkField = (field, fieldValue) => {
    if (field === 'year') {
      const yearInt = parseInt(fieldValue);
      if (yearInt < 1000 || yearInt > 9999) onUpdateExpressionField('expressionValue', 0);
    }

    if (
      expressionValues.operatorType === 'number' &&
      field === 'operatorValue' &&
      fieldValue !== 'MATCH' &&
      !Number(expressionValues.expressionValue)
    ) {
      const number = Number(fieldValue);
      if (!number) onUpdateExpressionField('expressionValue', '');
    }
  };

  const buildValueInput = () => {
    const { operatorType, operatorValue } = expressionValues;

    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return;
    }

    if (operatorType === 'date') {
      return (
        <Calendar
          appendTo={document.body}
          baseZIndex={6000}
          dateFormat="yy-mm-dd"
          monthNavigator={true}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder="YYYY-MM-DD"
          readOnlyInput={false}
          value={expressionValues.expressionValue}
          yearNavigator={true}
          yearRange="1900:2500"></Calendar>
      );
    }
    if (operatorType === 'day') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          max={32}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'string') {
      if (operatorValue === 'MATCH') {
        const ccButtonValue = `${expressionValues.expressionValue}{%R3_COUNTRY_CODE%}`;
        return (
          <span className={styles.inputStringMatch}>
            <InputText
              disabled={isDisabled}
              onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
              placeholder={resourcesContext.messages.value}
              value={expressionValues.expressionValue}
              ref={inputStringMatchRef}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label="CC"
              tooltip={resourcesContext.messages['matchStringTooltip']}
              tooltipOptions={{ position: 'top' }}
              onClick={() => onCCButtonClick(ccButtonValue)}
            />
          </span>
        );
      }
    }

    if (operatorType === 'number') {
      if (operatorValue === 'MATCH') {
        return (
          <InputText
            disabled={isDisabled}
            onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
            placeholder={resourcesContext.messages.value}
            value={expressionValues.expressionValue}
          />
        );
      }

      if (fieldType === 'NUMBER_DECIMAL') {
        return (
          <InputText
            keyfilter={valueKeyFilter}
            disabled={isDisabled}
            format={false}
            onBlur={e => checkField('number', e.target.value)}
            onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
            placeholder={resourcesContext.messages.value}
            value={expressionValues.expressionValue}
          />
        );
      }

      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          mode="decimal"
          onBlur={e => checkField('number', e.target.value)}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'year') {
      return (
        <InputNumber
          disabled={isDisabled}
          mode="decimal"
          onBlur={e => checkField('year', e.target.value)}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'month') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          max={13}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }
    return (
      <InputText
        keyfilter={valueKeyFilter}
        disabled={isDisabled}
        onChange={e => {
          onUpdateExpressionField('expressionValue', e.target.value);
        }}
        placeholder={resourcesContext.messages.value}
        value={expressionValues.expressionValue}
      />
    );
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
          disabled={isDisabled || position === 0}
          onChange={e => onUpdateExpressionField('union', e.target.value)}
          optionLabel="label"
          options={config.validations.logicalOperators}
          placeholder={resourcesContext.messages.union}
          value={expressionValues.union}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          disabled={isDisabled}
          onChange={e => onUpdateExpressionField('operatorType', e.target.value)}
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
          disabled={isDisabled}
          onChange={e => onUpdateExpressionField('operatorValue', e.target.value)}
          optionLabel="label"
          options={operatorValues}
          placeholder={resourcesContext.messages.operator}
          value={expressionValues.operatorValue}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('expressionValue')}
        className={`${styles.expressionValue} formField ${printRequiredFieldError('expressionValue')}`}>
        {buildValueInput()}
      </span>
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
export { ValidationExpression };
