package org.qrone.android.tester;

import java.util.List;

import org.qrone.android.util.Asyncer;
import org.qrone.android.util.Asyncer.Flag;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class TesterActivity extends Activity {
	private final String TAG = "Activity";
	
	private TextView tv;
	private List<String> loglist;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv = (TextView)findViewById(R.id.textView1);
        
        tv.append("Started...\n");
        
        testHIPRI();
        
    }
    
    
    public void testHIPRI(){
    	final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	new Asyncer().drawer(new Asyncer.Task() {
			@Override
			public void task(Asyncer a, Flag f) {
		    	NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI);
		    	log(info.toString());
		    	
		    	cm.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		    	log("startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, \"enableHIPRI\");");
		    	
		    	f.set("time", System.currentTimeMillis());
		    	f.set("state", info.getState());
			}
		}).loopdrawer(new Asyncer.Loop() {
			@Override
			public boolean loop(Asyncer a, Flag f) {
				long time = (Long)f.get("time");
				State state = (State)f.get("state");

	    		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI);
	    		if(state != info.getState()){
	    	    	log(info.toString());
			    	f.set("state", info.getState());
	    		}
	    		
	    		if(System.currentTimeMillis() - time > 30000){
	    			error("waiting TYPE_MOBILE_HIPRI=CONNECTED timeout.");
	    			return false;
	    		}

	    		if(state == NetworkInfo.State.CONNECTED){
	    			return false;
	    		}
		    	return true;
			}
		}).drawer(new Asyncer.Task() {
			@Override
			public void task(Asyncer a, Flag f) {
				cm.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
				log("stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, \"enableHIPRI\");");
			}
		})
		.go();

    }

    public void error(String l){
    	tv.append("ERROR - " + l + "\n");
    }
    
    public void log(String l){
    	tv.append(l + "\n");
    }
}