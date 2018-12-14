package org.uu.nl.analyze.progress;

public class Result {
	
	public double[] vector;
	public String uri;
	public double distance;
	
	public Result() {}
	
	public Result(String uri, double distance, double[] vector) {
		this.uri = uri;
		this.distance = distance;
		this.vector = vector;
	}
	
	public double[] getVector() {
		return vector;
	}
	public void setVector(double[] vector) {
		this.vector = vector;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	
}
