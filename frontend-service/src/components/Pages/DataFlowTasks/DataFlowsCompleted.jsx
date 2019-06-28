import React, { useEffect, useContext, useState } from "react";
//import styles from "./DataFlowTasks.module.scss";
import DataFlowList from "./DataFlowList/DataFlowList";
import DataFlawsCompleted from "../../../assets/jsons/DataFlawsCompleted.json";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import ResourcesContext from "../../Context/ResourcesContext";
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const DataFlowsCompleted = () => {
	const resources = useContext(ResourcesContext);
	const [completedDataFlows, setCompletedDataFlows] = useState([]);

	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call
		const jsonMimic = DataFlawsCompleted;

		setCompletedDataFlows([...jsonMimic]);
		console.log('completedDataFlows', completedDataFlows)
		
	}, []);

	return (
		<MainLayout>
			<div className="rep-container">
				<div className="rep-row">
					<DataFlowColumn navTitle={resources.messages['dataFlow']} search={false} />
					<div className="subscribe-df rep-col-xs-12 rep-col-md-9">
						<DataFlowList
							listContent={completedDataFlows}
							listType="completed"
							listTitle={resources.messages["completedDataFlowTitle"]}
							listDescription={resources.messages["completedDataFlowText"]}
						/>						
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowsCompleted;
