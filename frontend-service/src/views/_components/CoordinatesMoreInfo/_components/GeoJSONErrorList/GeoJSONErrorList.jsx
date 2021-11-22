import { useContext } from 'react';
import geojsonhint from '@mapbox/geojsonhint';

import uniqueId from 'lodash/uniqueId';

import styles from './GeoJSONErrorList.module.scss';

import { GeoJSONError } from './_components/GeoJSONError';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const GeoJSONErrorList = ({ geoJSON }) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div>
      <label className={styles.geoJSONErrorsTitle}>{resourcesContext.messages['geoJSONErrors']}</label>
      <pre>
        <ul>
          {geojsonhint.hint(geoJSON).map(error => (
            <GeoJSONError key={uniqueId('geoJSONError_')} line={error.line} message={error.message} />
          ))}
        </ul>
      </pre>
    </div>
  );
};
