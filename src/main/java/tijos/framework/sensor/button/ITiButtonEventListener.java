package tijos.framework.sensor.button;

/*
 * Event listener for TiButton 
 * 
 */
public interface ITiButtonEventListener {	
	/**
	 * the event is triggered when the button is pressed
	 * @param button  the button which is pressed
	 */
	public void onPressed(TiButton button);
	
	/**
	 * the event is triggered when the button is released
	 * @param button the button which is released
	 */
	public void onReleased(TiButton button);
}
