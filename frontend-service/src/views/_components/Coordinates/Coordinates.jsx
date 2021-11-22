import { useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import classNames from 'classnames';

import styles from './Coordinates.module.scss';

import { Button } from 'views/_components/Button';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Coordinates = ({
  crsDisabled,
  crsOptions = [],
  crsValue,
  disabled = false,
  errorMessage = '',
  hasErrorMessage = false,
  id,
  initialGeoJson,
  isCellEditor = false,
  onBlur = () => {},
  onCrsChange = () => {},
  onFocus = () => {},
  onKeyDown = () => {},
  onMapOpen = () => {},
  showMessageError = false,
  xyLabels = false
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const [hasErrors, setHasErrors] = useState({});
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  useEffect(() => {
    if (initialGeoJson) {
      const parsedInitialGeoJson = JSON.parse(initialGeoJson);
      setLatitude(parsedInitialGeoJson.geometry.coordinates[0]);
      setLongitude(parsedInitialGeoJson.geometry.coordinates[1]);
    }
  }, [initialGeoJson]);

  const checkCoordinates = (lat, long) => {
    setHasErrors({
      latitude: checkEmptyCoordinate(lat),
      longitude: checkEmptyCoordinate(long)
    });
  };

  const checkEmptyCoordinate = coord => (!isNil(coord) ? coord.toString().trim() === '' : true);

  return (
    <div>
      <div className={styles.coordinatesWrapper}>
        <label className={styles.epsg}>{resourcesContext.messages[!xyLabels ? 'latitude' : 'x']}:</label>
        <InputText
          className={classNames({ [styles.error]: hasErrors.latitude, [styles.isCellEditor]: isCellEditor })}
          disabled={disabled}
          id={`${id}_lat`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([e.target.value, longitude].join(', '));
            checkCoordinates(e.target.value, longitude);
          }}
          onChange={e => setLatitude(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onFocus();
          }}
          onKeyDown={e => onKeyDown(e, e.target.value)}
          type="text"
          value={latitude}
        />
        <label className={classNames(styles.epsg, styles.longitude)}>
          {resourcesContext.messages[!xyLabels ? 'longitude' : 'y']}:
        </label>
        <InputText
          className={classNames({ [styles.error]: hasErrors.longitude, [styles.isCellEditor]: isCellEditor })}
          disabled={disabled}
          id={`${id}_long`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([latitude, e.target.value].join(', '));
            checkCoordinates(latitude, e.target.value);
          }}
          onChange={e => setLongitude(e.target.value)}
          onFocus={e => {
            e.preventDefault();
            onFocus();
          }}
          onKeyDown={e => onKeyDown(e, e.target.value)}
          type="text"
          value={longitude}
        />
      </div>
      {hasErrorMessage && (
        <p className={styles.errorMessage}>
          {showMessageError && <span className={styles.pointError}>{errorMessage}</span>}
        </p>
      )}
      <div className={styles.pointEpsgWrapper}>
        <label className={styles.epsg}>{resourcesContext.messages['epsg']}</label>
        <Dropdown
          appendTo={document.body}
          ariaLabel={'crs'}
          className={styles.epsgSwitcher}
          disabled={crsDisabled}
          onChange={e => onCrsChange(e.target.value)}
          optionLabel="label"
          options={crsOptions}
          placeholder={resourcesContext.messages['selectCRS']}
          value={crsValue}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          disabled={false}
          icon="marker"
          onClick={() => onMapOpen(initialGeoJson)}
          tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );
};
