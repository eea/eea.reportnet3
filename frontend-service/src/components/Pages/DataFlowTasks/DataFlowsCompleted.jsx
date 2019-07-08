import React, { useEffect, useContext, useState } from "react";
import styles from "./DataFlowTasks.module.scss";
import DataFlowList from "./DataFlowList/DataFlowList";
import DataFlaws from "../../../assets/jsons/DataFlaws2.json";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import ResourcesContext from "../../Context/ResourcesContext";
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
//import ResourcesContext from '../../Context/ResourcesContext';

const DataFlowsCompleted = () => {
	const resources = useContext(ResourcesContext);
	const [pendingDataFlows, setPendingDataFlows] = useState([]);
	const [acceptetDataFlows, setAcceptedDataFlows] = useState([]);


	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call
		const jsonMimic = DataFlaws;

		const arrayPending = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "pending"
		);
		const arrayAccepted = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "accepted"
		);

		setPendingDataFlows([...arrayPending]);
		setAcceptedDataFlows([...arrayAccepted]);
	}, []);

	return (
		<MainLayout>
			<div className="rep-container">
				<div className="rep-row">
					<DataFlowColumn navTitle={resources.messages['dataFlow']} search={false} />
					<div className="subscribe-df rep-col-xs-12 rep-col-md-9">
						<DataFlowList
							listContent={pendingDataFlows}
							listType="pending"
							listTitle={resources.messages["pendingDataFlowTitle"]}
							listDescription={resources.messages["pendingDataFlowText"]}
						/>
						<DataFlowList
							listContent={acceptetDataFlows}
							listType="accepted"
							listTitle={resources.messages["acceptedDataFlowTitle"]}
							listDescription={resources.messages["acceptedDataFlowText"]}
						/>
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowsCompleted;
