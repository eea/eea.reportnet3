import React, { useContext, useEffect, useState } from 'react';

import styles from './ContributorsList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Contributor } from './_components/Contributor';

import { ContributorService } from 'core/services/Contributor';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export function ContributorsList({ dataFlowId }) {
  const resources = useContext(ResourcesContext);
  const [contributorsArray, setContributorsArray] = useState([]);

  const onLoadContributorsList = async () => {
    setContributorsArray(await ContributorService.all(dataFlowId));
  };

  useEffect(() => {
    //TODO HERE SHOULD BE CALLED THE CONTRIBUTORS GET ALL USING DATAFLOW ID
    /* const contributorsArray = [
      { id: '1111', login: 'ygryc@ygryc.net', role: 'read_write' },
      { id: '2222', login: 'igor@igor.com', role: 'read' }
    ];
    setContributorsArray(contributorsArray); */
    onLoadContributorsList();
  }, []);

  return (
    <div>
      <ul className={styles.listContainer}>
        {contributorsArray.map(contributor => (
          <Contributor contributorData={contributor} key={contributor.id} />
        ))}
      </ul>
      <Button
        icon="plus"
        tooltip={resources.messages.addContributor}
        label={resources.messages.add}
        className={`${styles.addContributorButton} rp-btn default`}
      />
    </div>
  );
}
