import React, { useContext, useState, useEffect, useRef } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './Map.module.scss';
import 'leaflet/dist/leaflet.css';
import 'esri-leaflet-geocoder/dist/esri-leaflet-geocoder.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import 'proj4leaflet';
import L from 'leaflet';
import { CRS } from 'leaflet';
import * as ELG from 'esri-leaflet-geocoder';
import * as esri from 'esri-leaflet';
import { Map as MapComponent, TileLayer, Marker, Popup } from 'react-leaflet';
// import ReactMapboxGl, { Feature, Layer, Marker, Popup } from 'react-mapbox-gl';

import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import newMarkerIcon from 'assets/images/newMarker.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
// import { svg } from 'assets/svg/marker';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow
});

const crsProj = new L.Proj.CRS('EPSG:4326', '+proj=longlat +datum=WGS84 +no_defs', {
  origin: [-180, 90],
  resolutions: [8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125]
});

L.Marker.prototype.options.icon = DefaultIcon;

let NewMarkerIcon = L.icon({
  iconUrl: newMarkerIcon
  // iconSize: [27, 31],
  // iconAnchor: [13.5, 17.5],
  // popupAnchor: [0, -11]
});

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
        : [55.6811608, 12.5844761]
  },
  selectButton = false
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const crs = [
    { label: 'WGS84', value: 'EPSG4326' },
    { label: 'ETRS89', value: 'EPSG4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG3035' }
  ];

  const themes = [
    { label: 'Topographic', value: 'Topographic' },
    { label: 'Streets', value: 'Streets' },
    { label: 'National Geographic', value: 'NationalGeographic' },
    { label: 'Oceans', value: 'Oceans' },
    { label: 'Gray', value: 'Gray' },
    { label: 'Dark Gray', value: 'DarkGray' },
    { label: 'Imagery', value: 'Imagery' },
    { label: 'Imagery (Clarity)', value: 'ImageryClarity' },
    { label: 'Imagery (Firefly)', value: 'ImageryFirefly' },
    { label: 'Shaded Relief', value: 'ShadedRelief' }
  ];

  console.log(userContext.userProps);
  const [currentTheme, setCurrentTheme] = useState(
    themes.filter(theme => theme.value === userContext.userProps.basemapLayer)[0] || themes[0]
  );
  const [currentCRS, setCurrentCRS] = useState(crs[1]);
  const [is3dSwitched, setIs3dSwitched] = useState(false);
  const [marker, setMarker] = useState(options.center);
  const [newPositionMarker, setNewPositionMarker] = useState([0, 0]);
  const [isNewPositionMarkerVisible, setIsNewPositionMarkerVisible] = useState(false);
  // const [popUpCoordinates, setPopUpCoordinates] = useState(options.center);
  const [popUpVisible, setPopUpVisible] = useState(false);

  const mapRef = useRef();

  useEffect(() => {
    console.log(mapRef.current);
    const map = mapRef.current.leafletElement;
    esri.basemapLayer(currentTheme.value).addTo(map);
    // mapRef.current.leafletElement.setView(options.center, 2);
    // esri
    //   .tiledMapLayer({
    //     url: 'https://services.arcgisonline.com/ArcGIS/rest/services/USA_Topo_Maps/MapServer'
    //   })
    //   .addTo(map);

    // var defaultLayer = L.tileLayer(
    //   '//services.arcgisonline.com/arcgis/rest/services/ESRI_Imagery_World_2D/MapServer/tile/{z}/{y}/{x}',
    //   {
    //     attribution: false,
    //     continuousWorld: true,
    //     crs: '4326',
    //     tileSize: 512
    //   }
    // );
    // defaultLayer.addTo(map);

    // var map = new L.Map(mapRef.current.leafletElement).setView([45.543, -122.621], 5);

    const searchControl = new ELG.Geosearch().addTo(map);
    const results = new L.LayerGroup().addTo(map);

    searchControl.on('results', function (data) {
      results.clearLayers();
      for (let i = data.results.length - 1; i >= 0; i--) {
        console.log(data.results[i]);
        setNewPositionMarker([data.results[i].latlng.lat, data.results[i].latlng.lng]);
        // results.addLayer(L.marker(data.results[i].latlng));
      }
    });

    // var service = esri.mapService({
    //   url: 'https://air.discomap.eea.europa.eu/arcgis/rest/services/AirQuality/'
    // });

    // service
    //   .identify()
    //   .on(map)
    //   // .at([45.543, -12.621])
    //   .layers('visible:1')
    //   .run(function (error, featureCollection, response) {
    //     if (error) {
    //       console.log(error);
    //       return;
    //     }
    //     console.log('UTC Offset: ' + featureCollection.features[0].properties.ZONE);
    //   });

    // setDraggablePointsCoordinates([
    //   [12.5874761, 55.6811578],
    //   [12.5944761, 55.6811578]
    // ]);
    // let map = L.map(element).setView([-41.2858, 174.78682], 14);
    console.log('ESRI::', esri);

    // esri
    //   .featureLayer({
    //     url: 'https://sampleserver6.arcgisonline.com/arcgis/rest/services/Earthquakes_Since1970/MapServer/0'
    //   })
    //   .addTo(map);
  }, []);

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    // esri.removeLayer();
    esri.basemapLayer(currentTheme.value).addTo(map);
  }, [currentTheme]);

  // const onDragEndPolygon = (evt, index) => {
  //   const inmDraggablePointsCoordinates = [...draggablePointsCoordinates];
  //   inmDraggablePointsCoordinates[index] = [evt.lngLat.lng, evt.lngLat.lat];
  //   setDraggablePointsCoordinates(inmDraggablePointsCoordinates);
  // };

  // const basemapTemplate = option => {
  //   console.log({ option });
  //   if (!option.value) {
  //     return option.label;
  //   } else {
  //     return (
  //       <div className={`p-clearfix ${styles.basemapItem}`}>
  //         <span style={{ margin: '.5em .25em 0 0.5em' }}>{option.label}</span>
  //         <img alt={option.label} src={`assets/img/layers/Streets.png`} />
  //       </div>
  //     );
  //   }
  // };

  const onCRSChange = item => {
    console.log({ item });
    const selectedCRS = crs.filter(t => t.value === item.value)[0];
    console.log(selectedCRS);
    setCurrentCRS(selectedCRS);
  };

  const onPrintCoordinates = coordinates => {
    return `{Lat: ${coordinates[0]}, Lng: ${coordinates[1]}}`;
  };

  const onThemeChange = item => {
    console.log({ item });
    const selectedTheme = themes.filter(t => t.value === item.value)[0];
    console.log(selectedTheme);
    setCurrentTheme(selectedTheme);
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
    <>
      <Dropdown
        ariaLabel={'themes'}
        className={styles.themeSwitcherSplitButton}
        // itemTemplate={basemapTemplate}
        options={themes}
        optionLabel="label"
        onChange={e => {
          onThemeChange(e.target.value);
        }}
        placeholder="Select a theme"
        value={currentTheme}
        style={{ width: '20%' }}
      />
      <Dropdown
        ariaLabel={'crs'}
        className={styles.crsSwitcherSplitButton}
        options={crs}
        optionLabel="label"
        onChange={e => {
          onCRSChange(e.target.value);
        }}
        placeholder="Select a CRS"
        value={currentCRS}
        style={{ width: '20%' }}
      />
      {console.log(CRS[currentCRS.value], currentCRS.value, CRS.EPSG4326)}
      <MapComponent
        // crs={CRS[currentCRS.value]}
        // crs={crsProj}
        continuousWorld={true}
        worldCopyJump={false}
        style={{ height: '60vh' }}
        doubleClickZoom={false}
        // fitBounds={[[40.712, -74.227]]}
        center={options.center}
        // setView={([42.528, -12.68], 2)}
        zoom="10"
        ref={mapRef}
        onClick={e => {
          if (!isNewPositionMarkerVisible) {
            setIsNewPositionMarkerVisible(true);
          }
          setNewPositionMarker([e.latlng.lat, e.latlng.lng]);
          onSelectPoint([e.latlng.lat, e.latlng.lng]);
        }}>
        {isNewPositionMarkerVisible && (
          <Marker
            draggable={true}
            icon={NewMarkerIcon}
            position={newPositionMarker}
            onClick={e => {
              // setPopUpCoordinates(newPositionMarker);
              if (!popUpVisible) {
                setPopUpVisible(true);
              }
            }}
            onDrag={e => setNewPositionMarker([e.latlng.lat, e.latlng.lng])}>
            <Popup>{onPrintCoordinates(newPositionMarker)}</Popup>
          </Marker>
        )}
        <Marker
          className={`${styles.marker} ${styles.bounce}`}
          position={marker}
          onClick={e => {
            // setPopUpCoordinates(marker);
            if (!popUpVisible) {
              setPopUpVisible(true);
            }
          }}>
          {/* <div className={`${styles.marker} ${styles.bounce}`}></div>
          <div className={styles.pulse}></div> */}
          <Popup>{onPrintCoordinates(marker)}</Popup>
        </Marker>
        {/* <div className="pointer" /> */}
      </MapComponent>
    </>
  );
};
