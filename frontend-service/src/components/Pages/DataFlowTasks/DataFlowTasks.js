import React, {useEffect, /* useContext,*/ useState} from 'react';
import styles from './DataFlowTasks.module.scss';
import DataFlowList from './DataFlowList/DataFlowList';
import DataFlaws from '../../../assets/jsons/DataFlaws.json'
import MainLayout from '../../Layout/main-layout.component';
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

    setPendingDataFlows([...pendingDataFlows, ...arrayPending])
    setAcceptedDataFlows([...acceptetDataFlows, ...arrayAccepted])   

  }, []);

  return (
    <MainLayout>
      <div className="rep-container">
        <div className="rep-row">

          <div className="subscribe-df rep-col-xs-12 rep-col-md-2">
            {/*TODO  Create component for this button */}
            <h3 className="subscribe-df__title">DATA FLOWS</h3>
            <button className="subscribe-df__btn">+ Subscribe to a data flow</button>
          </div>

          <div className="subscribe-df rep-col-xs-12 rep-col-md-10">
            <DataFlowList 
              listContent={pendingDataFlows} 
              listType="pending" 
              listTitle="Pending data flows." 
              listDescription="You are required to accept and report data to these data flows" 
            />
            <DataFlowList 
              listContent={acceptetDataFlows} 
              listType="accepted"
              listTitle="My data flows." 
              listDescription="Please proceed to report before deadline" 
              /> 
          </div>

        </div>
      </div>
      
    </MainLayout>
  );
}
export default DataFlowTasks;
