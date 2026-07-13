import { useContext } from 'react';
import styles from './CoordinatesMoreInfo.module.scss';

import { Button } from 'views/_components/Button';
import { GeoJSONErrorList } from './_components/GeoJSONErrorList/GeoJSONErrorList';

import { MapUtils } from 'views/_functions/Utils/MapUtils';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { DatasetService } from 'services/DatasetService';
import { DownloadFile } from 'views/_components/DownloadFile';

export const CoordinatesMoreInfo = ({
  geoJSON,
  datasetId,
  recordId,
  fieldId,
  dataflowId,
  tableSchemaId,
  providerId,
  isDownloading,
  setIsDownloading
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const isValidJSON = MapUtils.isValidJSON(geoJSON);
  const getGeoJson = () => (isValidJSON ? JSON.stringify(JSON.parse(geoJSON), null, 2) : geoJSON);

  const onDownloadGeoJSON = async () => {
    setIsDownloading(true);

    try {
      const { data } = await DatasetService.downloadGeometry({
        datasetId,
        recordId,
        fieldId,
        dataflowId,
        tableSchemaId,
        providerId: providerId ?? 0
      });
      DownloadFile(data, 'geometry.geojson');
    } catch (error) {
      console.error('CoordinatesMoreInfo - onDownloadGeoJSON.', error);
      setIsDownloading(false);
    }
  };

  return (
    <div>
      <div className={styles.geoJSONErrorWrapper}>
        <div>
          <Button
            className="p-button-secondary"
            disabled={isDownloading}
            icon={isDownloading ? 'spinnerAnimate' : 'export'}
            // icon="export"
            label={resourcesContext.messages['downloadGeoJsonFile']}
            onClick={onDownloadGeoJSON}
            tooltipOptions={{ position: 'top' }}
          />
        </div>
        {isValidJSON && <GeoJSONErrorList geoJSON={getGeoJson(geoJSON)} />}
      </div>
    </div>
  );
};
