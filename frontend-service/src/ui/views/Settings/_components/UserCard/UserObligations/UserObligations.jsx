import React, { useState, useContext } from 'react';

import styles from './UserObligations.module.scss';

import { Button } from 'ui/views/_components/Button';

import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const UserObligations = () => {
  const resources = useContext(ResourcesContext);

  const [isUserObligationsCollapse, setIsUserObligationsCollapse] = useState(true);

  console.log(`isUserObligationsCollapse ${isUserObligationsCollapse}`);

  const dataflow = 'dataflow';

  return (
    <div className={styles.userObligationsContainer}>
      <div className={styles.userObligationsTitle}>Obligations</div>

      {/* <div>
        <span>Dataflow 1</span>
        <Button
          className={`p-button-secondary-transparent ${styles.orderIcon}`}
          icon={isUserObligationsCollapse === true ? 'angleRight' : 'angleDown'}
          onClick={() => setIsUserObligationsCollapse(!isUserObligationsCollapse)}
          tooltip={
            isUserObligationsCollapse === true ? resources.messages['expandAll'] : resources.messages['collapseAll']
          }
          tooltipOptions={{ position: 'bottom' }}
        />
      </div> */}

      <TreeViewExpandableItem
        className={styles.obligationsExpandable}
        expanded={false}
        items={[{ label: dataflow }, { label: dataflow }]}
        children={['dataflow1 ', 'dataflow2 ']}
      />
    </div>
  );
};

export { UserObligations };
