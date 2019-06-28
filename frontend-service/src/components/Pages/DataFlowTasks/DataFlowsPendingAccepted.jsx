import React, { useEffect, useContext, useState, Fragment } from "react";
import DataFlaws from "../../../assets/jsons/DataFlaws.json";
import ResourcesContext from "../../Context/ResourcesContext";

import DataFlowList from "./DataFlowList/DataFlowList";
import { ProgressSpinner } from "primereact/progressspinner";

const DataFlowsPendingAccepted = () => {
	const resources = useContext(ResourcesContext);
	const [pendingDataFlows, setPendingDataFlows] = useState([]);
	const [acceptetDataFlows, setAcceptedDataFlows] = useState([]);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call
		const jsonMimic = DataFlaws;

		const arrayPending = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "pending"
		);
		const arrayAccepted = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "accepted"
		);
		setTimeout(() => {
			setPendingDataFlows([...arrayPending]);
			setAcceptedDataFlows([...arrayAccepted]);
			setLoading(false);
		}, 2000);
	}, []);

	if (loading) {
		return <ProgressSpinner />;
	}

	return (
		<Fragment>
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
		</Fragment>
	);
};

export default DataFlowsPendingAccepted;
