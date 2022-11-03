package com.barikoi.cnlapp.RoomDb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.barikoi.cnlapp.Order_Create.RoomDB.DataConvertor;
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList;
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderListDao;
import com.barikoi.cnlapp.Order_Create.RoomDB.SaveOrder;
import com.barikoi.cnlapp.Order_Create.RoomDB.SaveOrderDao;


@Database(entities = {OrderList.class, SaveOrder.class}, version = 2, exportSchema = false)
@TypeConverters(DataConvertor.class)
public abstract class AppDatabase extends RoomDatabase {

	public abstract OrderListDao orderListDao();
	public abstract SaveOrderDao saveOrderDao();

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
