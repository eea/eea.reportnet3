import React, { useState, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

const ValidationExpresion = ({
  expresionValues,
  isDisabled,
  layout,
  onExpresionDelete,
  onExpresionFieldUpdate,
  onExpresionGroup
}) => {
  const { expresionId } = expresionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const operatorTypes = {
    number: {
      option: { label: 'Number', value: 'number' },
      values: [
        { label: '>', value: '>' },
        { label: '<', value: '<' },
        { label: '>=', value: '>=' },
        { label: '<=', value: '<=' },
        { label: '=', value: '=' },
        { label: '!=', value: '!=' }
      ]
    }
    // string: {
    //   option: { label: 'String', value: 'string' },
    //   values: [{ label: 'length', value: 'length' }]
    // },
    // all: {
    //   option: { label: 'All', value: 'all' },
    //   values: []
    // }
  };
  const getOperatorTypeOptions = () => {
    const options = [];
    for (const type in operatorTypes) {
      options.push(operatorTypes[type].option);
    }
    return options;
  };
  useEffect(() => {
    if (expresionValues.operatorType) {
      setOperatorValues(operatorTypes[expresionValues.operatorType].values);
    }
  }, [expresionValues.operatorType]);

  // layouts
  const defaultLayout = (
    <tr>
      <td>
        <Checkbox
          onChange={e => onExpresionGroup({ key: 'group', value: { value: e.checked } })}
          isChecked={expresionValues.group}
          disabled={isDisabled}
        />
      </td>
      <td>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder="union"
          optionLabel="label"
          options={[
            { label: 'AND', value: 'AND' },
            { label: 'OR', value: 'OR' }
          ]}
          onChange={e =>
            onExpresionFieldUpdate(expresionId, {
              key: 'union',
              value: e.target.value
            })
          }
          value={{ label: expresionValues.union, value: expresionValues.union }}
        />
      </td>
      <td>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder="Operator type"
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
      </td>
      <td>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder="Operator"
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
      </td>
      <td>
        <InputText
          disabled={isDisabled}
          placeholder="Value"
          value={expresionValues.ruleValue}
          onChange={e =>
            onExpresionFieldUpdate(expresionId, {
              key: 'expresionValue',
              value: { value: e.target.value }
            })
          }
        />
      </td>
      <td>
        <Button
          disabled={isDisabled}
          type="button"
          icon="trash"
          onClick={e => {
            onExpresionDelete(expresionId);
          }}
        />
      </td>
    </tr>
  );
  const layouts = {
    default: defaultLayout
  };

  return layout ? layouts[layout] : layouts['default'];
};
export { ValidationExpresion };
