import React, { useContext, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './TreeViewExpandableItem.module.css';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TreeViewExpandableItem = ({ buttons, expanded = true, children, items, className }) => {
  const [isOpen, setIsOpen] = useState(expanded);
  const resources = useContext(ResourcesContext);

  const renderHeader = () => {
    console.log(items);
    const width = 90 / items.length;
    return items.map((item, i) => (
      <span key={i} style={{ width: `${width}%` }}>
        {item}
      </span>
    ));
  };

  const renderButtons = () => {
    return !isUndefined(buttons)
      ? buttons.map((button, i) => (
          <Button
            icon={button.icon}
            disabled={!isUndefined(button.disabled) ? button.disabled : false}
            key={i}
            label={button.label}
            onClick={button.onClick}
            style={{ marginLeft: '0.5rem' }}
            tooltip={button.tooltip}
            tooltipOptions={{ position: 'bottom' }}
            visible={!isUndefined(button.visible) ? button.visible : true}
          />
        ))
      : null;
  };

  return (
    <React.Fragment>
      <div className={!isUndefined(className) ? className : styles.defaultExpandable}>
        {!isUndefined(items) & (items.length > 0) ? (
          isOpen ? (
            <FontAwesomeIcon
              icon={AwesomeIcons('minusSquare')}
              style={{ cursor: 'pointer' }}
              onClick={() => setIsOpen(!isOpen)}
            />
          ) : (
            <FontAwesomeIcon
              icon={AwesomeIcons('plusSquare')}
              style={{ cursor: 'pointer' }}
              onClick={() => setIsOpen(!isOpen)}
            />
          )
        ) : (
          ''
        )}
        {renderHeader()}
        {renderButtons()}
      </div>
      {isOpen ? children : null}
      {React.Children.count(children) === 0 && isOpen ? resources.messages['emptyDatasetDesign'] : null}
    </React.Fragment>
  );
};

export { TreeViewExpandableItem };
