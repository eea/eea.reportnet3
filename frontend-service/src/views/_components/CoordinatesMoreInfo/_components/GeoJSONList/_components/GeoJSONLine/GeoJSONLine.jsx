import { useEffect, useRef } from 'react';

import styles from './GeoJSONLine.module.scss';

import { useRecoilValue } from 'recoil';

import { geoJSONListStore } from 'views/_components/CoordinatesMoreInfo/_functions/Stores/geoJSONListStore';

export const GeoJSONLine = ({ line, lineNumber }) => {
  const lineRef = useRef();
  const selectedLine = useRecoilValue(geoJSONListStore);

  useEffect(() => {
    if (selectedLine === lineNumber + 1) {
      lineRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [selectedLine]);

  return (
    <span className={selectedLine === lineNumber + 1 ? styles.selectedLine : ''} ref={lineRef}>
      <label className={styles.line}>{line}</label>
      <br />
    </span>
  );
};
