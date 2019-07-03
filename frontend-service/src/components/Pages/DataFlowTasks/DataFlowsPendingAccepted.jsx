import React, { useEffect, useContext, useState, Fragment } from "react";
import ResourcesContext from "../../Context/ResourcesContext";

import DataFlowList from "./DataFlowList/DataFlowList";

const DataFlowsPendingAccepted = props => {
	const { listData } = props;
	const resources = useContext(ResourcesContext);
	const [pendingDataFlows, setPendingDataFlows] = useState([]);
	const [acceptetDataFlows, setAcceptedDataFlows] = useState([]);

	useEffect(() => {
		const arrayPending = listData.filter(
			jsonData => jsonData.dataFlowStatus === "pending"
		);
		const arrayAccepted = listData.filter(
			jsonData => jsonData.dataFlowStatus === "accepted"
		);

		setPendingDataFlows([...arrayPending]);
		setAcceptedDataFlows([...arrayAccepted]);
	}, [listData]);

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
