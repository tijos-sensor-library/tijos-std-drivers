package tijos.framework.sensor.vs1838b;

/*
 * Event listener for TiVS1838BNEC 
 * 
 */
public interface ITiVS1838BNECEventListener {	
	/**
	 * the event is triggered when the command is received
	 * @param vs1838b the vs1838b which command is received
	 */
	public void cmdReceived(TiVS1838BNEC vs1838b);
	
	/**
	 * the event is triggered when the last command is repeat
	 * 
	 * @param vs1838b the vs1838b which last command is repeat
	 */
	public void cmdRepeat(TiVS1838BNEC vs1838b);
}
