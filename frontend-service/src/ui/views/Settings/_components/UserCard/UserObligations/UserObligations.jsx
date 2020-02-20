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

  const arr = [{ item: 'item 1' }, { item: 'item2' }, { item: 'item4' }];
  const renderDataFlows = () => {
    return arr.map((e, i) => (
      <TreeViewExpandableItem
        className={styles.obligationsExpandable}
        expanded={false}
        key={i}
        items={[e.item]}
        children={['dataflow1  ']}>
        <h1>{e.item}</h1>
      </TreeViewExpandableItem>
    ));
  };

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
      {renderDataFlows()}
    </div>
  );
};

export { UserObligations };
