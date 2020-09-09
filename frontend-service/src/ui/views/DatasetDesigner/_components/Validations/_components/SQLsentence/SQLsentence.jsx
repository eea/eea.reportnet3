import React, { useContext, useState } from 'react';

import styles from './SQLsentence.module.scss';

import { InputTextarea } from 'primereact/inputtextarea';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SQLsentence = ({}) => {
  const resources = useContext(ResourcesContext);

  const [sentence, setSentence] = useState('');

  return (
    <React.Fragment>
      <div className={styles.section}>
        <h3>SQL sentence:</h3>

        <textarea
          cols={30}
          id="SQLsentenceTextarea"
          name=""
          onChange={e => setSentence(e.target.value)}
          rows={10}
          value={sentence}
        />
      </div>
    </React.Fragment>
  );
};
