/**
 * 
 */
package com.gamesPnL;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.ads.*;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * @author Boris
 *
 */
public class GraphData extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="GraphData: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String currentUser = new String();
	DecimalFormat df = new DecimalFormat("#,##0.00");
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		gamesLogger.i(TAG, SubTag + "Starting GraphData");
		super.onCreate(savedInstanceState);
	}
	
	protected void onResume() 
	{
		super.onResume();
	
		GraphicalView mChartView = null;
		setContentView(R.layout.graphdata);
		
		AdView	adView = (AdView)findViewById(R.id.adGraphData);
	    adView.loadAd(new AdRequest());
	    
	    int		i=0;
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
		this.setTitle("User: " + username);
		
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"_id",
				"name",
				"amount",
				"date",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};

		String	query = null;
    	Bundle extras = getIntent().getExtras(); 
    	if(extras != null)
    	{
    		query = extras.getString("queStr");
    	}
    	gamesLogger.i(TAG, SubTag + "Query: " + query);
		Cursor result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		if ( result.getCount() > 0 )
		{
			double[] values = new double[result.getCount()+1];
			double[] vSeries = new double[result.getCount()+1];
			double	sum = 0;
			double	min = 0, max = 0;
			if ( result.moveToFirst() )
			{
				gamesLogger.i(TAG, SubTag + "got result back from provider");
				String value;
				double	fValue;
				values[i++] = 0;
				do
				{
					value = result.getString(result.getColumnIndex("amount"));
					if ( value.equals("") )
						fValue = (float) 0.0;
					else
						fValue = Float.parseFloat(value);
					sum += fValue;
					vSeries[i] = fValue;
					values[i++] = sum;

					if ( i == 1 )
					{
						// First read value. Use it as a min and a max
						min = sum;
						max = sum;
					}
					else
					{
						if ( sum > max)
							max = sum;
						if ( sum < min )
							min = sum;
					}
				} while (result.moveToNext());
	
			}
			else
				gamesLogger.i(TAG, SubTag + "No Data returned from Content Provider");
/*			
			String[] verlabels = new String[] { "$" + df.format(max), "$" + df.format(((min+max)/2)), "$" + df.format(min) };
			String[] horlabels = new String[] { "", "" };
			GraphView graphView = new GraphView(this, values, "Running Total", horlabels, verlabels, GraphView.LINE);
			setContentView(graphView);
*/
			XYSeries sDataset = new XYValueSeries("Earnings");
			XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
			
		    int seriesLength = i;
		    sDataset.clear();
		    gamesLogger.i(TAG, SubTag + "Adding " + i + " elements" );
		    for (int k = 0; k < seriesLength; k++) 
		    {
		    	gamesLogger.i(TAG, SubTag + "Adding " + values[k]);
		    	sDataset.add(k, values[k]);
		    }
		    mDataset.addSeries(sDataset);
		    gamesLogger.i(TAG, SubTag + "Dataset has been populated");
		    int[] colors = new int[] { Color.CYAN};
		    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE};

		    XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		    mRenderer.setAxisTitleTextSize(16);
		    mRenderer.setChartTitleTextSize(20);
		    mRenderer.setLabelsTextSize(15);
		    mRenderer.setLegendTextSize(15);
		    mRenderer.setPointSize(5f);
		    mRenderer.setXLabels(0);
		    mRenderer.setShowGrid(true);
		    mRenderer.setDisplayChartValues(true);
		    mRenderer.setChartTitle("Earnings");
		    mRenderer.setXTitle("Date");
		    mRenderer.setYTitle("$");
		    mRenderer.setXAxisMin(0);
		    mRenderer.setXAxisMax(i);
		    mRenderer.setYAxisMin((double)min);
		    mRenderer.setYAxisMax(max);
		    mRenderer.setAxesColor(Color.WHITE);
		    mRenderer.setLabelsColor(Color.WHITE);
		    
		    // mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		    int length = colors.length;
		    for ( int k = 0; k < length; k++) 
		    {
		    	XYSeriesRenderer r = new XYSeriesRenderer();
		    	r.setColor(colors[k]);
		    	r.setPointStyle(styles[k]);
		    	mRenderer.addSeriesRenderer(r);
		    }
		    length = mRenderer.getSeriesRendererCount();
		    for (int k = 0; k < length; k++) 
		    {
		      ((XYSeriesRenderer) mRenderer.getSeriesRendererAt(k)).setFillPoints(true);
		    }
		    gamesLogger.i(TAG, SubTag + "mRenderer and mDataset are set");
			if (mChartView == null) 
			{
				try 
				{
					LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
					gamesLogger.i(TAG, SubTag + "mDataset count: " + mDataset.getSeriesCount());
					mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
					layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				}
				catch (Exception e)
				{
					gamesLogger.e(TAG, SubTag + e.getMessage());
				}
			} 
		}
		else
        {
        	int duration = Toast.LENGTH_LONG;
			String text = "Need to add at least one result!";
			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
			finish();
        }
	}
}
