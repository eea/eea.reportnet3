import styles from './CoordinatesMoreInfo.module.scss';

import { GeoJSONList } from './_components/GeoJSONList/GeoJSONList';
import { GeoJSONErrorList } from './_components/GeoJSONErrorList/GeoJSONErrorList';

import { MapUtils } from 'views/_functions/Utils/MapUtils';

export const CoordinatesMoreInfo = ({ geoJSON }) => {
  const isValidJSON = MapUtils.isValidJSON(geoJSON);
  const getGeoJson = () => (isValidJSON ? JSON.stringify(JSON.parse(geoJSON), null, 2) : geoJSON);

  return (
    <div>
      <div className={styles.geoJSONErrorWrapper}>
        <div>
          <GeoJSONList geoJSON={getGeoJson(geoJSON)} />
        </div>
        {isValidJSON && <GeoJSONErrorList geoJSON={getGeoJson(geoJSON)} />}
      </div>
    </div>
  );
};
