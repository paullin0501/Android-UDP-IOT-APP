package com.rexlite.rexlitebasicnew.RoomDataBase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Shortcut.class},version = 2,exportSchema = true)//資料綁定的Getter-Setter,資料庫版本,是否將資料導出至文件
public abstract class ShortcutDataBase  extends RoomDatabase {
    public static final String DB_NAME = "ShortcutData.db";//資料庫名稱
    private static volatile ShortcutDataBase instance;

    public static synchronized ShortcutDataBase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ShortcutDataBase.class,DB_NAME).addMigrations(MARGIN_1to2).build();
        }
        return instance;
    }

    private static ShortcutDataBase create(final Context context){
        return Room.databaseBuilder(context,ShortcutDataBase.class,DB_NAME).build();
    }
    public static Migration MARGIN_1to2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE \"ShortcutTable\"  ADD COLUMN type TEXT ");


        }
    };
    @Override
    public void clearAllTables() {

    }
    public abstract ShortcutDataDao getShortcutDataDao();//設置對外接口
}
