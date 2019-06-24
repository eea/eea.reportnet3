import React, {useState, useEffect, useContext} from 'react';

import jsonDataSchema from '../../../../assets/jsons/datosDataSchema3.json';
//import jsonDataSchemaErrors from '../../../../assets/jsons/errorsDataSchema.json';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from './DataFlowColumn.module.css';
import ResourcesContext from '../../../Context/ResourcesContext';

const DataFlowColumn = () => {
  const resources = useContext(ResourcesContext);  

  return (
    <div>
        <div className="rep-row">
            <div className="rep-col-12">
                <p className={styles.title}>Title</p>
            </div>
            <div className="rep-col-12">
                <input
                    type="text"
                    id=""
                    /* onKeyUp="" */
                    className="subscribe-df__input rep-col-12"
                    placeholder="Search data flows"
                    title="Type a DataFlow name"
                />
            </div>
        </div>
        <div className="rep-row">
            <div className="rep-col-12">
                <p className={styles.title}>{jsonDataSchema.nameDataSetSchema}</p>
            </div>
            <div className="rep-col-12">
                <button>This is going to be a subscribe button</button>
            </div>
        </div>
    </div>
  );
}

export default DataFlowColumn;