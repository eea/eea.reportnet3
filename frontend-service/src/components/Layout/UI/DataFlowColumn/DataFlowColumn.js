import React from "react";
import { Button } from "primereact/button";
import { IconComponent } from "./duck";
import config from "./duck/config";
import styles from "./DataFlowColumn.module.css";

const DataFlowColumn = props => {
	const { navTitle, dataFlowTitle } = props;
	return (
		<div className="nav rep-col-12 rep-col-sm-3">
			<h2 className={styles.title}>{navTitle}</h2>
			<div className="navSection">
				<input
					type="text"
					id=""
					/* onKeyUp="" */
					className=""
					placeholder="Search data flows"
					title="Type a DataFlow name"
				/>
			</div>
			<div className="navSection">
				<h4 className={styles.title}>
					<IconComponent icon={config.icons.shoppingCart} />
					{dataFlowTitle}
				</h4>
				<Button
					icon={config.icons.plus}
					label="Suscribe to a data flow"
					className="p-button-primary"
				/>
			</div>
		</div>
	);
};

export default DataFlowColumn;
