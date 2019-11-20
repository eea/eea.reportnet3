import React, { useContext, useState } from 'react';

import styles from './TreeViewExpandableItem.module.css';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

const TreeViewExpandableItem = ({ expanded = true, title, children }) => {
  const [isOpen, setIsOpen] = useState(expanded);
  const resources = useContext(ResourcesContext);

  return (
    <React.Fragment>
      <div
        onClick={() => {
          setIsOpen(!isOpen);
        }}
        style={{ color: '#008080', fontSize: '14px', fontWeight: 'bold', cursor: 'pointer', marginTop: '0.5rem' }}>
        {title !== '' ? (isOpen ? '- ' : '+ ') : ''}
        {title}
      </div>
      {isOpen ? children : null}
      {React.Children.count(children) === 0 && isOpen ? resources.messages['emptyDatasetDesign'] : null}
    </React.Fragment>
  );
};

export { TreeViewExpandableItem };
