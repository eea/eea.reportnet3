import React, { useState, useRef, useEffect, useContext } from 'react';

import { isNil, isUndefined } from 'lodash';
import { config } from 'conf';

import styles from './Tab.module.css';

import classNames from 'classnames';

import { ContextMenu } from 'ui/views/_components/ContextMenu';
import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

const Tab = ({
  addTab,
  ariaControls,
  checkEditingTabs,
  className,
  closeIcon,
  designMode = false,
  divScrollTabsRef,
  disabled = false,
  editable = false,
  hasPKReferenced = false,
  header,
  headerStyle,
  id,
  isDataflowOpen,
  index,
  initialTabIndexDrag,
  isNavigationHidden,
  leftIcon,
  newTab,
  onTabBlur,
  onTabAddCancel,
  onTabDeleteClick,
  onTabDragAndDrop,
  onTabDragAndDropStart,
  onTabEditingHeader,
  onTabHeaderClick,
  onTabMouseWheel,
  onTabNameError,
  rightIcon,
  scrollTo,
  selected,
  tableSchemaId,
  totalTabs
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [editingHeader, setEditingHeader] = useState(!isUndefined(newTab) ? newTab : false);
  const [hasErrors, setHasErrors] = useState(false);
  const [initialTitleHeader, setInitialTitleHeader] = useState(!isUndefined(addTab) ? '' : header);
  const [iconToShow, setIconToShow] = useState(!isUndefined(closeIcon) ? closeIcon : 'cancel');
  const [menu, setMenu] = useState();
  const [titleHeader, setTitleHeader] = useState(!isUndefined(addTab) ? '' : header);

  const resources = useContext(ResourcesContext);

  let contextMenuRef = useRef();
  const tabRef = useRef();

  useEffect(() => {
    setMenu([
      {
        label: resources.messages['edit'],
        icon: config.icons['edit'],
        command: () => {
          setEditingHeader(true);
        }
      },
      {
        label: resources.messages['delete'],
        icon: config.icons['trash'],
        command: () => {
          if (!isUndefined(onTabDeleteClick) && !addTab && !hasPKReferenced) {
            onTabDeleteClick(tableSchemaId);
          }
        },
        disabled: hasPKReferenced
      }
    ]);
  }, [tableSchemaId, hasPKReferenced]);

  useEffect(() => {
    if (!editingHeader) {
      setTitleHeader(titleHeader !== '' && titleHeader === header ? titleHeader : header !== '' ? header : titleHeader);
      setInitialTitleHeader(header);
    }
    if (document.getElementsByClassName('tabInput').length > 0) {
      document.getElementsByClassName('tabInput')[0].focus();
    }
  }, [onTabBlur, hasErrors]);

  useEffect(() => {
    if (!isUndefined(newTab)) {
      if (newTab) {
        setEditingHeader(true);
      }
    }
  }, [newTab]);

  const onTabDragStart = event => {
    if (editingHeader) {
      event.preventDefault();
    } else {
      //For firefox
      event.dataTransfer.setData('text/plain', null);
      if (!isUndefined(onTabDragAndDropStart)) {
        onTabDragAndDropStart(index, tableSchemaId);
      }
    }
  };

  const onTabDragEnd = () => {
    if (!isUndefined(initialTabIndexDrag)) {
      setIsDragging(false);
      if (!isUndefined(onTabDragAndDropStart)) {
        onTabDragAndDropStart(index, tableSchemaId);
      }
    }
  };

  const onTabDragOver = event => {
    if (!isUndefined(initialTabIndexDrag)) {
      if (index !== initialTabIndexDrag && !addTab) {
        event.currentTarget.style.border = 'var(--drag-and-drop-div-border)';
        event.currentTarget.style.opacity = 'var(--drag-and-drop-div-opacity)';
      }
      if (event.currentTarget.tabIndex !== initialTabIndexDrag) {
        if (!isDragging) {
          if (
            (index === '-1' && totalTabs - initialTabIndexDrag !== 1) ||
            (index !== '-1' && initialTabIndexDrag - index !== -1)
          ) {
            setIsDragging(true);
          }
        }
      }
    }
  };

  const onTabDragLeave = event => {
    if (!isUndefined(initialTabIndexDrag)) {
      event.currentTarget.style.border = '';
      event.currentTarget.style.opacity = '';
      event.preventDefault();

      setIsDragging(false);
      if (event.currentTarget.tabIndex !== initialTabIndexDrag) {
        if (isDragging) {
          setIsDragging(false);
        }
      }
    }
  };

  const onTabDrop = event => {
    if (!isUndefined(initialTabIndexDrag)) {
      //Get the dragged tab header
      event.currentTarget.style.border = '';
      event.currentTarget.style.opacity = '';
      const dataString = event.dataTransfer.getData('text/html');
      const range = document.createRange();
      const draggedTabHeader = range.createContextualFragment(dataString).childNodes[0].innerText;
      //currentTarget gets the child's target parent
      const childs = event.currentTarget.childNodes;
      for (let i = 0; i < childs.length; i++) {
        if (childs[i].nodeName === 'SPAN' && childs[i].className === 'p-tabview-title') {
          if (!isUndefined(onTabDragAndDrop)) {
            onTabDragAndDrop(draggedTabHeader, childs[i].textContent);
            setIsDragging(false);
          }
        }
      }
    }
  };

  const onKeyChange = (event, index) => {
    if (event.key === 'Escape') {
      if (newTab) {
        onTabAddCancel();
      } else {
        setTitleHeader(initialTitleHeader);
        if (!hasErrors) {
          setEditingHeader(false);
        }
        setHasErrors(false);
      }
    }
    if (event.key === 'Enter') {
      if (titleHeader !== '') {
        onInputBlur(event.target.value, index, initialTitleHeader);
      } else {
        if (!isUndefined(onTabNameError)) {
          onTabNameError(resources.messages['emptyTabHeader'], resources.messages['emptyTitleValidationError']);
        }
      }
    }
  };

  const onInputBlur = (value, index, initialTitleHeader) => {
    const correctNameChange = onTabBlur(value, index, initialTitleHeader);
    //Check for duplicates. Undefined is also a correct value
    if (!isUndefined(correctNameChange)) {
      if (correctNameChange.correct) {
        setEditingHeader(false);
        setTitleHeader(correctNameChange.tableName);
        setHasErrors(false);
      } else {
        setEditingHeader(true);
        setHasErrors(true);
      }
    } else {
      setEditingHeader(false);
      setHasErrors(false);
    }
  };

  const onTabDoubleClick = () => {
    if (editable) {
      if (!isUndefined(onTabEditingHeader)) {
        setEditingHeader(true);
        onTabEditingHeader(true);
      }
    }
  };

  return (
    <React.Fragment>
      <div
        style={{
          display: isDragging ? 'inline' : 'none',
          left:
            !isUndefined(tabRef.current) && !isUndefined(divScrollTabsRef)
              ? `${tabRef.current.offsetLeft - divScrollTabsRef.scrollLeft - 18}px`
              : '0px',
          position: 'absolute',
          top: !isUndefined(tabRef.current) ? `${tabRef.current.offsetTop - 15}px` : '100px',
          zIndex: 9999
        }}>
        <FontAwesomeIcon className={styles.dragArrow} icon={AwesomeIcons('arrowDown')} />
      </div>
      <div
        style={{
          display: isDragging ? 'inline' : 'none',
          left:
            !isUndefined(tabRef.current) && !isUndefined(divScrollTabsRef)
              ? `${tabRef.current.offsetLeft - divScrollTabsRef.scrollLeft - 18}px`
              : '0px',
          position: 'absolute',
          top: !isUndefined(tabRef.current)
            ? `${tabRef.current.offsetTop + tabRef.current.clientHeight - 4}px`
            : '100px',
          zIndex: 9999
        }}>
        <FontAwesomeIcon className={styles.dragArrow} icon={AwesomeIcons('arrowUp')} />
        {/* <div
          style={{
            height: '40px',
            width: '30px',
            // border: '2px 2px 0 2px solid gray',
            marginRight: '3px',
            backgroundColor: 'var(--c-corporate-yellow)'
          }}></div> */}
      </div>
      <li
        className={`${className} p-tabview-nav-li datasetSchema-new-table-help-step`}
        onContextMenu={e => {
          if (designMode && !addTab) {
            const contextMenus = document.getElementsByClassName('p-contextmenu p-component');
            const inmContextMenus = [...contextMenus];
            const hideContextMenus = inmContextMenus.filter(contextMenu => contextMenu.style.display !== '');
            hideContextMenus.forEach(contextMenu => (contextMenu.style.display = 'none'));
            contextMenuRef.current.show(e);
          }
        }}
        role="presentation"
        style={{ ...headerStyle, pointerEvents: 'fill' }}
        ref={tabRef}
        tabIndex={index}>
        <a
          draggable={designMode ? (!addTab ? true : false) : false}
          aria-controls={ariaControls}
          aria-selected={selected}
          className={
            editable
              ? styles.p_tabview_design
              : addTab
              ? `${styles.p_tabview_design_add} datasetSchema-created-table-help-step`
              : styles.p_tabview_noDesign
          }
          href={'#' + ariaControls}
          id={id}
          onAuxClick={e => e.preventDefault()}
          onMouseDownCapture={e => {
            if (e.button === 1) {
              e.preventDefault();
              if (!isUndefined(checkEditingTabs)) {
                if (!checkEditingTabs()) {
                  if (!isUndefined(onTabDeleteClick) && !addTab && !hasPKReferenced) {
                    onTabDeleteClick(tableSchemaId);
                  }
                }
              }
            }
          }}
          onWheel={e => {
            const hasScrollbar = window.innerWidth > document.documentElement.clientWidth;
            if (!hasScrollbar) {
              onTabMouseWheel(e.deltaY);
            }
          }}
          onClick={e => {
            if (!disabled) {
              onTabHeaderClick(e);
              scrollTo(tabRef.current.offsetLeft - 80, 0);
            }
          }}
          onDragEnd={e => {
            onTabDragEnd(e);
          }}
          onDragOver={onTabDragOver}
          onDragLeave={onTabDragLeave}
          onDragStart={onTabDragStart}
          onDrop={e => onTabDrop(e)}
          onDoubleClick={onTabDoubleClick}
          role="tab"
          style={{
            pointerEvents: 'fill',
            display: 'inline-block',
            height: isNavigationHidden ? '2.6rem' : '2.7rem',
            minWidth: '3.6rem'
          }}
          tabIndex={index}>
          {leftIcon && <span className={classNames('p-tabview-left-icon ', leftIcon)}></span>}
          {!isUndefined(editingHeader) && editingHeader ? (
            <InputText
              autoFocus={true}
              key={index}
              className={`${styles.p_tabview_input_design} tabInput`}
              maxLength={60}
              onBlur={e => {
                //Check for empty table name
                if (titleHeader !== '') {
                  onInputBlur(e.target.value, index, initialTitleHeader);
                } else {
                  if (!isUndefined(onTabNameError)) {
                    if (!newTab) {
                      onTabNameError(
                        resources.messages['emptyTabHeader'],
                        resources.messages['emptyTitleValidationError']
                      );
                    } else {
                      onTabAddCancel();
                    }
                  }
                }
              }}
              onChange={e => setTitleHeader(e.target.value)}
              onKeyDown={e => onKeyChange(e, index)}
              placeholder={resources.messages['newTablePlaceHolder']}
              value={!isUndefined(titleHeader) ? titleHeader : header}></InputText>
          ) : (
            <span className="p-tabview-title">{!isUndefined(titleHeader) ? titleHeader : header}</span>
          )}
          {rightIcon && !editingHeader && <span className={classNames('p-tabview-right-icon ', rightIcon)}></span>}
          {designMode && !hasPKReferenced && !isDataflowOpen ? (
            <div
              onClick={e => {
                e.preventDefault();
                e.stopPropagation();
                if (!isUndefined(checkEditingTabs)) {
                  if (!checkEditingTabs()) {
                    if (!isUndefined(onTabDeleteClick)) {
                      onTabDeleteClick(tableSchemaId);
                    }
                  }
                }
              }}
              onMouseOut={() => setIconToShow('cancel')}
              onMouseOver={() => setIconToShow('errorCircle')}>
              {!addTab && !newTab ? (
                <Icon
                  icon={iconToShow}
                  style={{
                    position: 'absolute',
                    top: '33%',
                    right: '6px',
                    fontSize: '1rem',
                    cursor: 'pointer ',
                    opacity: '0.7'
                  }}
                />
              ) : null}
            </div>
          ) : null}
        </a>
      </li>
      {designMode ? <ContextMenu model={menu} ref={contextMenuRef} /> : null}
    </React.Fragment>
  );
};

export { Tab };
