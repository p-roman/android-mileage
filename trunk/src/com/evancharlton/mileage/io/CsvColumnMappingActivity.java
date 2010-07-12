package com.evancharlton.mileage.io;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.evancharlton.mileage.ImportActivity;
import com.evancharlton.mileage.R;
import com.evancharlton.mileage.adapters.CsvFieldAdapter;
import com.evancharlton.mileage.io.importers.CsvWizardActivity;
import com.evancharlton.mileage.tasks.CsvColumnReaderTask;

public class CsvColumnMappingActivity extends CsvWizardActivity {
	private CsvColumnReaderTask mColumnReaderTask;
	private final ArrayList<Spinner> mColumnSpinners = new ArrayList<Spinner>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		restoreTask();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mColumnReaderTask;
	}

	private void restoreTask() {
		mColumnReaderTask = (CsvColumnReaderTask) getLastNonConfigurationInstance();

		if (mColumnReaderTask == null) {
			mColumnReaderTask = new CsvColumnReaderTask();
		}
		mColumnReaderTask.attach(this);
		if (mColumnReaderTask.getStatus() == AsyncTask.Status.PENDING) {
			mColumnReaderTask.execute(getIntent().getStringExtra(ImportActivity.FILENAME));
		}
	}

	public void setColumns(String[] columnNames) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final int length = columnNames.length;
		for (int i = 0; i < length; i++) {
			String columnName = columnNames[i];
			Log.d("CsvImportActivity", "Adding a UI mapping for " + columnName);
			View v = inflater.inflate(R.layout.import_csv_mapping, mContainer);
			TextView title = (TextView) v.findViewById(R.id.title);
			title.setText(columnName);
			title.setId(columnName.hashCode());
			Spinner spinner = (Spinner) v.findViewById(R.id.mappings);
			spinner.setAdapter(new CsvFieldAdapter(this));
			spinner.setId(columnName.hashCode());
			spinner.setSelection(i);
			spinner.setTag(columnName);

			mColumnSpinners.add(spinner);
		}
	}

	@Override
	protected boolean buildIntent(Intent intent) {
		intent.setClass(this, CsvVehicleMappingActivity.class);
		for (Spinner columnSpinner : mColumnSpinners) {
			intent.putExtra(columnSpinner.getSelectedItem().toString(), columnSpinner.getSelectedItemPosition());
		}
		return true;
	}
}