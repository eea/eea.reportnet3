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
  position
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { expressionId } = expressionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const [operatorTypes, setOperatorTypes] = useState([]);
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
      <span className={styles.union}>
        <Dropdown
          disabled={isDisabled || position == 0}
          appendTo={document.body}
          placeholder={resourcesContext.messages.union}
          optionLabel="label"
          options={config.validations.logicalOperators}
          onChange={e =>
            onExpressionFieldUpdate(expressionId, {
              key: 'union',
              value: e.target.value
            })
          }
          value={{ label: expressionValues.union, value: expressionValues.union }}
        />
      </span>
      <span className={styles.operatorType}>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder={resourcesContext.messages.operatorType}
          optionLabel="label"
          options={operatorTypes}
          onChange={e =>
            onExpressionFieldUpdate(expressionId, {
              key: 'operatorType',
              value: e.target.value
            })
          }
          value={!isEmpty(expressionValues.operatorType) ? operatorTypesConf[expressionValues.operatorType].option : ''}
        />
      </span>
      <span className={styles.operatorValue}>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder={resourcesContext.messages.operator}
          optionLabel="label"
          options={operatorValues}
          onChange={e =>
            onExpressionFieldUpdate(expressionId, {
              key: 'operatorValue',
              value: e.target.value
            })
          }
          value={
            !isEmpty(expressionValues.operatorValue)
              ? { label: expressionValues.operatorValue, value: expressionValues.operatorValue }
              : ''
          }
        />
      </span>
      <span className={styles.operatorValue}>
        {expressionValues.operatorType == 'date' ? (
          <Calendar
            appendTo={document.body}
            baseZIndex={6000}
            dateFormat="yy-mm-dd"
            monthNavigator={true}
            readOnlyInput={false}
            onChange={e => {
              onExpressionFieldUpdate(expressionId, {
                key: 'expressionValue',
                value: { value: e.target.value }
              });
            }}
            value={expressionValues.expressionValue}
            yearNavigator={true}
            yearRange="2015:2030"></Calendar>
        ) : (
          <InputText
            disabled={isDisabled}
            placeholder={resourcesContext.messages.value}
            value={expressionValues.expressionValue}
            keyfilter={expressionValues.operatorType == 'LEN' || expressionValues.operatorType == 'number' ? 'num' : ''}
            onChange={e =>
              onExpressionFieldUpdate(expressionId, {
                key: 'expressionValue',
                value: { value: e.target.value }
              })
            }
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
