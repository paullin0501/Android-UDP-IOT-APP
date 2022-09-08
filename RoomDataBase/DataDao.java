package com.rexlite.rexlitebasicnew.RoomDataBase;

import android.provider.ContactsContract;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DataDao {

    String tableName = "DeviceTable";
    /**=======================================================================================*/
    /**簡易新增所有資料的方法*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)//預設萬一執行出錯怎麼辦，REPLACE為覆蓋
    void insertData(Device myData);

    /**詳細新增所有資料的方法*/
    @Query("INSERT INTO "+tableName+"(deviceId,deviceSN,deviceName,deviceIcon,isHost,superior,subtitle) VALUES(:deviceId,:deviceSN,:deviceName,:deviceIcon,:isHost,:superior,:subtitle)")
    void insertData(String deviceId,String deviceSN,String deviceName,int deviceIcon,boolean isHost,String superior,String subtitle);

    /**=======================================================================================*/
    /**撈取全部資料*/
    @Query("SELECT * FROM " + tableName)
    List<Device> displayAll();

    /**撈取某個deviceid的相關資料*/
    @Query("SELECT * FROM " + tableName +" WHERE deviceId = :deviceId")
    List<Device> findDataByDeviceId(String deviceId);

    /**撈取某個SN的相關資料*/
    @Query("SELECT * FROM " + tableName +" WHERE deviceSN = :deviceSN")
    List<Device> findDataBySN(String deviceSN);

    /**撈取某devicesn and deviceid的資料*/
    @Query("select * from "+tableName+" where deviceId = :deviceId and deviceSN = :deviceSN")
    List<Device> findDataBySNandID(String deviceId,String deviceSN);

    /**=======================================================================================*/
    /**簡易更新資料的方法*/
    @Update
    void updateData(Device myData);

    /**詳細更新資料的方法*/
    @Query("UPDATE "+tableName+" SET deviceName = :deviceName WHERE id = :id" )
    void updateData(int id,String deviceName);

    /**更新名稱和subtitle的方法*/
    @Query("UPDATE "+tableName+" SET deviceName = :deviceName,subtitle = :subtitle WHERE id = :id" )
    void updateDeviceNameAndSubtitle(int id,String deviceName,String subtitle);

    /**更新名稱的方法*/
    @Query("UPDATE "+tableName+" SET deviceName = :deviceName WHERE id = :id" )
    void updateDeviceName(int id,String deviceName);

    /**更新Superior的方法*/
    @Query("UPDATE "+tableName+" SET superior = :superior WHERE deviceId = :deviceId and deviceSN = :deviceSN" )
    void updateDeviceSuperior(String superior,String deviceId,String deviceSN);

    /**=======================================================================================*/
    /**簡單刪除資料的方法*/
    @Delete
    void deleteData(Device myData);

    /**複雜刪除資料的方法*/
    @Query("DELETE  FROM " + tableName + " WHERE id = :id")
    void deleteData(int id);

    /**刪除特定裝置*/
    @Query("DELETE  FROM " + tableName + " WHERE deviceId = :deviceId and deviceSN = :deviceSN" )
    void deleteDevice(String deviceId,String deviceSN);

    /**清除資料表*/
    @Query("DELETE FROM " + tableName)
    void nukeTable();
    /**=======================================================================================*/
    //刪除重複資料
   /* @Query("DELETE FROM test
            WHERE id NOT IN (SELECT MIN(id) FROM test GROUP BY name, lastName)")
    void deleteDuplicates();*/

}
