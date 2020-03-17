import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ValidationExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
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
  const operatorTypes = config.validations.operatorTypes;
  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypes[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

  const getOperatorTypeOptions = () => {
    const options = [];
    for (const type in operatorTypes) {
      options.push(operatorTypes[type].option);
    }
    return options;
  };

  // layouts
  const defaultLayout = (
    <li className={styles.expression}>
      <span>
        <Checkbox
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
          isChecked={expressionValues.group}
          disabled={isDisabled}
        />
      </span>
      <span>
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
      <span>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder={resourcesContext.messages.operatorType}
          optionLabel="label"
          options={getOperatorTypeOptions()}
          onChange={e =>
            onExpressionFieldUpdate(expressionId, {
              key: 'operatorType',
              value: e.target.value
            })
          }
          value={!isEmpty(expressionValues.operatorType) ? operatorTypes[expressionValues.operatorType].option : null}
        />
      </span>
      <span>
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
              : null
          }
        />
      </span>
      <span>
        <InputText
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expressionValues.expressionValue}
          onChange={e =>
            onExpressionFieldUpdate(expressionId, {
              key: 'expressionValue',
              value: { value: e.target.value }
            })
          }
        />
      </span>
      <span>
        <Button
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
