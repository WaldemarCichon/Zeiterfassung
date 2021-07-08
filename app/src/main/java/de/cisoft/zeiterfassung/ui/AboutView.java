package de.cisoft.zeiterfassung.ui;

import de.cisoft.zeiterfassung.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutView extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Über MPZE-A");
        Button okButton = (Button) findViewById(R.id.btOk);
        okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AboutView.this.finish();
			}
		});
	}
}
