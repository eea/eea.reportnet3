import React, { useContext, useState, useEffect } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './Map.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { RotationControl } from 'react-mapbox-gl';
import { ScaleControl } from 'react-mapbox-gl';
import { ZoomControl } from 'react-mapbox-gl';
import L from 'leaflet';
import ReactMapboxGl, { Feature, Layer, Marker, Popup } from 'react-mapbox-gl';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
// import { svg } from 'assets/svg/marker';

const LeafletMap = ReactMapboxGl({
  accessToken: 'pk.eyJ1Ijoia2lrb2xhYmlhbm8iLCJhIjoiY2sxN3FqMHMyMG81bTNwbzI4ZDQ1cDVveiJ9.y7rL8Kz1R_RvYMtalBO9cA'
});

// const lineLayout = {
//   'line-cap': 'round',
//   'line-join': 'round'
// };

// Define layout to use in Layer component
// const markerLayer = { 'icon-image': 'mapMarker' };

// const linePaint = {
//   'line-color': '#4790E5',
//   'line-width': 8
// };

const paintLayer = {
  'fill-extrusion-color': '#add8e6',
  'fill-extrusion-height': {
    type: 'identity',
    property: 'height'
  },
  'fill-extrusion-base': {
    type: 'identity',
    property: 'min_height'
  },
  'fill-extrusion-opacity': 0.6
};

// const polygonPaint = {
//   'fill-color': '#6F788A',
//   'fill-opacity': 0.7
// };

// const draggableCirclePaint = {
//   'circle-stroke-width': 4,
//   'circle-radius': 10,
//   'circle-blur': 0.15,
//   'circle-color': '#4790E5',
//   'circle-stroke-color': 'red'
// };

// Create an image for the Layer
// const image = new Image();
// image.src = 'data:image/svg+xml;charset=utf-8;base64,' + btoa(svg);
// const images = ['mapMarker', image];

const themes = [
  { id: 'Decimal', url: 'mapbox://styles/kikolabiano/ck17wg2ys28jh1cnr32lvcxsi' },
  { id: 'Minimo', url: 'mapbox://styles/kikolabiano/ck17qkf2y24am1cpzrf1x7wy8' },
  { id: 'Night', url: 'mapbox://styles/mapbox/navigation-preview-night-v2' },
  { id: 'North', url: 'mapbox://styles/kikolabiano/ck17wkefc4llr1cs23xs0gf2g' },
  { id: 'Satellite', url: 'mapbox://styles/mapbox/satellite-v9' },
  { id: 'Streets', url: 'mapbox://styles/mapbox/streets-v10' },
  { id: 'Terrain', url: 'mapbox://styles/kikolabiano/ck17whvsv4skp1dpffy3i2owh' }
];

export const Map = ({
  centerToCoordinates = false,
  coordinates,
  onSelectPoint,
  options = {
    zoom: [15],
    bearing: [0],
    pitch: [0],
    center:
      coordinates !== ''
        ? !Array.isArray(coordinates)
          ? coordinates.split(',')
          : coordinates
        : [12.5844761, 55.6811578]
  },
  selectButton = false
}) => {
  const resources = useContext(ResourcesContext);
  const [currentTheme, setCurrentTheme] = useState(themes[1]);
  const [is3dSwitched, setIs3dSwitched] = useState(false);
  const [draggablePointsCoordinates, setDraggablePointsCoordinates] = useState();
  const [popUpCoordinates, setPopUpCoordinates] = useState(options.center);
  const [popUpVisible, setPopUpVisible] = useState(false);

  useEffect(() => {
    setDraggablePointsCoordinates([
      [12.5874761, 55.6811578],
      [12.5944761, 55.6811578]
    ]);
  }, []);

  // const onDragEndPolygon = (evt, index) => {
  //   const inmDraggablePointsCoordinates = [...draggablePointsCoordinates];
  //   inmDraggablePointsCoordinates[index] = [evt.lngLat.lng, evt.lngLat.lat];
  //   setDraggablePointsCoordinates(inmDraggablePointsCoordinates);
  // };

  const onPrintCoordinates = coordinates => {
    // const latlng = new L.latLng(coordinates[0], coordinates[1]);
    // const point = L.Projection.Mercator.project(latlng);
    // ${JSON.stringify(point)}
    return `{Lng: ${coordinates[0]}, Lat: ${coordinates[1]}}`;
  };

  const onThemeChange = item => {
    const selectedTheme = themes.filter(t => t.id === item.id)[0];
    setCurrentTheme(selectedTheme);
  };

  const onUpdatePopupCoordinates = element => {
    if (!popUpVisible) {
      setPopUpVisible(true);
    }
    setPopUpCoordinates([element.lngLat.lng, element.lngLat.lat]);
  };

  const parseCoordinates = coord => {
    const parsedCoords = coord
      .replace(/\[/g, '')
      .replace(/\]/g, '')
      .split(',')
      .map(c => Number(c))
      .reduce((result, value, i, array) => {
        if (i % 2 === 0) result.push(array.slice(i, i + 2));
        return result;
      }, []);
    if (parsedCoords.length > 1) {
      return parsedCoords;
    } else {
      return parsedCoords[0];
    }
  };

  // const parseCoordinatesSRID = coord => {
  //   coord = 'SRID=4326;POINT(111.1111111 1.1111111)';
  //   const splitedCoords = coord.split(';');
  //   const SRID = splitedCoords[0];
  //   const coordData = splitedCoords[1];

  //   const parsedCoords = coordData
  //     .substring(coordData.indexOf('(') + 1)
  //     .replace(/\(/g, '')
  //     .replace(/\)/g, '')
  //     .split(' ')
  //     .map(c => Number(c))
  //     .reduce((result, value, i, array) => {
  //       if (i % 2 === 0) result.push(array.slice(i, i + 2));
  //       return result;
  //     }, []);
  //   if (parsedCoords.length > 1) {
  //     return parsedCoords;
  //   } else {
  //     return parsedCoords[0];
  //   }
  // };

  return (
    <LeafletMap
      bearing={options.bearing}
      center={
        centerToCoordinates && parseCoordinates(coordinates).length === 1
          ? parseCoordinates(coordinates)
          : popUpCoordinates
      }
      containerStyle={{ height: '70vh' }}
      onClick={(map, e) => {
        onUpdatePopupCoordinates(e);
      }}
      onMove={e => {
        // console.log('MOOOVE', e);
      }}
      pitch={options.pitch}
      style={currentTheme ? currentTheme.url : 'mapbox://styles/kikolabiano/ck17qkf2y24am1cpzrf1x7wy8'}
      zoom={options.zoom}>
      {is3dSwitched ? (
        <Layer
          id="3d-buildings"
          sourceId="composite"
          sourceLayer="building"
          filter={['==', 'extrude', 'true']}
          type="fill-extrusion"
          minZoom={14}
          paint={paintLayer}
        />
      ) : null}

      {/* {parseCoordinates(coordinates).length == 1 ? ( */}
      <Marker
        // coordinates={parseCoordinates(coordinates)}
        draggable={true}
        coordinates={popUpCoordinates}
        anchor="bottom"
        onClick={e => {
          setPopUpCoordinates(options.center);
          if (!popUpVisible) {
            setPopUpVisible(true);
          }
        }}>
        <div className={`${styles.marker} ${styles.bounce}`}></div>
        <div className={styles.pulse}></div>
      </Marker>
      {/* ) : null} */}

      {/* Line example 
      <Layer type="line" layout={lineLayout} paint={linePaint}>
        <Feature
          coordinates={parseCoordinates(
            '[[12.5874567, 55.6808570],[12.5914371, 55.6797863],[12.5906646, 55.6787519],[12.5925207, 55.6782861],[12.5912440, 55.6775542],[12.5896347, 55.6768464],[12.5871134, 55.6760720]]'
          )}
          onMouseEnter={e => {
            e.map.getCanvas().style.cursor = 'pointer';
          }}
          onMouseLeave={e => {
            e.map.getCanvas().style.cursor = 'grab';
          }}
        />
      </Layer>*/}

      {/* Polygon example 
      <Layer type="fill" paint={polygonPaint}>
        <Feature
          // draggable={true}
          // onDrag={e => {
          //   onDragEndPolygon(e);
          // }}
          coordinates={[
            parseCoordinates(
              '[[12.5852895,55.6814377],[12.5873387,55.68086],[12.5868022,55.6803338],[12.5865662,55.6800495],[12.5862014,55.6799073],[12.5855523,55.6797894],[12.5852358,55.6799466],[12.5848818,55.6809145],[12.5852948,55.6814438]]'
            )
          ]}
          onMouseEnter={e => {
            e.map.getCanvas().style.cursor = 'pointer';
          }}
          onMouseLeave={e => {
            e.map.getCanvas().style.cursor = 'grab';
          }}
        />
      </Layer>*/}

      {/* Draggable point example */}
      {/* <Layer type="symbol" id="position-marker" layout={markerLayer} images={images}> 
      <Layer type="circle" id="position-marker" paint={draggableCirclePaint}>
        {!isUndefined(draggablePointsCoordinates)
          ? draggablePointsCoordinates.map((loc, index) => (
              <Feature
                key={index}
                coordinates={loc}
                draggable={true}
                onDragEnd={e => {
                  onDragEndPolygon(e, index);
                }}
              />
            ))
          : null}
      </Layer>*/}

      {popUpVisible ? (
        <Popup
          coordinates={popUpCoordinates}
          offset={{
            'bottom-left': [12, -38],
            bottom: [0, -38],
            'bottom-right': [-12, -38]
          }}
          onClick={() => {
            setPopUpVisible(false);
          }}>
          <h3>{onPrintCoordinates(popUpCoordinates)}</h3>
        </Popup>
      ) : null}

      <div className={styles.mapButtonsBar}>
        <Dropdown
          className={styles.themeSwitcherSplitButton}
          options={themes}
          optionLabel="id"
          onChange={e => {
            onThemeChange(e.target.value);
          }}
          placeholder="Select a theme"
          value={currentTheme}
          style={{ width: '20%' }}
        />
        <span className={styles.InputSwitchText}>{resources.messages['2d']}</span>
        <InputSwitch
          checked={is3dSwitched}
          onChange={e => {
            setIs3dSwitched(e.value);
          }}
          // style={{ width: '20%' }}
        />
        <span className={styles.InputSwitchText}>{resources.messages['3d']}</span>
      </div>
      <div className={styles.mapButtonsBar}>
        {selectButton && (
          <Button
            className={`p-button-primary ${styles.selectPointButton}`}
            icon={'check'}
            label={resources.messages['selectGeographicalData']}
            onClick={() => {
              if (!isNil(onSelectPoint)) {
                onSelectPoint(popUpCoordinates);
              }
            }}
          />
        )}
      </div>

      <ZoomControl style={{ top: 60 }} />
      <ScaleControl />
      <RotationControl style={{ top: 120 }} />
    </LeafletMap>
  );
};
