package org.qrone.android.tester;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.qrone.android.util.Asyncer;
import org.qrone.android.util.Asyncer.Flag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
	private int error = 0;
	private int success = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tv = (TextView)findViewById(R.id.textView1);
        
        tv.append("Started...\n");
        
        Asyncer hipritest = testHIPRI();
        tv.append("hiprisize" + hipritest.size());
        Asyncer a = new Asyncer();
        for (int i = 0; i < 100; i++) {
            a = a.add(hipritest);
		}
        a.drawer(new Asyncer.Task() {
			@Override
			public void run(Flag arg1) {
				log("Test:" + (success+error) + " / Success:" + success + " / Error:" + error);
		    	
				new AlertDialog.Builder(TesterActivity.this)
				.setTitle("Result")
				.setMessage("Test:" + (success+error) + " / Success:" + success + " / Error:" + error)
				.show();

			}
		})
        .progress(this, "Testing").go();
        
    }
    
    
    public Asyncer testHIPRI(){
		final DefaultHttpClient client = new DefaultHttpClient();
    	final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	return new Asyncer().drawer(new Asyncer.Task() {
			@Override
			public void run(Flag f) {
		    	NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI);
		    	log(info.toString());
		    	
		    	cm.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		    	log("startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, \"enableHIPRI\");");
		    	
		    	f.set("time", System.currentTimeMillis());
		    	f.set("state", info.getState());
			}
		}).loopdrawer(new Asyncer.Loop() {
			@Override
			public boolean loop(Flag f) {
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
		}).worker(new Asyncer.Task() {
			@Override
			public void run(Flag f) {
				HttpGet method = new HttpGet("http://www.google.com/");
				HttpResponse response;
				try {
					response = client.execute(method);
					f.set("googleresult", response);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).drawer(new Asyncer.Task() {
			
			@Override
			public void run(Flag f) {
				HttpResponse r = (HttpResponse)f.get("googleresult");
				int status = r.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK){
					error("httpstatus" + status);
				}else{
					log("httpstatus" + status);
				}
			}
		}).drawer(new Asyncer.Task() {
			@Override
			public void run(Flag f) {
				cm.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
				log("stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, \"enableHIPRI\");");
				
			}
		}).loopdrawer(new Asyncer.Loop() {
			@Override
			public boolean loop(Flag f) {
				long time = (Long)f.get("time");
				State state = (State)f.get("nstate");

	    		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    		if(state != info.getState()){
	    	    	log(info.toString());
			    	f.set("nstate", info.getState());
	    		}
	    		
	    		if(System.currentTimeMillis() - time > 30000){
	    			error("waiting TYPE_MOBILE=CONNECTED timeout.");
	    			return false;
	    		}

	    		if(state == NetworkInfo.State.CONNECTED){
	    			return false;
	    		}
		    	return true;
			}
		}).worker(new Asyncer.Task() {
			@Override
			public void run(Flag f) {
				HttpGet method = new HttpGet("http://www.google.com/");
				HttpResponse response;
				try {
					response = client.execute(method);
					f.set("googleresult", response);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).drawer(new Asyncer.Task() {
			@Override
			public void run(Flag f) {
				HttpResponse r = (HttpResponse)f.get("googleresult");
				int status = r.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK){
					error("httpstatus: " + status);
				}else{
					log("httpstatus: " + status);
				}
			}
		});
    }

    public void error(String l){
    	tv.append("ERROR - " + l + "\n");
		error++;
    }
    
    public void log(String l){
    	tv.append(l + "\n");
		success++;
    }
}