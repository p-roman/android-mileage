package com.evancharlton.mileage.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.evancharlton.mileage.VehicleStatisticsActivity;
import com.evancharlton.mileage.dao.CachedValue;
import com.evancharlton.mileage.dao.Fillup;
import com.evancharlton.mileage.dao.FillupSeries;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.math.Calculator;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.tables.CacheTable;
import com.evancharlton.mileage.provider.tables.FillupsTable;

public class VehicleStatisticsTask extends AsyncTask<Cursor, Integer, Integer> {
	private VehicleStatisticsActivity mActivity;
	private ContentResolver mContentResolver;
	private int mProgress = 0;
	private int mTotal = 0;

	public void setActivity(VehicleStatisticsActivity activity) {
		mActivity = activity;
		mContentResolver = activity.getContentResolver();
	}

	@Override
	protected void onPreExecute() {
		mActivity.setProgressBarVisible(true);
	}

	@Override
	protected Integer doInBackground(Cursor... cursors) {
		// delete the cache
		String[] args = new String[] {
			String.valueOf(mActivity.getVehicle().getId())
		};
		mContentResolver.delete(CacheTable.BASE_URI, CachedValue.ITEM + " = ?", args);

		String selection = Fillup.VEHICLE_ID + " = ?";
		args = new String[] {
			String.valueOf(mActivity.getVehicle().getId())
		};

		Cursor cursor = mContentResolver.query(FillupsTable.BASE_URI, FillupsTable.PROJECTION, selection, args, Fillup.ODOMETER + " asc");
		mTotal = cursor.getCount();
		Log.d("CalculateTask", "Recalculating...");
		// recalculate a whole bunch of shit
		FillupSeries series = new FillupSeries();
		// math gets understandably funky around Double.(MAX|MIN)_VALUE
		final double MAX = 10000D;
		final double MIN = -10000D;

		double totalVolume = 0D;
		double firstVolume = -1D;
		double minVolume = MAX;
		double maxVolume = MIN;

		double minDistance = MAX;
		double maxDistance = MIN;

		double totalCost = 0D;
		double minCost = MAX;
		double maxCost = MIN;

		double minEconomy = MAX;
		double maxEconomy = MIN;

		double minCostPerDistance = MAX;
		double maxCostPerDistance = MIN;

		double minPrice = MAX;
		double maxPrice = MIN;

		double minLatitude = MAX;
		double maxLatitude = MIN;
		double minLongitude = MAX;
		double maxLongitude = MIN;

		double lastMonthCost = 0D;
		double lastYearCost = 0D;

		final long lastYear = System.currentTimeMillis() - Calculator.YEAR_MS;
		final long lastMonth = System.currentTimeMillis() - Calculator.MONTH_MS;

		final Vehicle vehicle = mActivity.getVehicle();
		while (cursor.moveToNext()) {
			Fillup fillup = new Fillup(cursor);
			series.add(fillup);

			if (fillup.hasPrevious()) {
				double distance = fillup.getDistance();
				if (distance > maxDistance) {
					maxDistance = distance;
					update(Statistics.MAX_DISTANCE, maxDistance);
				} else {
					publishProgress();
				}

				if (distance < minDistance) {
					minDistance = distance;
					update(Statistics.MIN_DISTANCE, minDistance);
				} else {
					publishProgress();
				}

				double economy = Calculator.averageEconomy(vehicle, fillup);
				if (Calculator.isBetterEconomy(vehicle, economy, maxEconomy)) {
					maxEconomy = economy;
					update(Statistics.MAX_ECONOMY, maxEconomy);
				} else {
					publishProgress();
				}

				if (!Calculator.isBetterEconomy(vehicle, economy, minEconomy)) {
					minEconomy = economy;
					update(Statistics.MIN_ECONOMY, minEconomy);
				} else {
					publishProgress();
				}

				double costPerDistance = fillup.getCostPerDistance();
				if (costPerDistance > maxCostPerDistance) {
					maxCostPerDistance = costPerDistance;
					update(Statistics.MAX_COST_PER_DISTANCE, maxCostPerDistance);
				} else {
					publishProgress();
				}

				if (costPerDistance < minCostPerDistance) {
					minCostPerDistance = costPerDistance;
					update(Statistics.MIN_COST_PER_DISTANCE, minCostPerDistance);
				} else {
					publishProgress();
				}

				long timestamp = fillup.getTimestamp();
				if (timestamp >= lastMonth) {
					lastMonthCost += fillup.getTotalCost();
					update(Statistics.LAST_MONTH_COST, lastMonthCost);
				} else {
					publishProgress();
				}

				if (timestamp >= lastYear) {
					lastYearCost += fillup.getTotalCost();
					update(Statistics.LAST_YEAR_COST, lastYearCost);
				} else {
					publishProgress();
				}

			} else {
				publishProgress(8);
			}

			double volume = fillup.getVolume();
			if (firstVolume == -1D) {
				firstVolume = volume;
			} else {
				publishProgress();
			}

			if (volume > maxVolume) {
				maxVolume = volume;
				update(Statistics.MAX_FUEL, maxVolume);
			} else {
				publishProgress();
			}

			if (volume < minVolume) {
				minVolume = volume;
				update(Statistics.MIN_FUEL, minVolume);
			} else {
				publishProgress();
			}

			totalVolume += volume;
			update(Statistics.TOTAL_FUEL, totalVolume);

			double cost = fillup.getTotalCost();
			if (cost > maxCost) {
				maxCost = cost;
				update(Statistics.MAX_COST, maxCost);
			} else {
				publishProgress();
			}

			if (cost < minCost) {
				minCost = cost;
				update(Statistics.MIN_COST, minCost);
			} else {
				publishProgress();
			}

			totalCost += cost;
			update(Statistics.TOTAL_COST, totalCost);

			double price = fillup.getUnitPrice();
			if (price > maxPrice) {
				maxPrice = price;
				update(Statistics.MAX_PRICE, maxPrice);
			} else {
				publishProgress();
			}

			if (price < minPrice) {
				minPrice = price;
				update(Statistics.MIN_PRICE, minPrice);
			} else {
				publishProgress();
			}

			double latitude = fillup.getLatitude();
			if (latitude > maxLatitude) {
				maxLatitude = latitude;
				update(Statistics.NORTH, maxLatitude);
			} else {
				publishProgress();
			}

			if (latitude < minLatitude) {
				minLatitude = latitude;
				update(Statistics.SOUTH, minLatitude);
			} else {
				publishProgress();
			}

			double longitude = fillup.getLongitude();
			if (longitude > maxLongitude) {
				maxLongitude = longitude;
				update(Statistics.EAST, maxLongitude);
			} else {
				publishProgress();
			}

			if (longitude < minLongitude) {
				minLongitude = longitude;
				update(Statistics.WEST, minLongitude);
			} else {
				publishProgress();
			}

			double avgFuel = totalVolume / series.size();
			update(Statistics.AVG_FUEL, avgFuel);

			double avgEconomy = Calculator.averageEconomy(vehicle, series);
			update(Statistics.AVG_ECONOMY, avgEconomy);

			double avgDistance = Calculator.averageDistanceBetweenFillups(series);
			update(Statistics.AVG_DISTANCE, avgDistance);

			double avgCost = Calculator.averageFillupCost(series);
			update(Statistics.AVG_COST, avgCost);

			double avgCostPerDistance = Calculator.averageCostPerDistance(series);
			update(Statistics.AVG_COST_PER_DISTANCE, avgCostPerDistance);

			double avgPrice = Calculator.averagePrice(series);
			update(Statistics.AVG_PRICE, avgPrice);

			double fuelPerDay = Calculator.averageFuelPerDay(series);
			update(Statistics.FUEL_PER_YEAR, fuelPerDay * 365);

			double costPerDay = Calculator.averageCostPerDay(series);
			update(Statistics.AVG_MONTHLY_COST, costPerDay * 30);
			update(Statistics.AVG_YEARLY_COST, costPerDay * 365);
		}

		cursor.close();

		return 0;
	}

	private void update(Statistics.Statistic statistic, double value) {
		statistic.setValue(value);
		final String vehicleId = String.valueOf(mActivity.getVehicle().getId());
		ContentValues values = new ContentValues();
		values.put(CachedValue.VALID, true);
		values.put(CachedValue.VALUE, statistic.getValue());
		String where = CachedValue.KEY + " = ? and " + CachedValue.ITEM + " = ?";
		String[] args = new String[] {
				statistic.getKey(),
				vehicleId
		};
		int num = mContentResolver.update(CacheTable.BASE_URI, values, where, args);
		if (num == 0) {
			values.put(CachedValue.ITEM, vehicleId);
			values.put(CachedValue.KEY, statistic.getKey());
			values.put(CachedValue.GROUP, statistic.getGroup());
			values.put(CachedValue.ORDER, statistic.getOrder());
			mContentResolver.insert(CacheTable.BASE_URI, values);
		}

		publishProgress();
	}

	@Override
	protected void onProgressUpdate(Integer... updates) {
		if (updates.length > 0) {
			mProgress += updates[0];
		} else {
			mProgress += 1;
		}
		mActivity.setProgressValue(mProgress);
		if (mTotal > 0) {
			mActivity.setMax(mTotal * Statistics.STATISTICS.size());
			mTotal = 0;
		}
	}

	@Override
	protected void onPostExecute(Integer done) {
		// FIXME: we can't do this on the UI thread
		Log.d("CalculateTask", "Done recalculating!");
		if (mActivity.getAdapter() == null) {
			mActivity.setAdapter(mActivity.getCursor());
		} else {
			mActivity.getAdapter().notifyDataSetChanged();
		}
		mActivity.setProgressBarVisible(false);
	}
}
