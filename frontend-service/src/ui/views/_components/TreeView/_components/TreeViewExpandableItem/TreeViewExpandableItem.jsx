import { Children, Fragment, useContext, useEffect, useState } from 'react';

import { isUndefined } from 'lodash';

import styles from './TreeViewExpandableItem.module.css';

// import { TransitionGroup, CSSTransition } from 'react-transition-group';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TreeViewExpandableItem = ({
  blockExpand = false,
  buttons,
  children,
  expanded = true,
  infoButtons,
  items,
  className,
  onCollapseTree,
  onExpandTree
}) => {
  const [isOpen, setIsOpen] = useState(expanded);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    if (isOpen) {
      if (!isUndefined(onExpandTree)) {
        onExpandTree();
      }
    } else {
      if (!isUndefined(onCollapseTree)) {
        onCollapseTree();
      }
    }
  }, [isOpen]);

  useEffect(() => {
    setIsOpen(expanded);
  }, [expanded]);

  const renderHeader = () => {
    const width = 90 / items.length;
    return items.map((item, i) =>
      !isUndefined(item.type) && item.type === 'box' ? (
        <div
          className={styles.defaultHeaderItem}
          key={i}
          style={{
            width: `${width}%`
          }}>
          <span className={item.className}>{item.label}</span>
        </div>
      ) : (
        <span
          className={styles.defaultHeaderItem}
          key={i}
          style={{
            width: `${width}%`
          }}>
          {item.label}
        </span>
      )
    );
  };

  const renderButtons = () => {
    return !isUndefined(buttons)
      ? buttons.map((button, i) => (
          <Button
            className={`${button.className} ${
              !isUndefined(button.disabled) && button.disabled
                ? styles.defaultExpandableButtonDisable
                : styles.defaultExpandableButtonEnable
            } ${!isUndefined(button.iconSlashed) && button.iconSlashed ? styles.slashSpan : null}`}
            disabled={!isUndefined(button.disabled) ? button.disabled : false}
            icon={button.icon}
            key={i}
            label={button.label}
            onClick={button.onClick}
            onMouseDown={button.onMouseDown}
            style={{ marginLeft: '0.5rem' }}
            tooltip={button.tooltip}
            tooltipOptions={{ position: 'bottom' }}
            visible={!isUndefined(button.visible) ? button.visible : true}
          />
        ))
      : null;
  };

  const renderInfoButtons = () => {
    return !isUndefined(infoButtons)
      ? infoButtons.map((button, i) => (
          <Button
            className={`${button.className} ${styles.defaultInfoButton}`}
            icon={button.icon}
            key={i}
            label={button.label}
            onClick={() => setIsOpen(true)}
            style={{ ...button.style, marginLeft: '0.5rem' }}
            tooltip={button.tooltip}
            tooltipOptions={{ position: 'bottom' }}
          />
        ))
      : null;
  };

  return (
    <Fragment>
      <div
        style={{
          cursor: 'pointer',
          color: blockExpand ? 'var(--errors)' : 'inherit',
          opacity: blockExpand ? '0.7' : '1'
        }}
        className={!isUndefined(className) ? className : styles.defaultExpandable}
        onClick={!blockExpand ? () => setIsOpen(!isOpen) : null}>
        {!isUndefined(items) & (items.length > 0) ? (
          <FontAwesomeIcon
            icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
            style={{
              cursor: 'pointer',
              color: blockExpand ? 'var(--errors)' : 'inherit',
              opacity: blockExpand ? '0.7' : '1'
            }}
            onClick={!blockExpand ? () => setIsOpen(!isOpen) : null}
          />
        ) : (
          ''
        )}
        {renderHeader()}
        {renderButtons()}
        {renderInfoButtons()}
      </div>
      {/* <div className={styles.treeChildrenWrapper}> */}
      {isOpen ? <div className={styles.treeChildrenWrapper}>{children}</div> : null}
      {Children.count(children) === 0 && isOpen && !isUndefined(items[0]) ? (
        <span className={styles.emptyProperty}>{`${resources.messages['emptyDatasetDesign']} ${items[0].label}`}</span>
      ) : null}
      {/* </div> */}
    </Fragment>
  );
};

export { TreeViewExpandableItem };
