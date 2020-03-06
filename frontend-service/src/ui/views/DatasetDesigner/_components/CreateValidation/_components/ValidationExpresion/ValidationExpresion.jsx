import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

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
  onExpresionGroup
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { expresionId } = expresionValues;
  const [operatorValues, setOperatorValues] = useState([]);
  const operatorTypes = config.validations.operatorTypes;
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
      </td>
      <td>
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
      </td>
      <td>
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
      </td>
      <td>
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
