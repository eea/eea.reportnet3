import React, { useState, useEffect, useContext } from 'react';

import styles from './ValidationExpresionGroup.module.scss';

import { config } from 'conf/';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ValidationExpresionGroup = ({
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
  const [groupExpresionsVisible, setGroupExpresionsVisible] = useState(true);
  const expresionsVisibilityToggle = () => {
    setGroupExpresionsVisible(!groupExpresionsVisible);
  };
  const expresionsVisibilityToggleBtn = () => {
    const btnStyle = {
      fontSize: '2rem',
      cursor: 'pointer'
    };
    if (groupExpresionsVisible) {
      return (
        <FontAwesomeIcon icon={AwesomeIcons('expanded')} style={btnStyle} onClick={e => expresionsVisibilityToggle()} />
      );
    } else {
      return (
        <FontAwesomeIcon
          icon={AwesomeIcons('collapsed')}
          style={btnStyle}
          onClick={e => expresionsVisibilityToggle()}
        />
      );
    }
  };

  // layouts
  const defaultLayout = (
    <li className={styles.groupExpresion}>
      <ul>
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
            <FontAwesomeIcon icon={AwesomeIcons('folder')} style={{ fontSize: '2rem' }} />
            {expresionsVisibilityToggleBtn()}
          </span>
        </li>
      </ul>
    </li>
  );
  const layouts = {
    default: defaultLayout
  };

  return layout ? layouts[layout] : layouts['default'];
};
export { ValidationExpresionGroup };
