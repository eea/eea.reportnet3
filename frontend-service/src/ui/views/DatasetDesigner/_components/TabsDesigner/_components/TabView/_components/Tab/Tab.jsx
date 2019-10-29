import React, { useState, useRef, useEffect, useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './Tab.module.css';

import classNames from 'classnames';

import { Icon } from 'ui/views/_components/Icon';
import { InputText } from 'ui/views/_components/InputText';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const Tab = ({
  addTab,
  ariaControls,
  checkEditingTabs,
  className,
  closeIcon,
  editable = true,
  header,
  headerStyle,
  id,
  index,
  leftIcon,
  newTab,
  onTabBlur,
  onTabAddCancel,
  onTabDeleteClick,
  onTabDragAndDrop,
  onTabEditingHeader,
  onTabHeaderClick,
  onTabNameError,
  rightIcon,
  scrollTo,
  selected
}) => {
  const [editingHeader, setEditingHeader] = useState(!isUndefined(newTab) ? newTab : false);
  const [titleHeader, setTitleHeader] = useState(!isUndefined(addTab) ? '' : header);
  const [initialTitleHeader, setInitialTitleHeader] = useState(!isUndefined(addTab) ? '' : header);
  const [iconToShow, setIconToShow] = useState(!isUndefined(closeIcon) ? closeIcon : 'cancel');
  //const [draggedTabIndex, setDraggedTabIndex] = useState(null);

  const resources = useContext(ResourcesContext);

  // const draggableTabRef = useRef();
  const tabRef = useRef();

  useEffect(() => {
    setTitleHeader(titleHeader !== '' && titleHeader === header ? titleHeader : header !== '' ? header : titleHeader);
    setInitialTitleHeader(header);
  }, [onTabBlur]);

  useEffect(() => {
    if (!isUndefined(newTab)) {
      if (newTab) {
        setEditingHeader(true);
      }
    }
  }, [newTab]);

  // const onTabDragStart = () => {
  //   console.log('DragStart: ', index);
  //   setDraggedTabIndex(index);
  // };

  // const onTabDragEnd = event => {
  //   console.log(draggedTabIndex, event.currentTarget, event.target);
  // };

  // const onTabDragEnter = () => {
  //   setDragging(true);
  // };

  // const onTabDragLeave = () => {
  //   setDragging(false);
  // };

  // const onTabDrop = event => {
  //   //Get the dragged tab header
  //   const dataString = event.dataTransfer.getData('text/html');
  //   const range = document.createRange();
  //   const draggedTabHeader = range.createContextualFragment(dataString).childNodes[0].innerText;
  //   //currentTarget gets the child's target parent
  //   const childs = event.currentTarget.childNodes;
  //   for (let i = 0; i < childs.length; i++) {
  //     if (childs[i].nodeName === 'SPAN') {
  //       if (!isUndefined(onTabDragAndDrop)) {
  //         onTabDragAndDrop(draggedTabHeader, childs[i].textContent);
  //       }
  //     }
  //     // console.log(childs[i]);
  //   }
  //   console.log(event.currentTarget);
  // };

  const onKeyChange = (event, index) => {
    if (event.key === 'Escape') {
      if (newTab) {
        onTabAddCancel();
      } else {
        setTitleHeader(initialTitleHeader);
        setEditingHeader(false);
      }
    }
    if (event.key === 'Enter') {
      if (titleHeader !== '') {
        onInputBlur(event.target.value, index, initialTitleHeader);
      } else {
        if (!isUndefined(onTabNameError)) {
          onTabNameError(resources.messages['emptyTabHeader'], resources.messages['emptyTabHeaderError']);
          setEditingHeader(true);
          document.getElementsByClassName('p-inputtext p-component')[0].focus();
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
      } else {
        setEditingHeader(true);
        //Set focus on input if the name is empty
        document.getElementsByClassName('p-inputtext p-component')[0].focus();
      }
    } else {
      setEditingHeader(false);
    }
  };

  const onTabDoubleClick = () => {
    if (editable) {
      setEditingHeader(true);
      onTabEditingHeader(true);
    }
  };

  return (
    <li
      className={className}
      role="presentation"
      style={headerStyle}
      // style={{ pointerEvents: 'fill' }}
      ref={tabRef}
      tabIndex={index}>
      <a
        // draggable={!addTab ? true : false}
        aria-controls={ariaControls}
        aria-selected={selected}
        className={!addTab ? styles.p_tabview_design : null}
        href={'#' + ariaControls}
        id={id}
        onMouseDownCapture={e => {
          if (e.button == 1) {
            if (!isUndefined(checkEditingTabs)) {
              if (!checkEditingTabs()) {
                if (!isUndefined(onTabDeleteClick) && !addTab) {
                  onTabDeleteClick(index);
                }
              }
            }
          }
        }}
        onClick={e => {
          onTabHeaderClick(e);
          scrollTo(tabRef.current.offsetLeft - 20, 0);
          // console.log(tabRef);
          // console.log(
          //   tabRef.current.clientWidth,
          //   tabRef.current.offsetLeft,
          //   tabRef.current.getBoundingClientRect().right
          // );
          // if (tabRef.current.getBoundingClientRect().right + tabRef.current.clientWidth >= divTabWidth) {
          //   navigationHidden(false);
          // }
        }}
        // onDragEnd={e => {
        //   onTabDragEnd(e);
        // }}
        // onDragEnter={onTabDragEnter}
        // onDragLeave={onTabDragLeave}
        // onDragStart={onTabDragStart}
        // onDrop={e => onTabDrop(e)}
        onDoubleClick={onTabDoubleClick}
        role="tab"
        style={{ pointerEvents: 'fill', display: 'inline-block' }}
        tabIndex={index}>
        {leftIcon && <span className={classNames('p-tabview-left-icon ', leftIcon)}></span>}
        {!isUndefined(editingHeader) && editingHeader ? (
          <InputText
            autoFocus={true}
            key={index}
            className={styles.p_tabview_input_design}
            onBlur={e => {
              //Check for empty table name
              if (titleHeader !== '') {
                onInputBlur(e.target.value, index, initialTitleHeader);
              } else {
                if (!isUndefined(onTabNameError)) {
                  onTabNameError(resources.messages['emptyTabHeader'], resources.messages['emptyTabHeaderError']);
                  setEditingHeader(true);
                  document.getElementsByClassName('p-inputtext p-component')[0].focus();
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
        {rightIcon && <span className={classNames('p-tabview-right-icon ', rightIcon)}></span>}
        <div
          onClick={e => {
            e.preventDefault();
            if (!isUndefined(checkEditingTabs)) {
              if (!checkEditingTabs()) {
                if (!isUndefined(onTabDeleteClick)) {
                  onTabDeleteClick(index);
                }
              }
            }
          }}
          onMouseOut={e => {
            setIconToShow('cancel');
          }}
          onMouseOver={e => {
            setIconToShow('errorCircle');
          }}>
          {!addTab && !newTab ? (
            <Icon
              icon={iconToShow}
              style={{
                position: 'absolute',
                top: '33%',
                right: '6px',
                fontSize: '1rem',
                cursor: 'pointer ',
                opacity: '0.7',
                color: selected ? 'white' : null
              }}
            />
          ) : null}
        </div>
      </a>
    </li>
  );
};
