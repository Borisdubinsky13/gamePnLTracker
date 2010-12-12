/**
 * 
 */
package gamePnLTracker.jar;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Boris
 *
 */
public class AboutHandler extends Activity
{
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
}
