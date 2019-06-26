import React from "react";
import IconComponent from "../../../../Layout/UI/icon-component";
import primeIcons from "../../../../../conf/prime.icons";

const DataFlowItem = props => {
	const { itemContent, listType } = props;
	return (
		<div className="card-component-df rep-row ">
			<div className="card-component-df-icon rep-col-xs-12 rep-col-md-1 ">
				<IconComponent icon={`${primeIcons.icons.clone}`} />
			</div>

			<div className="card-component-df-content rep-col-xs-12 rep-col-md-11 rep-col-xl-9">
				<div className="card-component-df-content-date">
					<span>{itemContent.date}</span>
				</div>
				<p className="card-component-df-content-title">{itemContent.title}</p>

				<p>{itemContent.description}</p>
			</div>

			<div className="card-component-df-btn rep-col-xs-12 rep-col-xl-2">
				{listType === "pending" ? (
					<>
						<button type="button" className="rep-button rep-button--primary">
							Accept
						</button>

						<button
							type="button"
							className="rep-button rep-button--primary"
							disabled
						>
							Reject
						</button>
					</>
				) : (
					<>
						<IconComponent icon={`${primeIcons.icons.comment}`} />
						<IconComponent icon={`${primeIcons.icons.share}`} />
					</>
				)}
			</div>
		</div>
	);
};

export default DataFlowItem;
