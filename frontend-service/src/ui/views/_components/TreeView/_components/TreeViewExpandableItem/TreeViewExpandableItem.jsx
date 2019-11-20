import React, { useState } from 'react';

import styles from './TreeViewExpandableItem.module.css';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';

const TreeViewExpandableItem = ({ expanded = true, title, children }) => {
  const [isOpen, setIsOpen] = useState(expanded);

  return (
    <React.Fragment>
      <div
        onClick={() => {
          setIsOpen(!isOpen);
          // growDiv();
        }}
        style={{ color: '#008080', fontSize: '14px', fontWeight: 'bold', cursor: 'pointer' }}>
        {title !== '' ? (isOpen ? '- ' : '+ ') : ''}
        {title}
      </div>
      {/* <TransitionGroup transitionName="slider"> */}
      {isOpen ? children : null}
      {/* </TransitionGroup> */}
      {React.Children.count(children) === 0 && isOpen ? 'The dataset design is empty' : null}
    </React.Fragment>
  );
};

export { TreeViewExpandableItem };
