import React, { Fragment } from "react";
import Navigation from "../Navigation/Navigation";
import Footer from "./Footer/Footer";

const MainLayout = ({ children }) => (
	<Fragment>
		<Navigation />
		{children}
		<Footer />
	</Fragment>
);
export default MainLayout;
