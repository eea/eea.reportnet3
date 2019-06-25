import React from "react";
import styles from "./DataFlowColumn.module.css";
import IconComponent from "../icon-component";
import { Button } from "primereact/button";

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
					<IconComponent icon="pi-shopping-cart" />
					{dataFlowTitle}
				</h4>
				<Button
					icon="pi pi-plus"
					label="Suscribe to a data flow"
					className="p-button-primary"
				/>
			</div>
		</div>
	);
};

export default DataFlowColumn;
