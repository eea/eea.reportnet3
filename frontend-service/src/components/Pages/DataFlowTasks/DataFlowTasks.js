import React, {useEffect, /* useContext,*/ useState} from 'react';
import styles from './DataFlowTasks.module.scss';
import DataFlowList from './DataFlowList/DataFlowList';
import DataFlaws from '../../../assets/jsons/DataFlaws.json'
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
//import ResourcesContext from '../../Context/ResourcesContext';



const DataFlowTasks = () => {
  const [pendingDataFlows, setPendingDataFlows] = useState([]);
  const [acceptetDataFlows, setAcceptedDataFlows] = useState([ ]);

/*   const resources = useContext(ResourcesContext);  
  // This is here just for example purpose 
  const home = {icon: resources.icons["home"], url: '#'}; */


  useEffect(()=>{
  
    //GET JSON    --->   TODO implement this function with real API call
    const jsonMimic = DataFlaws;

    const arrayPending = jsonMimic.filter(jsonData => jsonData.dataFlowStatus === "0");
    const arrayAccepted = jsonMimic.filter(jsonData => jsonData.dataFlowStatus === "1");

    setPendingDataFlows([...pendingDataFlows, arrayPending])
    setAcceptedDataFlows([...acceptetDataFlows, arrayAccepted])   

  }, []);

  return (
    <div className="container-df rep-typography rep-row">

      <div className="subscribe-df rep-col-xs-12 rep-col-md-2">
        {/*TODO  Create component for this button */}
        <h3 className="subscribe-df__title">DATA FLOWS</h3>
        <button className="subscribe-df__btn">+ Subscribe to a data flow</button>
      </div>

      <div className="subscribe-df rep-col-xs-12 rep-col-md-10">
        <DataFlowList pendingDataFlows={pendingDataFlows} />
        <DataFlowList acceptetDataFlows={acceptetDataFlows} /> 
      </div>
    </div>
  );
}
export default DataFlowTasks;
