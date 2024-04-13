package org.ecocompass.api.response;

import org.ecocompass.core.Reroute.Incident;

public class TrafficResponse {
    public boolean reRoute;
    public Incident incident;

    public TrafficResponse() {
        this.reRoute = false;
        this.incident = null;
    }

    public void setReRoute(boolean reRoute) { this.reRoute = reRoute;}

    public void setIncident(Incident incident) {this.incident = incident;}

    public boolean getReRoute() {return this.reRoute;}

    public Incident getIncident() {return this.incident;}
}
