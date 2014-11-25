package se.ltu.trafikgeneratorcoap;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;

public class AbstractActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
