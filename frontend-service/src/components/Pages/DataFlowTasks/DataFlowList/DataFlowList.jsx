import React from "react";
import DataFlowItem from "./DataFlowItem/DataFlowItem";

const DataFlowList = props => {
	const { listTitle, listDescription, listContent, listType } = props;
	return (
		<div className="wrap-card-component-df">
			<div className="title-card-component-df">
				<h2>{listTitle} </h2>
				<p>{listDescription}</p>
			</div>
			{listContent.map(item => {
				return (
					<DataFlowItem key={item.id} itemContent={item} listType={listType} />
				);
			})}
		</div>
	);
};

export default DataFlowList;
