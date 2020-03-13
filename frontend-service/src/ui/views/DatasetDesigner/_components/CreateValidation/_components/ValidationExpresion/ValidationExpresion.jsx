import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ValidationExpresion.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ValidationExpresion = ({
  expresionValues,
  isDisabled,
  layout,
  onExpresionDelete,
  onExpresionFieldUpdate,
  onExpresionGroup,
  position
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { expresionId } = expresionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const operatorTypes = config.validations.operatorTypes;
  useEffect(() => {
    if (expresionValues.operatorType) {
      setOperatorValues(operatorTypes[expresionValues.operatorType].values);
    }
  }, [expresionValues.operatorType]);

  const getOperatorTypeOptions = () => {
    const options = [];
    for (const type in operatorTypes) {
      options.push(operatorTypes[type].option);
    }
    return options;
  };

  // layouts
  const defaultLayout = (
    <li className={styles.expresion}>
      <span>
        <Checkbox
          onChange={e => onExpresionGroup(expresionId, { key: 'group', value: e.checked })}
          isChecked={expresionValues.group}
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
            onExpresionFieldUpdate(expresionId, {
              key: 'union',
              value: e.target.value
            })
          }
          value={{ label: expresionValues.union, value: expresionValues.union }}
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
            onExpresionFieldUpdate(expresionId, {
              key: 'operatorType',
              value: e.target.value
            })
          }
          value={!isEmpty(expresionValues.operatorType) ? operatorTypes[expresionValues.operatorType].option : null}
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
            onExpresionFieldUpdate(expresionId, {
              key: 'operatorValue',
              value: e.target.value
            })
          }
          value={
            !isEmpty(expresionValues.operatorValue)
              ? { label: expresionValues.operatorValue, value: expresionValues.operatorValue }
              : null
          }
        />
      </span>
      <span>
        <InputText
          disabled={isDisabled}
          placeholder={resourcesContext.messages.value}
          value={expresionValues.expresionValue}
          onChange={e =>
            onExpresionFieldUpdate(expresionId, {
              key: 'expresionValue',
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
            onExpresionDelete(expresionId);
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
export { ValidationExpresion };
