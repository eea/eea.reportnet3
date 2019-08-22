import React, { useContext } from 'react';

import styles from './ContributorsList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Contributor } from './_components/Contributor';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export function ContributorsList({
  contributorsArray = [
    { id: '1111', login: 'ygryc@ygryc.net', role: 'read_write' },
    { id: '2222', login: 'igor@igor.com', role: 'read' }
  ]
}) {
  const resources = useContext(ResourcesContext);
  return (
    <div>
      <ul className={styles.listContainer}>
        {contributorsArray.map(contributor => (
          <Contributor contributorData={contributor} key={contributor.id} />
        ))}
      </ul>
      <Button
        icon="plus"
        label={resources.messages.addContributor}
        className={`${styles.addContributorButton} rp-btn default`}
      />
    </div>
  );
}
