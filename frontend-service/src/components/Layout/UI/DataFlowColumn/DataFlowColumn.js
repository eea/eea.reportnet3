import React from "react";
import PropTypes from "prop-types";
import { Button } from "primereact/button";
import { IconComponent } from "./duck";
import config from "./duck/config";
import styles from "./DataFlowColumn.module.css";

const DataFlowColumn = ({ navTitle, dataFlowTitle, search = false }) => {
	return (
		<div className="nav rep-col-12 rep-col-sm-3">
			<h2 className={styles.title}>{navTitle}</h2>
			{search && (
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
			)}
			<div className="navSection">
				{dataFlowTitle && (
					<h4 className={styles.title}>
						<IconComponent icon={config.icons.shoppingCart} />
						{dataFlowTitle}
					</h4>
				)}

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
