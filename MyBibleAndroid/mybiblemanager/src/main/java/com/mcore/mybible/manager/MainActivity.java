package com.mcore.mybible.manager;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.ServerConfigurartionData;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.common.dto.StatusDTO;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.manager.adapters.StatisticsAdapter;
import com.mcore.mybible.services.client.IBusinessDelegate;
import com.mcore.mybible.services.client.factories.BusinessDelegateFactory;

public class MainActivity extends Activity {

	private ListView lsView;
	
	private TextView txtStatus;

	private StatisticsDTO model;
	
	private Switch swType;
	
	private RelativeLayout background;
	
	private ProgressBar prgStatus;
	
	private ProgressBar prgRefresh;
	
	private ImageButton btnRefresh;
	
	private String token;
	
	private boolean setting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setting = false;
		txtStatus = (TextView) findViewById(R.id.txt_status);
		prgStatus = (ProgressBar) findViewById(R.id.progress_status);
		prgRefresh = (ProgressBar) findViewById(R.id.progress_refresh);
		btnRefresh = (ImageButton) findViewById(R.id.btn_refresh);
		ImageButton btnStatus = (ImageButton) findViewById(R.id.btn_status);
		lsView = (ListView) findViewById(R.id.list_statictics);
		background = (RelativeLayout)findViewById(R.id.relativeLayout2);
		swType = (Switch) findViewById(R.id.sw_type);
		swType.setChecked(false);
		setDataEnabled(false);
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new RefreshOperationTask().execute();
			}
		});
		btnStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new StatusOperationTask().execute();
			}
		});
		if (savedInstanceState != null) {
			token = savedInstanceState.getString("token");
			model = (StatisticsDTO) savedInstanceState.getSerializable("list");
			updateStatistics(model);
		} else {
			btnRefresh.callOnClick();
			btnStatus.callOnClick();
		}
	}
	
	private void setDataEnabled(boolean enabled) {
		background.setBackgroundColor(enabled? Color.TRANSPARENT: Color.GRAY);
		swType.setEnabled(enabled);
	}
	
	private void preStatusResult() {
		prgStatus.setVisibility(View.VISIBLE);
		txtStatus.setVisibility(View.INVISIBLE);
	}
	
	private void processStatusResult(StatusDTO status) {
		prgStatus.setVisibility(View.INVISIBLE);
		txtStatus.setVisibility(View.VISIBLE);
		setting = true;
		if (status == null || status.getResultID() != CommonErrorCodes.ERROR_CODE_NO_ERROR) {
			setDataEnabled(false);
			background.setBackgroundColor(Color.RED);
			return;
		}
		token = status.getSecuritySeed();
		setDataEnabled(true);
		swType.setChecked(status.getFileOpts() != null && status.getFileOpts().equals("_all."));
		txtStatus.setText(status.getStatusDescription());
		swType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting) {
					return;
				}
				ServerConfigurartionData data = new ServerConfigurartionData();
				if (isChecked) {
					data.setTransationlistid("translations_all.props");
				} else {
					data.setTransationlistid("translations.props");
				}
				new ChangeConfigurationTask().execute(String.valueOf(("ss:" + token).hashCode()), data);
			}
		});
		setting = false;
		final String showData = "Status: " + status;
		txtStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, showData, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("list", model);
		outState.putString("token", token);
		super.onSaveInstanceState(outState);
	}
	
	protected void preUpdateStatistics() {
		prgRefresh.setVisibility(View.VISIBLE);
		btnRefresh.setVisibility(View.INVISIBLE);
	}

	protected void updateStatistics(StatisticsDTO result) {
		prgRefresh.setVisibility(View.INVISIBLE);
		btnRefresh.setVisibility(View.VISIBLE);
		model = result;
		StatisticsAdapter adapter = new StatisticsAdapter(this,
				R.layout.statistic_item, model.getDayStatistics());
		lsView.setAdapter(adapter);
	}
	
	protected void preChangeConfig() {
		swType.setEnabled(false);
	}
	
	protected void updateOnChangeConfig(ResultInfoDTO result) {
		swType.setEnabled(true);
		if (swType != null) {
			new StatusOperationTask().execute();
			if (result.getResultID() == CommonErrorCodes.ERROR_CODE_NO_ERROR) {
				Toast.makeText(MainActivity.this, "Update complete", Toast.LENGTH_LONG).show();
			} else {				
				Toast.makeText(MainActivity.this, "Result: " + result.getResultID() + " " + result.getResultDetails(), Toast.LENGTH_LONG).show();
			}
		}
	}

	private class RefreshOperationTask extends
			AsyncTask<Void, Void, StatisticsDTO> {
		
		public RefreshOperationTask() {
			preUpdateStatistics();
		}

		@Override
		protected StatisticsDTO doInBackground(Void... params) {
			IBusinessDelegate serviceClient = BusinessDelegateFactory
					.getNewServiceClient();
			return serviceClient.getStatistics(new StatisticsInDTO(
					"servertokenvalue", 5));
		}

		@Override
		protected void onPostExecute(StatisticsDTO result) {
			updateStatistics(result);
		}
	}

	private class StatusOperationTask extends AsyncTask<Void, Void, StatusDTO> {
		
		@Override
		protected void onPreExecute() {
			preStatusResult();
		}

		@Override
		protected StatusDTO doInBackground(Void... params) {
			IBusinessDelegate serviceClient = BusinessDelegateFactory
					.getNewServiceClient();
			return serviceClient.getStatus();
		}

		@Override
		protected void onPostExecute(StatusDTO result) {
			processStatusResult(result);
		}
	}
	
	private class ChangeConfigurationTask extends AsyncTask<Object, Void, ResultInfoDTO> {

		@Override
		protected void onPreExecute() {
			preChangeConfig();
		}
		
		@Override
		protected ResultInfoDTO doInBackground(Object... params) {
			IBusinessDelegate serviceClient = BusinessDelegateFactory.getNewServiceClient();
			return serviceClient.putConfiguration((String)params[0], (ServerConfigurartionData)params[1]);
		}

		@Override
		protected void onPostExecute(ResultInfoDTO result) {
			updateOnChangeConfig(result);
		}
	}

}
