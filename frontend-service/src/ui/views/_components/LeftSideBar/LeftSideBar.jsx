import React, { useState, useContext } from 'react';
import { withRouter, Link } from 'react-router-dom';

import styles from './LeftSideBar.module.css';

import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Icon } from 'ui/views/_components/Icon';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const LeftSideBar = withRouter(
  ({
    components = [],
    createDataflowButtonTitle,
    isCustodian,
    navTitle,
    onShowAddForm,
    onToggleSideBar,
    style,
    subscribeButtonTitle
  }) => {
    const resources = useContext(ResourcesContext);

    const [subscribeDialogVisible, setSubscribeDialogVisible] = useState(false);

    const setVisibleHandler = (fnUseState, visible) => {
      fnUseState(visible);
    };

    const onConfirmSubscribeHandler = () => {
      setSubscribeDialogVisible(false);
    };

    return (
      <div
        className={styles.leftSideBar}
        onMouseOver={() => onToggleSideBar(true)}
        onMouseOut={() => onToggleSideBar(false)}>
        {isCustodian && components.includes('createDataflow') ? (
          <React.Fragment>
            <a href="#">
              <div className={styles.leftSideBarElementWrapper} onClick={() => onShowAddForm()}>
                <Icon icon="plus" className={styles.leftSideBarElementAnimation} />
                <span className={styles.leftSideBarText}>{createDataflowButtonTitle}</span>
              </div>
            </a>
            <Link to={getUrl(routes.CODELISTS, {}, true)}>
              <div className={styles.leftSideBarElementWrapper}>
                <Icon icon="settings" className={styles.leftSideBarElementAnimation} />
                <span className={styles.leftSideBarText}>{resources.messages['manageCodelists']}</span>
              </div>
            </Link>
          </React.Fragment>
        ) : null}
      </div>

      // <div className="nav rep-col-12 rep-col-xl-2">
      //   <h2 className={styles.title}>{navTitle}</h2>
      //   {components.includes('search') && (
      //     <div className="navSection">
      //       <input
      //         className={styles.searchInput}
      //         id=""
      //         placeholder={resources.messages['searchDataflow']}
      //         type="text"
      //         disabled
      //       />
      //     </div>
      //   )}
      //   <div className="navSection">
      //     {isCustodian && components.includes('createDataflow') ? (
      //       <React.Fragment>
      //         <Button
      //           className={`${styles.columnButton} p-button-primary`}
      //           icon="plus"
      //           label={createDataflowButtonTitle}
      //           onClick={() => onShowAddForm()}
      //           style={{ textAlign: 'left' }}
      //         />
      //         <Link to={getUrl(routes.CODELISTS, {}, true)}>
      //           <Button
      //             className={styles.columnButton}
      //             icon="plus"
      //             label={resources.messages['manageCodelists']}
      //             style={style}
      //           />
      //         </Link>
      //       </React.Fragment>
      //     ) : null}

      //     {/* <Button
      //       className={styles.columnButton}
      //       icon="plus"
      //       label={subscribeButtonTitle}
      //       onClick={() => {
      //         setVisibleHandler(setSubscribeDialogVisible, true);
      //       }}
      //       style={style}
      //       disabled
      //     />           */}
      //   </div>

      //   <ConfirmDialog
      //     header={resources.messages['subscribeButtonTitle']}
      //     maximizable={false}
      //     labelCancel={resources.messages['close']}
      //     labelConfirm={resources.messages['yes']}
      //     onConfirm={onConfirmSubscribeHandler}
      //     onHide={() => setVisibleHandler(setSubscribeDialogVisible, false)}
      //     visible={subscribeDialogVisible}>
      //     {resources.messages['subscribeDataflow']}
      //   </ConfirmDialog>
      // </div>
    );
  }
);

export { LeftSideBar };
