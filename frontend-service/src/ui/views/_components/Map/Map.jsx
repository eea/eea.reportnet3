import React, { useContext, useState, useEffect, useRef } from 'react';
import isNil from 'lodash/isNil';
import cloneDeep from 'lodash/cloneDeep';

import styles from './Map.module.scss';
import 'leaflet/dist/leaflet.css';
import 'esri-leaflet-geocoder/dist/esri-leaflet-geocoder.css';

import { Dropdown } from 'ui/views/_components/Dropdown';

import 'proj4leaflet';
import L from 'leaflet';
import proj4 from 'proj4';
import * as ELG from 'esri-leaflet-geocoder';
import * as esri from 'esri-leaflet';
import { Map as MapComponent, GeoJSON, Marker, Popup } from 'react-leaflet';

import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import newMarkerIcon from 'assets/images/newMarker.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
});

// 3035 --> ETRS89 / ETRS-LAEA
// 4258 --> ETRS89
// 4326 --> WGS84

proj4.defs([
  ['EPSG:4258', '+proj=longlat +ellps=GRS80 +no_defs'],
  ['EPSG:3035', '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs'],
  ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs']
]);

L.Marker.prototype.options.icon = DefaultIcon;

let NewMarkerIcon = L.icon({
  iconUrl: newMarkerIcon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
});
export const Map = ({
  geoJson = '',
  onSelectPoint,
  options = {
    zoom: [15],
    bearing: [0],
    pitch: [0],
    center: MapUtils.checkValidJSONCoordinates(geoJson)
      ? typeof geoJson === 'object'
        ? JSON.stringify(geoJson)
        : geoJson
      : `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`
  },
  selectedCRS = { label: 'WGS84', value: 'EPSG:4326' }
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  // const { BaseLayer, Overlay } = LayersControl;

  const crs = [
    { label: 'WGS84', value: 'EPSG:4326' },
    { label: 'ETRS89', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG:3035' }
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

  const [currentTheme, setCurrentTheme] = useState(
    themes.filter(theme => theme.value === userContext.userProps.basemapLayer)[0] || themes[0]
  );

  const [currentCRS, setCurrentCRS] = useState(
    !isNil(selectedCRS) ? crs.filter(crsItem => crsItem.value === selectedCRS)[0] : selectedCRS
  );

  const [newPositionMarker, setNewPositionMarker] = useState();
  const [mapGeoJson, setMapGeoJson] = useState(options.center);

  const [isNewPositionMarkerVisible, setIsNewPositionMarkerVisible] = useState(false);
  const [popUpVisible, setPopUpVisible] = useState(false);

  const mapRef = useRef();

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    esri.basemapLayer(currentTheme.value).addTo(map);

    // const geojsonLayer = L.geoJson(geojson, {
    //   style: function (feature) {
    //     return { color: feature.properties.GPSUserColor };
    //   },
    //   pointToLayer: function (feature, latlng) {
    //     return new L.CircleMarker(latlng, { radius: 10, fillOpacity: 0.85 });
    //   },
    //   onEachFeature: function (feature, layer) {
    //     layer.bindPopup(feature.properties.GPSUserName);
    //   }
    // });

    // map.addLayer(geojsonLayer);

    // mapRef.current.leafletElement.setView(options.center, 2);
    // esri
    //   .tiledMapLayer({
    //     url: 'https://services.arcgisonline.com/ArcGIS/rest/services/USA_Topo_Maps/MapServer',
    //     tileSize: 256,
    //     maxZoom: 20,
    //     minZoom: 0
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
        setNewPositionMarker(`${data.results[i].latlng.lat}, ${data.results[i].latlng.lng}, EPSG:4326`);
        onSelectPoint(
          proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [data.results[i].latlng.lat, data.results[i].latlng.lng]),
          currentCRS.value
        );
      }
    });

    // var service = esri.mapService({
    //   url: 'https://land.discomap.eea.europa.eu/arcgis/rest/services/Background/Background_Cashed_WGS84/MapServer'
    // });

    // service
    //   .identify()
    //   .on(map)
    //   .at([45.543, -12.621])
    //   .layers('Countries')
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

    // esri
    //   .featureLayer({
    //     url: 'https://sampleserver6.arcgisonline.com/arcgis/rest/services/Earthquakes_Since1970/MapServer/0'
    //   })
    //   .addTo(map);
  }, []);

  useEffect(() => {
    const inmMapGeoJson = JSON.parse(cloneDeep(mapGeoJson));
    if (inmMapGeoJson.properties.rsid !== 'EPSG:4326') {
      inmMapGeoJson.geometry.coordinates = projectGeoJsonCoordinates(geoJson);
      setMapGeoJson(JSON.stringify(inmMapGeoJson));
    }
  }, [geoJson]);

  useEffect(() => {
    if (!isNewPositionMarkerVisible && !isNil(newPositionMarker)) {
      setIsNewPositionMarkerVisible(true);
    }
  }, [newPositionMarker]);

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    // esri.removeLayer();
    esri.basemapLayer(currentTheme.value).addTo(map);
  }, [currentTheme]);

  const onCRSChange = item => {
    const selectedCRS = crs.filter(t => t.value === item.value)[0];
    setCurrentCRS(selectedCRS);
  };

  const onEachFeature = (feature, layer) => {
    console.log({ feature, layer });
    layer.bindPopup(onPrintCoordinates(feature.geometry.coordinates.join(', ')));
    layer.on({
      click: () =>
        mapRef.current.leafletElement.setView(feature.geometry.coordinates, mapRef.current.leafletElement.zoom)
    });
  };

  const onPrintCoordinates = coordinates => `{Lat: ${coordinates.split(', ')[0]}, Lng: ${coordinates.split(', ')[1]}}`;

  const onThemeChange = item => {
    const selectedTheme = themes.filter(t => t.value === item.value)[0];
    setCurrentTheme(selectedTheme);
  };

  const projectGeoJsonCoordinates = geoJsonData => {
    const parsedGeoJsonData = typeof geoJsonData === 'object' ? geoJsonData : JSON.parse(geoJsonData);
    return proj4(
      proj4(!isNil(parsedGeoJsonData) ? parsedGeoJsonData.properties.rsid : currentCRS.value),
      proj4('EPSG:4326'),
      parsedGeoJsonData.geometry.coordinates
    );
  };

  const projectPointCoordinates = coordinates => {
    return proj4(
      proj4(!isNil(coordinates.split(', ')[2]) ? coordinates.split(', ')[2] : currentCRS.value),
      proj4('EPSG:4326'),
      MapUtils.parseCoordinates(coordinates.split(', '))
    );
  };

  return (
    <>
      <div style={{ display: 'inline-flex', width: '60%' }}>
        <Dropdown
          ariaLabel={'themes'}
          className={styles.themeSwitcherSplitButton}
          onChange={e => onThemeChange(e.target.value)}
          optionLabel="label"
          options={themes}
          placeholder="Select a theme"
          value={currentTheme}
          style={{ width: '20%' }}
        />
        <Dropdown
          ariaLabel={'crs'}
          className={styles.crsSwitcherSplitButton}
          disabled={!MapUtils.checkValidJSONCoordinates(geoJson) && !isNewPositionMarkerVisible}
          onChange={e => {
            onCRSChange(e.target.value);
            onSelectPoint(
              proj4(
                proj4('EPSG:4326'),
                proj4(e.target.value.value),
                isNewPositionMarkerVisible
                  ? MapUtils.parseCoordinates(newPositionMarker.split(', '))
                  : JSON.parse(mapGeoJson).geometry.coordinates
              ),
              e.target.value.value
            );
          }}
          optionLabel="label"
          options={crs}
          placeholder="Select a CRS"
          value={currentCRS}
          style={{ width: '20%' }}
        />
      </div>
      <label className={styles.mapSelectMessage}>{resources.messages['mapSelectPointMessage']}</label>
      <MapComponent
        style={{ height: '60vh' }}
        doubleClickZoom={false}
        center={projectGeoJsonCoordinates(options.center)}
        zoom="4"
        ref={mapRef}
        onDblclick={e => {
          setNewPositionMarker(`${e.latlng.lat}, ${e.latlng.lng}, EPSG:4326`);
          onSelectPoint(
            proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [e.latlng.lat, e.latlng.lng]),
            currentCRS.value
          );
          mapRef.current.leafletElement.setView(e.latlng, mapRef.current.leafletElement.zoom);
        }}>
        {/* <LayersControl position="topright">
          <BaseLayer checked name="EEA Countries">
            <TileLayer
              // attribution="Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012"
              url="https://land.discomap.eea.europa.eu/arcgis/rest/services/Background/Background_Cashed_WGS84/MapServer/tile/{z}/{y}/{x}"
              opacity={0.35}
            />
          </BaseLayer>
          <BaseLayer name="None">
            <TileLayer url="" />
          </BaseLayer>
        </LayersControl> */}
        {MapUtils.checkValidJSONCoordinates(geoJson) && (
          <GeoJSON
            data={JSON.parse(mapGeoJson)}
            onEachFeature={onEachFeature}
            coordsToLatLng={coords => new L.LatLng(coords[0], coords[1], coords[2])}
          />
        )}
        {isNewPositionMarkerVisible && (
          <Marker
            draggable={false}
            icon={NewMarkerIcon}
            position={projectPointCoordinates(newPositionMarker)}
            onClick={e => {
              if (!popUpVisible) {
                setPopUpVisible(true);
              }
              mapRef.current.leafletElement.setView(e.latlng, mapRef.current.leafletElement.zoom);
            }}>
            <Popup>{onPrintCoordinates(newPositionMarker)}</Popup>
          </Marker>
        )}
      </MapComponent>
    </>
  );
};
