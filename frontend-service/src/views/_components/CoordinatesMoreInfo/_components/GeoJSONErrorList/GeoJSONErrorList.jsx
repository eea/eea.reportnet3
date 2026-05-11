import { useContext } from 'react';
import geojsonhint from '@mapbox/geojsonhint';

import uniqueId from 'lodash/uniqueId';

import styles from './GeoJSONErrorList.module.scss';

import { GeoJSONError } from './_components/GeoJSONError';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const GeoJSONErrorList = ({ geoJSON }) => {
  const resourcesContext = useContext(ResourcesContext);

  const renderErrors = () => {
    const parsed = JSON.parse(geoJSON);
    const srid = parsed?.properties?.srid ?? parsed?.srid ?? parsed?.crs?.properties?.name;
    const geometryType = parsed?.geometry?.type ?? parsed?.type;
    const is3035Polygon =
      String(srid).includes('3035') && ['POLYGON', 'MULTIPOLYGON'].includes(String(geometryType).toUpperCase());

    const errors = geojsonhint
      .hint(geoJSON)
      .filter(error => !(is3035Polygon && /right-hand rule/i.test(error.message)));

    if (errors.length > 0) {
      return (
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
      );
    } else {
      return null;
    }
  };

  return renderErrors();
};
