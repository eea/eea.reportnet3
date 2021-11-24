import { useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import classNames from 'classnames';

import { config } from 'conf';

import styles from './Coordinates.module.scss';

import { Button } from 'views/_components/Button';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { TooltipButton } from 'views/_components/TooltipButton';

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
      longitude: checkEmptyCoordinate(long),
      latOutOfBounds: !inBounds(lat, 'latitude'),
      longOutOfBounds: !inBounds(long, 'longitude')
    });
  };

  const checkEmptyCoordinate = coord => (!isNil(coord) ? coord.toString().trim() === '' : true);

  const inBounds = (coord, coordType) => {
    console.log(crsValue);
    const parsedCoord = parseFloat(coord) || 0;
    if (crsValue.value !== 'EPSG:3035') {
      if (coordType === 'latitude') {
        console.log(parsedCoord, config.GEOGRAPHICAL_LAT_COORD.min, config.GEOGRAPHICAL_LAT_COORD.max);
        return parsedCoord >= config.GEOGRAPHICAL_LAT_COORD.min && parsedCoord <= config.GEOGRAPHICAL_LAT_COORD.max;
      } else {
        console.log(parsedCoord, config.MIN_GEOGRAPHICAL_LONG_COORD, config.MAX_GEOGRAPHICAL_LONG_COORD);
        return parsedCoord >= config.GEOGRAPHICAL_LONG_COORD.min && parsedCoord <= config.GEOGRAPHICAL_LONG_COORD.max;
      }
    } else {
      if (coordType === 'latitude') {
        return parsedCoord >= config.METRICAL_X_COORD.min && parsedCoord <= config.METRICAL_X_COORD.max;
      } else {
        return parsedCoord >= config.METRICAL_Y_COORD.min && parsedCoord <= config.METRICAL_Y_COORD.max;
      }
    }
  };

  const renderLabel = keys => {
    if (!xyLabels) {
      return resourcesContext.messages[isCellEditor ? keys.geographicalShort : keys.geographical];
    } else {
      return resourcesContext.messages[keys.metrical];
    }
  };

  const renderTooltipButtonMessage = () => {
    if (!xyLabels) {
      return `${resourcesContext.messages['coordsOutOfBoundsTooltip']} ${resourcesContext.messages['coordsOutOfBoundsTooltipGeographical']}`;
    } else {
      return `${resourcesContext.messages['coordsOutOfBoundsTooltip']} ${resourcesContext.messages['coordsOutOfBoundsTooltipMetrical']}`;
    }
  };

  return (
    <div>
      <div className={styles.coordinatesWrapper}>
        <label className={styles.epsg}>
          {renderLabel({ geographical: 'latitude', geographicalShort: 'lat', metrical: 'x' })}:
        </label>
        <InputText
          className={classNames({
            [styles.error]: hasErrors.latitude || hasErrors.latOutOfBounds,
            [styles.isCellEditor]: isCellEditor
          })}
          disabled={disabled}
          id={`${id}_lat`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([e.target.value, longitude].join(', '));
            checkCoordinates(e.target.value, longitude);
          }}
          onChange={event => setLatitude(event.target.value)}
          onFocus={e => {
            e.preventDefault();
            onFocus();
          }}
          onKeyDown={e => onKeyDown(e, e.target.value)}
          type="text"
          value={latitude}
        />
        <label className={classNames(styles.epsg, styles.longitude)}>
          {renderLabel({ geographical: 'longitude', geographicalShort: 'long', metrical: 'y' })}:
        </label>
        <InputText
          className={classNames({
            [styles.error]: hasErrors.longitude || hasErrors.longOutOfBounds,
            [styles.isCellEditor]: isCellEditor
          })}
          disabled={disabled}
          id={`${id}_long`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([latitude, e.target.value].join(', '));
            checkCoordinates(latitude, e.target.value);
          }}
          onChange={event => setLongitude(event.target.value)}
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
          {showMessageError && <span className={styles.pointError}>{errorMessage}. </span>}
          {(hasErrors.latOutOfBounds || hasErrors.longOutOfBounds) && (
            <span>
              <span className={styles.pointError}>{resourcesContext.messages['coordsOutOfBounds']}</span>
              <TooltipButton message={renderTooltipButtonMessage()} />
            </span>
          )}
        </p>
      )}
      <div className={`${!isCellEditor ? styles.pointEpsgWrapper : ''}`}>
        <label className={styles.epsg}>{resourcesContext.messages['epsg']}</label>
        <Dropdown
          appendTo={document.body}
          ariaLabel={'crs'}
          className={styles.epsgSwitcher}
          disabled={crsDisabled || hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
          onChange={e => onCrsChange(e.target.value)}
          optionLabel="label"
          options={crsOptions}
          placeholder={resourcesContext.messages['selectCRS']}
          value={crsValue}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          disabled={hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
          icon="marker"
          onClick={() => onMapOpen(initialGeoJson)}
          tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );
};
