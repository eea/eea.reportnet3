import { useState, useContext } from 'react';

import styles from './ValidationExpressionGroup.module.scss';

import { config } from 'conf/';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import isEmpty from 'lodash/isEmpty';
import first from 'lodash/first';

import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ValidationExpressionSelector } from '../ValidationExpressionSelector';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const ValidationExpressionGroup = ({
  expressionType,
  expressionValues,
  fieldType,
  isDisabled,
  layout,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  onGetFieldType,
  position,
  showRequiredFields,
  rawTableFields
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
          onClick={e => expressionsVisibilityToggle()}
          style={btnStyle}
        />
      );
    } else {
      return (
        <FontAwesomeIcon
          icon={AwesomeIcons('collapsed')}
          onClick={e => expressionsVisibilityToggle()}
          style={btnStyle}
        />
      );
    }
  };
  const getContainedExpressions = () => {
    if (expressionValues.expressions.length > 0) {
      return expressionValues.expressions.map((expression, i) => (
        <ValidationExpressionSelector
          expressionType={expressionType}
          expressionValues={expression}
          fieldType={fieldType}
          isDisabled={false}
          key={expression.expressionId}
          onExpressionDelete={onExpressionDelete}
          onExpressionFieldUpdate={onExpressionFieldUpdate}
          onExpressionGroup={onExpressionGroup}
          onExpressionsErrors={onExpressionsErrors}
          onGetFieldType={onGetFieldType}
          position={i}
          rawTableFields={rawTableFields}
        />
      ));
    }
    return <div />;
  };

  // layouts
  const defaultLayout = (
    <li className={styles.groupExpression}>
      <ul>
        <li className={styles.expression}>
          <div className={styles.groupRow}>
            <span className={styles.group}>
              <Checkbox
                checked={expressionValues.group}
                disabled={isDisabled}
                onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
              />
            </span>
            <span
              className={`${styles.union} formField ${
                showRequiredFields && position > 0 && isEmpty(expressionValues.union) ? 'error' : ''
              }`}>
              <Dropdown
                appendTo={document.body}
                disabled={isDisabled || position === 0}
                onChange={e => {
                  onExpressionFieldUpdate(expressionId, { key: 'union', value: e.target.value.value });
                }}
                optionLabel="label"
                options={config.validations.logicalOperators}
                placeholder={resourcesContext.messages.union}
                value={first(
                  config.validations.logicalOperators.filter(option => option.value === expressionValues.union)
                )}
              />
            </span>
            <span className={styles.groupToggler}>
              <FontAwesomeIcon icon={AwesomeIcons('folder')} style={{ fontSize: '2rem' }} />
              {expressionsVisibilityToggleBtn()}
            </span>
            <span>
              <Button
                className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
                disabled={isDisabled}
                icon="trash"
                onClick={e => {
                  onExpressionDelete(expressionId);
                }}
                type="button"
              />
            </span>
          </div>
          <div className={styles.groupExpressions}>
            <span>
              <ul>{groupExpressionsVisible && getContainedExpressions()}</ul>
            </span>
          </div>
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
