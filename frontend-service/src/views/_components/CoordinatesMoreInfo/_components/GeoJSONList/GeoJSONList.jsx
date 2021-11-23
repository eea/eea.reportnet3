import { useContext } from 'react';
import uniqueId from 'lodash/uniqueId';

import styles from './GeoJSONList.module.scss';

import { GeoJSONLine } from './_components/GeoJSONLine/GeoJSONLine';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const GeoJSONList = ({ geoJSON }) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div>
      <label className={styles.geoJSONTitle}>{resourcesContext.messages['geoJSON']}</label>
      <pre className={styles.geoJSON}>
        {geoJSON.split('\n').map((token, i) => (
          <GeoJSONLine key={uniqueId('geoJSONLine_')} line={token} lineNumber={i} />
        ))}
      </pre>
    </div>
  );
};
