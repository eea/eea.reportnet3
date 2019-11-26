import React, { useContext } from 'react';

import styles from './StatusList.module.scss';
import colors from 'conf/colors.json';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const StatusList = ({ color, levelErrors, filterDispatch, statusFilters }) => {
  const resources = useContext(ResourcesContext);
  let errorListFilters = levelErrors.map(errorLevel => {
    return (
      <li className={styles.listItem}>
        <input
          id={errorLevel.toString().toLowerCase()}
          className={styles.checkbox}
          style={{ backgroundColor: colors[errorLevel.toString().toLowerCase()] }}
          type="checkbox"
          defaultChecked={statusFilters.includes(errorLevel.toString()) ? false : true}
          onChange={e => {
            filterDispatch({
              type: e.target.checked ? 'STATUS_FILTER_ON' : 'STATUS_FILTER_OFF',
              payload: { msg: errorLevel.toString() }
            });
          }}
        />
        <label htmlFor={errorLevel.toString().toLowerCase()} className={styles.labelItem}>
          {resources.messages[errorLevel.toString().toLowerCase()]}
        </label>
      </li>
    );
  });
  return <ul className={styles.list}>{errorListFilters}</ul>;
};

// return (
//   <ul className={styles.list}>
//     <li className={styles.listItem}>
//       <input
//         id="correct"
//         className={styles.checkbox}
//         style={{ backgroundColor: color.CORRECT }}
//         type="checkbox"
//         defaultChecked={true}
//         onChange={e => {
//           if (e.target.checked) {
//             filterDispatch({
//               type: 'STATUS_FILTER_ON',
//               payload: { msg: 'CORRECT' }
//             });
//           } else {
//             filterDispatch({
//               type: 'STATUS_FILTER_OFF',
//               payload: { msg: 'CORRECT' }
//             });
//           }
//         }}
//       />
//       <label htmlFor="correct" className={styles.labelItem}>
//         {resources.messages.correct}
//       </label>
//     </li>
//     <li className={styles.listItem}>
//       <input
//         id="warning"
//         className={styles.checkbox}
//         style={{ backgroundColor: color.WARNING }}
//         type="checkbox"
//         defaultChecked={true}
//         onChange={e => {
//           if (e.target.checked) {
//             filterDispatch({
//               type: 'STATUS_FILTER_ON',
//               payload: { msg: 'WARNING' }
//             });
//           } else {
//             filterDispatch({
//               type: 'STATUS_FILTER_OFF',
//               payload: { msg: 'WARNING' }
//             });
//           }
//         }}
//       />
//       <label htmlFor="warning" className={styles.labelItem}>
//         {resources.messages.warning}
//       </label>
//     </li>
//     <li className={styles.listItem}>
//       <input
//         id="error"
//         className={styles.checkbox}
//         style={{ backgroundColor: color.ERROR }}
//         type="checkbox"
//         defaultChecked={true}
//         onChange={e => {
//           if (e.target.checked) {
//             filterDispatch({
//               type: 'STATUS_FILTER_ON',
//               payload: { msg: 'ERROR' }
//             });
//           } else {
//             filterDispatch({
//               type: 'STATUS_FILTER_OFF',
//               payload: { msg: 'ERROR' }
//             });
//           }
//         }}
//       />
//       <label htmlFor="error" className={styles.labelItem}>
//         {resources.messages.error}
//       </label>
//     </li>
//   </ul>
// );
