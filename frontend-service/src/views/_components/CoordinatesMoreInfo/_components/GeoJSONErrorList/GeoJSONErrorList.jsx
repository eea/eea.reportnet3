import { useContext } from 'react';
import geojsonhint from '@mapbox/geojsonhint';

import uniqueId from 'lodash/uniqueId';

import styles from './GeoJSONErrorList.module.scss';

import { GeoJSONError } from './_components/GeoJSONError';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const GeoJSONErrorList = ({ geoJSON }) => {
  const resourcesContext = useContext(ResourcesContext);
  const errors = geojsonhint.hint(geoJSON);
  return (
    errors.length > 0 && (
      <div>
        <label className={styles.geoJSONErrorsTitle}>{resourcesContext.messages['geoJSONErrors']}</label>
        <pre>
          <ul>
            {errors.map(error => (
              <GeoJSONError key={uniqueId('geoJSONError_')} line={error.line} message={error.message} />
            ))}
          </ul>
        </pre>
      </div>
    )
  );
};
