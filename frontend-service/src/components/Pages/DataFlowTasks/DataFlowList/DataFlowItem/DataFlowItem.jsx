import React from 'react'

const DataFlowItem = (props) => {

    const [itemsArray] = props.itemsArray;
    const{isPending} = props;


   if (isPending && itemsArray) {
    return(
      <div className="wrap-card-component-df">
        <div className="title-card-component-df">
          <h2>Pending data flows. </h2>
          <p>You are required to accept and report data to these data flows</p>
        </div>

        {itemsArray.map(dataFlow => (
            <div key={dataFlow.id} className="card-component-df rep-row ">
              <div className="card-component-df-icon rep-col-xs-12 rep-col-md-1 ">
                <i className="material-icons rep-icon rep-u-bg-secondary">
                  layers
                </i>
              </div>
  
              <div className="card-component-df-content rep-col-xs-12 rep-col-md-11 rep-col-xl-9">
                <div className="card-component-df-content-date">
                  <span>{dataFlow.date}</span>
                </div>
                <p className="card-component-df-content-title">
                  {dataFlow.title}
                </p>
  
                <p>{dataFlow.description}</p>
              </div>
  
              <div className="card-component-df-btn rep-col-xs-12 rep-col-xl-2">
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
              </div>
            </div>
          ))}
      </div>
    )
   } else if(isPending === false && itemsArray ) {
    return(
      <div className="wrap-card-component-df">
        <div className="title-card-component-df">
          <h2>My data flows. </h2>
          <p>Please proceed to report before deadline</p>
        </div>
        {itemsArray.map(dataFlow => (
          <div key={dataFlow.id} className="card-component-df rep-row ">
            <div className="card-component-df-icon rep-col-xs-12 rep-col-md-1 ">
              <i className="material-icons rep-icon rep-u-bg-secondary">
                layers
              </i>
            </div>

            <div className="card-component-df-content rep-col-xs-12 rep-col-md-11 rep-col-xl-9">
              <div className="card-component-df-content-date">
                <span>{dataFlow.date}</span>
              </div>
              <p className="card-component-df-content-title">
                {dataFlow.title}
              </p>

              <p>{dataFlow.description}</p>
            </div>

            <div className="card-component-df-btn rep-col-xs-12 rep-col-xl-2">
              <i className="material-icons rep-icon rep-u-color-primary">
                message
              </i>

              <i className="material-icons rep-icon rep-u-color-primary">
                share
              </i>
            </div>
          </div>
        ))}
        
      </div>
    )
   }else{
     return <></>
   }
}

export default DataFlowItem
