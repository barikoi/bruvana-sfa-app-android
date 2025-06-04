package com.barikoi.cnlapp.RoomDb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.barikoi.cnlapp.order_create.RoomDB.DataConvertor;
import com.barikoi.cnlapp.order_create.RoomDB.OrderList;
import com.barikoi.cnlapp.order_create.RoomDB.OrderListDao;
import com.barikoi.cnlapp.order_create.RoomDB.SaveOrder;
import com.barikoi.cnlapp.order_create.RoomDB.SaveOrderDao;
import com.barikoi.cnlapp.Order_Delivery.RoomDB.UpdateOrder;
import com.barikoi.cnlapp.Order_Delivery.RoomDB.UpdateOrderDao;


@Database(entities = {OrderList.class, SaveOrder.class, UpdateOrder.class}, version = 6, exportSchema = false)
@TypeConverters(DataConvertor.class)
public abstract class AppDatabase extends RoomDatabase {

	public abstract OrderListDao orderListDao();
	public abstract SaveOrderDao saveOrderDao();
	public abstract UpdateOrderDao updateOrderDao();

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
