package tijos.framework.sensor.general;

/*
 * Event listener for TiGeneralSensor
 * 
 */
public interface ITiGeneralSensorEventListener {
	/**
	 * notification when the value is beyond of the threshold value 
	 * 
	 * @param sensor the sensor object
	 */
	void onThresholdNotify(TiGeneralSensor sensor);	
}
