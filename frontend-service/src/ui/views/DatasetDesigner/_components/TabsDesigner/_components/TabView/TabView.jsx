import React, { useEffect, useState, useContext, useRef } from 'react';
import { isUndefined, isNull } from 'lodash';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import UniqueComponentId from 'ui/UniqueComponentId';

import styles from './TabView.module.css';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Tab } from './_components/Tab';

export const TabView = ({
  activeIndex = 0,
  checkEditingTabs,
  children,
  className = null,
  id = null,
  isErrorDialogVisible,
  onTabAdd,
  onTabBlur,
  onTabDragAndDrop,
  onTabAddCancel,
  onTabConfirmDelete,
  onTabChange = null,
  onTabEditingHeader,
  onTabNameError,
  onTabClick,
  renderActiveOnly = true,
  style = null
}) => {
  const [activeIdx, setActiveIdx] = useState(activeIndex);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [idx] = useState(id || UniqueComponentId());
  const [idxToDelete, setidxToDelete] = useState(null);
  const [isNavigationHidden, setIsNavigationHidden] = useState(true);

  const divTabsRef = useRef();
  const ulTabsRef = useRef();
  const resources = useContext(ResourcesContext);

  const classNamed = classNames('p-tabview p-component p-tabview-top', className);

  useEffect(() => {
    setTimeout(() => {
      if (ulTabsRef.current.clientWidth >= divTabsRef.current.clientWidth) {
        setIsNavigationHidden(false);
      }
    }, 100);
  }, [children]);

  const onTabHeaderClick = (event, tab, index) => {
    if (tab.props.addTab) {
      onTabAdd({ header: '', editable: true, addTab: false, newTab: true }, () => {
        scrollTo(ulTabsRef.current.clientWidth + 100, 0);
      });
    } else {
      if (!tab.props.disabled) {
        if (!isUndefined(onTabClick) && !isNull(onTabClick)) {
          onTabClick({ originalEvent: event, index: index });
        }
        if (!isUndefined(onTabChange) && !isNull(onTabChange)) {
          onTabChange({ originalEvent: event, index: index });
        } else {
          setActiveIdx(index);
        }
      }
    }

    event.preventDefault();
  };

  const onTabDeleteClicked = deleteIndex => {
    setidxToDelete(deleteIndex);
    setDeleteDialogVisible(true);
  };

  const createContent = (tab, index) => {
    const selected = isSelected(index);
    const className = classNames(tab.props.contentClassName, 'p-tabview-panel', { 'p-hidden': !selected });
    const id = `${idx}_content_${index}`;
    const ariaLabelledBy = `${idx}_header_${index}`;

    return (
      <div
        id={id}
        aria-labelledby={ariaLabelledBy}
        aria-hidden={!selected}
        className={className}
        style={tab.props.contentStyle}
        role="tabpanel"
        key={id}>
        {!renderActiveOnly ? tab.props.children : selected && tab.props.children}
      </div>
    );
  };

  const isSelected = index => {
    const actIndex = !isUndefined(onTabChange) && !isNull(onTabChange) ? activeIndex : activeIdx;
    return actIndex === index;
  };

  const renderTabHeader = (tab, index) => {
    const selected = isSelected(index);
    const className = classNames(tab.props.headerClassName, 'p-unselectable-text', {
      'p-tabview-selected p-highlight': selected,
      'p-disabled': tab.props.disabled
    });
    const id = `${idx}_header_${index}`;
    const ariaControls = `${idx}_content_${index}`;
    return (
      <Tab
        addTab={tab.props.addTab}
        ariaControls={ariaControls}
        checkEditingTabs={checkEditingTabs}
        children={tab.props.children}
        className={className}
        editable={tab.props.editable}
        header={tab.props.header}
        headerStyle={tab.props.headerStyle}
        id={id}
        index={index}
        key={id}
        leftIcon={tab.props.leftIcon}
        newTab={tab.props.newTab}
        onTabBlur={onTabBlur}
        onTabAddCancel={onTabAddCancel}
        onTabDeleteClick={onTabDeleteClicked}
        onTabDragAndDrop={onTabDragAndDrop}
        onTabHeaderClick={event => {
          onTabHeaderClick(event, tab, index);
          onTabEditingHeader(false);
        }}
        onTabEditingHeader={onTabEditingHeader}
        onTabNameError={onTabNameError}
        rightIcon={tab.props.rightIcon}
        selected={selected}
        scrollTo={scrollTo}
      />
    );
  };

  const renderTabHeaders = () => {
    return React.Children.map(children, (tab, index) => {
      return renderTabHeader(tab, index);
    });
  };

  const renderNavigator = () => {
    const headers = renderTabHeaders();
    return (
      <div className={styles.scrollTab} ref={divTabsRef}>
        <Icon
          className={`${styles.stepInitIcon} ${styles.navigationTabIcons} ${
            isNavigationHidden ? styles.iconHidden : null
          }`}
          icon={'stepBackward'}
          onClick={() => {
            scrollTo(0, 0);
          }}
        />
        <Icon
          className={`${styles.stepBackwardIcon} ${styles.navigationTabIcons} ${
            isNavigationHidden ? styles.iconHidden : null
          }`}
          icon={'caretLeft'}
          onClick={e => {
            scrollTo(divTabsRef.current.scrollLeft - divTabsRef.current.clientWidth * 0.75, 0);
          }}
        />
        <ul className="p-tabview-nav p-reset" role="tablist" style={{ display: 'inline-flex' }} ref={ulTabsRef}>
          {headers}
        </ul>
        <Icon
          className={`${styles.stepForwardIcon} ${styles.navigationTabIcons} ${
            isNavigationHidden ? styles.iconHidden : null
          }`}
          icon={'caretRight'}
          onClick={() => {
            scrollTo(divTabsRef.current.scrollLeft + divTabsRef.current.clientWidth * 0.75, 0);
          }}
        />
        <Icon
          className={`${styles.stepEndIcon} ${styles.navigationTabIcons} ${
            isNavigationHidden ? styles.iconHidden : null
          }`}
          icon={'stepForward'}
          onClick={() => {
            scrollTo(ulTabsRef.current.clientWidth + 100, 0);
          }}
        />
      </div>
    );
  };

  const renderContent = () => {
    const contents = React.Children.map(children, (tab, index) => {
      if (!renderActiveOnly || isSelected(index)) {
        return createContent(tab, index);
      }
    });
    return <div className="p-tabview-panels">{contents}</div>;
  };

  const renderConfirmDialog = () => {
    return (
      <ConfirmDialog
        header={resources.messages['deleteTabHeader']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => {
          onTabConfirmDelete(idxToDelete);
          setDeleteDialogVisible(false);
        }}
        onHide={() => setDeleteDialogVisible(false)}
        visible={deleteDialogVisible}>
        {resources.messages['deleteTabConfirm']}
      </ConfirmDialog>
    );
  };

  const scrollTo = (xCoordinate, yCoordinate) => {
    divTabsRef.current.scrollTo(xCoordinate, yCoordinate);
    //Await for scroll
    setTimeout(() => {
      if (ulTabsRef.current.clientWidth >= divTabsRef.current.clientWidth) {
        if (isNavigationHidden) {
          setIsNavigationHidden(false);
        }
      } else {
        if (!isNavigationHidden) {
          setIsNavigationHidden(true);
        }
      }
    }, 100);
  };

  return (
    <div id={id} className={classNamed} style={style}>
      {renderNavigator()}
      {renderContent()}
      {!isErrorDialogVisible ? renderConfirmDialog() : null}
    </div>
  );
};
TabView.propTypes = {
  activeIndex: PropTypes.number,
  checkEditingTabs: PropTypes.func,
  className: PropTypes.string,
  id: PropTypes.string,
  onTabAdd: PropTypes.func,
  onTabAddCancel: PropTypes.func,
  onTabChange: PropTypes.func,
  onTabConfirmDelete: PropTypes.func,
  onTabDragAndDrop: PropTypes.func,
  onTabClick: PropTypes.func,
  onTabNameError: PropTypes.func,
  renderActiveOnly: PropTypes.bool,
  style: PropTypes.object
};
