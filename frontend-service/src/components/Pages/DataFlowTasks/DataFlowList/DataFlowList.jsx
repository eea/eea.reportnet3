import React from "react";
import DataFlowItem from "./DataFlowItem/DataFlowItem";
import styles from "./DataFlowList.module.scss";

const DataFlowList = props => {
	const {
		listTitle,
		listDescription,
		listContent,
		listType,
		dataFetch
	} = props;

	return (
		<div className={styles.wrap}>
			<h2>{listTitle} </h2>
			<p>{listDescription}</p>

			{listContent.map(item => {
				return (
					<DataFlowItem
						key={item.id}
						itemContent={item}
						listType={listType}
						dataFetch={dataFetch}
					/>
				);
			})}
		</div>
	);
};

export default DataFlowList;
