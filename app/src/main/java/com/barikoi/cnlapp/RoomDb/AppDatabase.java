package com.barikoi.cnlapp.RoomDb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {OrderList.class}, version = 1, exportSchema = false)
@TypeConverters(DataConvertor.class)
public abstract class AppDatabase extends RoomDatabase {

	public abstract OrderListDao orderListDao();

	public static AppDatabase INSTANCE;
	private static final Object sLock = new Object();


	public static AppDatabase getInstance(Context context) {
		synchronized (sLock) {
			if (INSTANCE == null) {
				INSTANCE = Room.databaseBuilder(context,
						AppDatabase.class, "cnlapp.db")
						.allowMainThreadQueries()
						.fallbackToDestructiveMigration()
						.build();
			}
			return INSTANCE;
		}
	}
}
