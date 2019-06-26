import React from "react";
import IconComponent from "../../../../Layout/UI/icon-component";
import primeIcons from "../../../../../conf/prime.icons";
import styles from './DataFlowItem.module.scss'

const DataFlowItem = props => {
	const { itemContent, listType } = props;
	return (
		<div className={`${styles.container} rep-row`}>
			<div className={`${styles.card_component_icon} rep-col-xs-12 rep-col-md-1 `}>
				<IconComponent icon={`${primeIcons.icons.clone}`} className={`${styles.card_component_icon_i}`}/>
			</div>

			<div className={`${styles.card_component_content} rep-col-xs-12 rep-col-md-11 rep-col-xl-9`}>
				<div className={`${styles.card_component_content_date}`}>
					<span>{itemContent.date}</span>
				</div>
				<p className={`${styles.card_component_content_title}`}>{itemContent.title}</p>

				<p>{itemContent.description}</p>
			</div>

			<div className={`${styles.card_component_btn} rep-col-xs-12 rep-col-xl-2`}>
				{listType === "pending" ? (
					<>
						<button type="button" className={`${styles.rep_button}`}>
							Accept
						</button>

						<button
							type="button"
							className={`${styles.rep_button}`}
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
