import { useState, useContext, Fragment } from 'react';

import styles from './ValidationExpressionGroup.module.scss';

import { config } from 'conf/';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import isEmpty from 'lodash/isEmpty';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'primereact/dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ValidationExpressionSelector } from '../ValidationExpressionSelector';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

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
          expressionType={expressionType}
          expressionValues={expression}
          fieldType={fieldType}
          isDisabled={false}
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
    return <Fragment></Fragment>;
  };

  // layouts
  const defaultLayout = (
    <li className={styles.groupExpression}>
      <ul>
        <li className={styles.expression}>
          <div className={styles.groupRow}>
            <span className={styles.group}>
              <Checkbox
                onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
                isChecked={expressionValues.group}
                disabled={isDisabled}
              />
            </span>
            <span
              className={`${styles.union} formField ${
                showRequiredFields && position > 0 && isEmpty(expressionValues.union) ? 'error' : ''
              }`}>
              <Dropdown
                disabled={isDisabled || position === 0}
                placeholder={resourcesContext.messages.union}
                optionLabel="label"
                options={config.validations.logicalOperators}
                onChange={e => {
                  onExpressionFieldUpdate(expressionId, { key: 'union', value: e.value });
                }}
                value={expressionValues.union}
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
                type="button"
                icon="trash"
                onClick={e => {
                  onExpressionDelete(expressionId);
                }}
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
