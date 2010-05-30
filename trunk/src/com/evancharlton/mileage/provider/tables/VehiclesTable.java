package com.evancharlton.mileage.provider.tables;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.evancharlton.mileage.dao.Dao;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.FillUpsProvider;

public class VehiclesTable extends ContentTable {
	// make sure it's globally unique
	private static final int VEHICLES = 40;
	private static final int VEHICLE_ID = 41;

	public static final String VEHICLES_URI = "vehicles/";
	public static final Uri BASE_URI = Uri.withAppendedPath(FillUpsProvider.BASE_URI, VEHICLES_URI);

	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evancharlton.vehicles";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evancharlton.vehicle";

	public static String[] PROJECTION = new String[] {
			Dao._ID,
			Vehicle.TITLE,
			Vehicle.DESCRIPTION,
			Vehicle.YEAR,
			Vehicle.MAKE,
			Vehicle.MODEL,
			Vehicle.VEHICLE_TYPE,
			Vehicle.DEFAULT_TIME,
			Vehicle.PREF_DISTANCE_UNITS,
			Vehicle.PREF_VOLUME_UNITS,
			Vehicle.PREF_ECONOMY_UNITS,
			Vehicle.PREF_CURRENCY
	};

	@Override
	protected Class<? extends Dao> getDaoType() {
		return Vehicle.class;
	}

	@Override
	public int delete(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTableName() {
		return "vehicles";
	}

	@Override
	public String getType(int type) {
		switch (type) {
			case VEHICLES:
				return CONTENT_TYPE;
			case VEHICLE_ID:
				return CONTENT_ITEM_TYPE;
		}
		return null;
	}

	@Override
	public String init() {
		// FIXME: hardcoded strings = bad!
		return new InsertBuilder().add(Vehicle.TITLE, "Default vehicle").add(Vehicle.DESCRIPTION, "Auto-generated vehicle").add(Vehicle.DEFAULT_TIME,
				System.currentTimeMillis()).add(Vehicle.MAKE, "Android").add(Vehicle.MODEL, "Mileage").add(Vehicle.YEAR, "2010").add(
				Vehicle.VEHICLE_TYPE, 1).build();
	}

	@Override
	public long insert(int type, SQLiteDatabase db, ContentValues initialValues) {
		switch (type) {
			case VEHICLES:
				return db.insert(getTableName(), null, initialValues);
		}
		return -1L;
	}

	@Override
	public boolean query(int type, Uri uri, SQLiteQueryBuilder queryBuilder) {
		switch (type) {
			case VEHICLES:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
				return true;
			case VEHICLE_ID:
				queryBuilder.setTables(getTableName());
				queryBuilder.setProjectionMap(buildProjectionMap(PROJECTION));
				queryBuilder.appendWhere(Dao._ID + " = " + uri.getPathSegments().get(1));
				return true;
		}
		return false;
	}

	@Override
	public void registerUris(UriMatcher uriMatcher) {
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, VEHICLES_URI, VEHICLES);
		uriMatcher.addURI(FillUpsProvider.AUTHORITY, VEHICLES_URI + "#", VEHICLE_ID);
	}

	@Override
	public int update(int match, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (match) {
			case VEHICLE_ID:
				return db.update(getTableName(), values, Vehicle._ID + " = ?", new String[] {
					values.getAsString(Vehicle._ID)
				});
		}
		return -1;
	}

	@Override
	public String upgrade(int currentVersion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultSortOrder() {
		return Vehicle.DEFAULT_TIME + " desc";
	}
}
