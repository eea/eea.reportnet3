import React, { useState, useContext, Fragment } from 'react';

import styles from './ValidationExpressionGroup.module.scss';

import { config } from 'conf/';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ValidationExpressionSelector } from '../ValidationExpressionSelector';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ValidationExpressionGroup = ({
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
  const [groupExpressionsVisible, setGroupExpressionsVisible] = useState(true);
  const expressionsVisibilityToggle = () => {
    setGroupExpressionsVisible(!groupExpressionsVisible);
  };
  const expressionsVisibilityToggleBtn = () => {
    const btnStyle = {
      fontSize: '2rem',
      cursor: 'pointer'
    };
    if (groupExpressionsVisible) {
      return (
        <FontAwesomeIcon
          icon={AwesomeIcons('expanded')}
          style={btnStyle}
          onClick={e => expressionsVisibilityToggle()}
        />
      );
    } else {
      return (
        <FontAwesomeIcon
          icon={AwesomeIcons('collapsed')}
          style={btnStyle}
          onClick={e => expressionsVisibilityToggle()}
        />
      );
    }
  };
  const getContainedExpressions = () => {
    if (expressionValues.expressions.length > 0) {
      return expressionValues.expressions.map((expression, i) => (
        <ValidationExpressionSelector
          expressionValues={expression}
          isDisabled={false}
          onExpressionDelete={onExpressionDelete}
          onExpressionFieldUpdate={onExpressionFieldUpdate}
          onExpressionGroup={onExpressionGroup}
          position={i}
        />
      ));
    }
    return <Fragment></Fragment>;
  };

  // layouts
  const defaultLayout = (
    <li className={styles.groupExpression}>
      <ul>
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
            <FontAwesomeIcon icon={AwesomeIcons('folder')} style={{ fontSize: '2rem' }} />
            {expressionsVisibilityToggleBtn()}
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
          <span>{groupExpressionsVisible && getContainedExpressions()}</span>
        </li>
      </ul>
    </li>
  );
  const layouts = {
    default: defaultLayout
  };

  return layout ? layouts[layout] : layouts['default'];
};
export { ValidationExpressionGroup };
