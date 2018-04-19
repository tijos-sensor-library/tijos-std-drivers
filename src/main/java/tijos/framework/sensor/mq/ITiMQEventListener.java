package tijos.framework.sensor.mq;

/*
 * Event listener for TiMQ
 * 
 */
public interface ITiMQEventListener {	

	/**
	 * notification of change of threshold value detection value
	 * 
	 * @param mq the current MQn object
	 */
	public void onThresholdNotify(TiMQ mq);
}
