import { Children, Fragment, useContext, useEffect, useState } from 'react';

import isUndefined from 'lodash/isUndefined';
import uniqueId from 'lodash/uniqueId';

import styles from './TreeViewExpandableItem.module.css';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const TreeViewExpandableItem = ({
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
  const resourcesContext = useContext(ResourcesContext);

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
    setIsOpen(!isUndefined(items) & (items.length > 0) ? expanded : true);
  }, [expanded]);

  const renderHeader = () => {
    const width = 90 / items.length;
    return items.map((item, i) =>
      !isUndefined(item.type) && item.type === 'box' ? (
        <div
          className={styles.defaultHeaderItem}
          key={uniqueId()}
          style={{
            width: `${width}%`
          }}>
          <span className={item.className}>{item.label}</span>
        </div>
      ) : (
        <span
          className={styles.defaultHeaderItem}
          key={uniqueId()}
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
            key={uniqueId()}
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
            key={uniqueId()}
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
        className={!isUndefined(className) ? className : styles.defaultExpandable}
        onClick={() => setIsOpen(!isOpen)}>
        {!isUndefined(items) && items.length > 0 ? (
          <FontAwesomeIcon
            aria-label={isOpen ? resourcesContext.messages['collapse'] : resourcesContext.messages['expand']}
            icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
            onClick={() => setIsOpen(!isOpen)}
          />
        ) : (
          ''
        )}
        {renderHeader()}
        {renderButtons()}
        {renderInfoButtons()}
      </div>
      {isOpen ? <div className={styles.treeChildrenWrapper}>{children}</div> : null}
      {Children.count(children) === 0 && isOpen && !isUndefined(items[0]) ? (
        <span
          className={
            styles.emptyProperty
          }>{`${resourcesContext.messages['emptyDatasetDesign']} ${items[0].label}`}</span>
      ) : null}
    </Fragment>
  );
};

export { TreeViewExpandableItem };
