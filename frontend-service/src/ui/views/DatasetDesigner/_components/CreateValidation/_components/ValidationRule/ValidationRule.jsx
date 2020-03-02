import React, { useState, useEffect } from 'react';

import { isUndefined, isEmpty } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

const ValidationRule = ({ deleteRule, isDisabled, layout, ruleValues, setValidationRule }) => {
  const onChangeField = field => {
    setValidationRule(ruleValues.ruleId, field);
  };
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
    if (ruleValues.operatorType) {
      setOperatorValues(operatorTypes[ruleValues.operatorType].values);
    }
  }, [ruleValues.operatorType]);

  // layouts
  const defaultLayout = (
    <tr>
      <td>
        <Checkbox
          onChange={e => {
            onChangeField({ key: 'group', value: { value: e.checked } });
          }}
          isChecked={ruleValues.group}
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
          onChange={e => {
            onChangeField({
              key: 'union',
              value: e.target.value
            });
          }}
          value={{ label: ruleValues.union, value: ruleValues.union }}
        />
      </td>
      <td>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder="Operator type"
          optionLabel="label"
          options={getOperatorTypeOptions()}
          onChange={e => {
            onChangeField({
              key: 'operatorType',
              value: e.target.value
            });
          }}
          value={!isEmpty(ruleValues.operatorType) ? operatorTypes[ruleValues.operatorType].option : null}
        />
      </td>
      <td>
        <Dropdown
          disabled={isDisabled}
          appendTo={document.body}
          placeholder="Operator"
          optionLabel="label"
          options={operatorValues}
          onChange={e => {
            onChangeField({
              key: 'operatorValue',
              value: e.target.value
            });
          }}
          value={
            !isEmpty(ruleValues.operatorValue)
              ? { label: ruleValues.operatorValue, value: ruleValues.operatorValue }
              : null
          }
        />
      </td>
      <td>
        <InputText
          disabled={isDisabled}
          placeholder="Value"
          value={ruleValues.ruleValue}
          onChange={e => {
            onChangeField({
              key: 'ruleValue',
              value: { value: e.target.value }
            });
          }}
        />
      </td>
      <td>
        <Button
          disabled={isDisabled}
          type="button"
          icon="trash"
          onClick={e => {
            deleteRule(ruleValues.ruleId);
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
export { ValidationRule };
