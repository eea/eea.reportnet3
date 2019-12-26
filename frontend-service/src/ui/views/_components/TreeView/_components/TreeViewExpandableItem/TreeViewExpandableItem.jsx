import React, { useContext, useState } from 'react';

import { isUndefined } from 'lodash';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

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
        {title !== '' ? (
          isOpen ? (
            <FontAwesomeIcon icon={AwesomeIcons('minusSquare')} />
          ) : (
            <FontAwesomeIcon icon={AwesomeIcons('plusSquare')} />
          )
        ) : (
          ''
        )}
        {!isUndefined(title) ? title : null}
      </div>
      {isOpen ? children : null}
      {React.Children.count(children) === 0 && isOpen ? resources.messages['emptyDatasetDesign'] : null}
    </React.Fragment>
  );
};

export { TreeViewExpandableItem };
