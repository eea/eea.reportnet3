import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import moment from 'moment';

import styles from './ValidationExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ValidationExpression = ({
  expressionValues,
  isDisabled,
  layout,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  position,
  showRequiredFields
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [clickedFields, setClickedFields] = useState([]);
  const {
    validations: { operatorTypes: operatorTypesConf }
  } = config;
  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

  useEffect(() => {
    const options = [];
    for (let type in operatorTypesConf) {
      options.push(operatorTypesConf[type].option);
    }
    setOperatorTypes(options);
  }, []);

  const printRequiredFieldError = field => {
    let conditions = false;
    if (field == 'union') {
      conditions =
        (showRequiredFields || clickedFields.includes(field)) && position != 0 && isEmpty(expressionValues[field]);
    } else if (field == 'expressionValue') {
      conditions = (showRequiredFields || clickedFields.includes(field)) && isEmpty(expressionValues[field].toString());
    } else {
      conditions = (showRequiredFields || clickedFields.includes(field)) && isEmpty(expressionValues[field]);
    }
    return conditions ? 'error' : '';
  };

  const onUpdateExpressionField = (key, value) => {
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
        {expressionValues.operatorType == 'date' ? (
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
        ) : (
          <InputText
            disabled={isDisabled}
            placeholder={resourcesContext.messages.value}
            value={expressionValues.expressionValue}
            keyfilter={expressionValues.operatorType == 'LEN' || expressionValues.operatorType == 'number' ? 'num' : ''}
            onChange={e => onUpdateExpressionField('expressionValue', { value: e.target.value })}
          />
        )}
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
