import React, { useEffect, useState, useContext, useRef } from 'react';
import { isUndefined, isNull } from 'lodash';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import UniqueComponentId from 'ui/views/_functions/PrimeReact/UniqueComponentId';

import styles from './TabView.module.css';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Tab } from './_components/Tab';

export const TabView = ({
  activeIndex = -1,
  checkEditingTabs,
  children,
  className = null,
  designMode = false,
  id = null,
  initialTabIndexDrag,
  isErrorDialogVisible,
  onTabAdd,
  onTabBlur,
  onTabDragAndDrop,
  onTabDragAndDropStart,
  onTabAddCancel,
  onTabConfirmDelete,
  onTabChange = null,
  onTabEditingHeader,
  onTabNameError,
  onTabClick,
  renderActiveOnly = true,
  style = null,
  totalTabs
}) => {
  const [activeIdx, setActiveIdx] = useState(activeIndex);
  const [idx] = useState(id || UniqueComponentId());
  const [idxToDelete, setIdxToDelete] = useState(null);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isNavigationHidden, setIsNavigationHidden] = useState(true);

  const divTabsRef = useRef();
  const ulTabsRef = useRef();
  const resources = useContext(ResourcesContext);

  const classNamed = classNames('p-tabview p-component p-tabview-top', className);
  useEffect(() => {
    setTimeout(() => {
      if (
        !isUndefined(ulTabsRef.current) &&
        !isNull(ulTabsRef.current) &&
        !isUndefined(divTabsRef.current) &&
        !isNull(divTabsRef.current)
      ) {
        if (ulTabsRef.current.clientWidth > divTabsRef.current.clientWidth) {
          setIsNavigationHidden(false);
        }
      }
    }, 100);
  }, [children]);

  useEffect(() => {
    setActiveIdx(activeIndex);
  }, [activeIndex]);

  const onTabHeaderClick = (event, tab, index) => {
    if (designMode) {
      if (tab.props.addTab) {
        onTabAdd({ header: '', editable: true, addTab: false, newTab: true }, () => {
          if (!isUndefined(ulTabsRef.current) && !isNull(ulTabsRef.current)) {
            scrollTo(ulTabsRef.current.clientWidth + 100, 0);
          }
        });
      } else {
        if (!tab.props.disabled) {
          if (!isUndefined(onTabClick) && !isNull(onTabClick)) {
            onTabClick({ originalEvent: event, index: index, header: tab.props.header });
          }
          if (!isUndefined(onTabChange) && !isNull(onTabChange)) {
            onTabChange({ originalEvent: event, index: index });
          } else {
            if (!isUndefined(onTabClick) && !isNull(onTabClick)) {
              onTabClick({ originalEvent: event, index: index, header: tab.props.header });
            }
          }
        }
      }
    } else {
      setActiveIdx(index);
    }
    event.preventDefault();
  };

  const onTabDeleteClicked = deleteIndex => {
    setIdxToDelete(deleteIndex);
    setIsDeleteDialogVisible(true);
  };

  const onTabMouseWheel = deltaY => {
    if (deltaY > 0) {
      scrollTo(divTabsRef.current.scrollLeft - divTabsRef.current.clientWidth * 0.75, 0);
    } else {
      scrollTo(divTabsRef.current.scrollLeft + divTabsRef.current.clientWidth * 0.75, 0);
    }
  };

  const createContent = (tab, index) => {
    const selected = isSelected(index);
    const className = classNames(tab.props.contentClassName, 'p-tabview-panel', { 'p-hidden': !selected });
    const id = `${idx}_content_${index}`;
    const ariaLabelledBy = `${idx}_header_${index}`;

    return (
      <div
        aria-hidden={!selected}
        aria-labelledby={ariaLabelledBy}
        className={className}
        key={id}
        id={id}
        role="tabpanel"
        style={tab.props.contentStyle}>
        {!renderActiveOnly ? tab.props.children : selected && tab.props.children}
      </div>
    );
  };

  const isSelected = index => {
    if (designMode) {
      return activeIndex === index;
    } else {
      return activeIdx === index;
    }
  };

  const renderTabHeader = (tab, index) => {
    const selected = isSelected(index);
    const className = classNames(tab.props.headerClassName, 'p-unselectable-text', {
      'p-tabview-selected p-highlight': selected,
      'p-disabled': tab.props.disabled
    });
    const id = `${idx}_header_${index}`;
    const ariaControls = `${idx}_content_${index}`;
    if (!isUndefined(divTabsRef.current) && !isNull(divTabsRef.current)) {
    }
    return (
      <Tab
        addTab={tab.props.addTab}
        ariaControls={ariaControls}
        checkEditingTabs={checkEditingTabs}
        children={tab.props.children}
        className={className}
        disabled={tab.props.disabled}
        editable={tab.props.editable}
        designMode={designMode}
        divScrollTabsRef={divTabsRef.current}
        header={tab.props.header}
        headerStyle={tab.props.headerStyle}
        id={id}
        index={index}
        initialTabIndexDrag={initialTabIndexDrag}
        key={id}
        leftIcon={tab.props.leftIcon}
        newTab={tab.props.newTab}
        onTabBlur={onTabBlur}
        onTabAddCancel={onTabAddCancel}
        onTabDeleteClick={onTabDeleteClicked}
        onTabDragAndDrop={onTabDragAndDrop}
        onTabDragAndDropStart={onTabDragAndDropStart}
        onTabHeaderClick={event => {
          onTabHeaderClick(event, tab, index);
          if (!isUndefined(onTabEditingHeader)) {
            onTabEditingHeader(false);
          }
        }}
        onTabEditingHeader={onTabEditingHeader}
        onTabMouseWheel={onTabMouseWheel}
        onTabNameError={onTabNameError}
        rightIcon={tab.props.rightIcon}
        scrollTo={scrollTo}
        selected={selected}
        totalTabs={totalTabs}
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
      <div className={styles.headersWrapper}>
        <Icon
          className={`${styles.navigationTabIcons} ${isNavigationHidden ? styles.iconHidden : null}`}
          icon={'stepBackward'}
          onClick={() => {
            scrollTo(0, 0);
          }}
        />
        <Icon
          className={`${styles.navigationTabIcons} ${isNavigationHidden ? styles.iconHidden : null}`}
          icon={'caretLeft'}
          onClick={e => {
            scrollTo(divTabsRef.current.scrollLeft - divTabsRef.current.clientWidth * 0.75, 0);
          }}
        />
        <div className={styles.scrollTab} ref={divTabsRef}>
          {/* <div className={styles.iconsWrapper}> */}

          {/* </div> */}
          <ul className="p-tabview-nav p-reset" role="tablist" style={{ display: 'inline-flex' }} ref={ulTabsRef}>
            {headers}
          </ul>
        </div>
        <Icon
          className={`${styles.navigationTabIcons} ${isNavigationHidden ? styles.iconHidden : null}`}
          icon={'caretRight'}
          onClick={() => {
            scrollTo(divTabsRef.current.scrollLeft + divTabsRef.current.clientWidth * 0.75, 0);
          }}
        />
        <Icon
          className={`${styles.navigationTabIcons} ${isNavigationHidden ? styles.iconHidden : null}`}
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
          setIsDeleteDialogVisible(false);
        }}
        onHide={() => setIsDeleteDialogVisible(false)}
        visible={isDeleteDialogVisible}>
        {resources.messages['deleteTabConfirm']}
      </ConfirmDialog>
    );
  };

  const scrollTo = (xCoordinate, yCoordinate) => {
    divTabsRef.current.scrollTo(xCoordinate, yCoordinate);
    //Await for scroll
    setTimeout(() => {
      if (!isUndefined(ulTabsRef.current) && !isNull(ulTabsRef.current)) {
        if (ulTabsRef.current.clientWidth > divTabsRef.current.clientWidth) {
          if (isNavigationHidden) {
            setIsNavigationHidden(false);
          }
        } else {
          if (!isNavigationHidden) {
            setIsNavigationHidden(true);
          }
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
