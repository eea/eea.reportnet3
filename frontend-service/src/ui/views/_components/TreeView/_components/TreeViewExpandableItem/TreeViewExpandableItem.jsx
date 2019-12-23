import React, { useContext, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './TreeViewExpandableItem.module.css';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TreeViewExpandableItem = ({ expanded = true, children, items, className }) => {
  const [isOpen, setIsOpen] = useState(expanded);
  const resources = useContext(ResourcesContext);

  const renderHeader = () => {
    const width = 90 / items.length;
    return items.map(item => <span style={{ width: `${width}%` }}>{item}</span>);
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
      </div>
      {isOpen ? children : null}
      {React.Children.count(children) === 0 && isOpen ? resources.messages['emptyDatasetDesign'] : null}
    </React.Fragment>
  );
};

export { TreeViewExpandableItem };
