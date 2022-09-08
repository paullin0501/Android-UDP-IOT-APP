package com.rexlite.rexlitebasicnew.RoomDataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface ShortcutDataDao {
    String tableName = "ShortcutTable";
    /**=======================================================================================*/
    /**簡易新增所有資料的方法*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)//預設萬一執行出錯怎麼辦，REPLACE為覆蓋
    void insertData(Shortcut myData);

    /**詳細新增所有資料的方法*/
    @Query("INSERT INTO "+tableName+"(name,icon,deviceType,hostDeviceSN,deciveCH,type) VALUES(:name,:icon,:deviceType,:hostDeviceSN,:deviceCH,:type)")
    void insertData(String name,int icon,String deviceType,String hostDeviceSN,String deviceCH,String type);
    /**=======================================================================================*/
    /**撈取全部資料*/
    @Query("SELECT * FROM " + tableName)
    List<Shortcut> displayAll();

    /**撈取某hostSN  and deviceCH  的資料 */
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deciveCH = :deviceCH")
    List<Shortcut> findDataByhostSNandCH(String hostDeviceSN,String deviceCH);

    /**撈取某hostSN  and type  的資料 */
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deviceType = :deviceType")
    List<Shortcut> findDataByhostSNandType(String hostDeviceSN,String deviceType);

    /**撈取某hostSN  and type and ch 的資料 */
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deviceType = :deviceType and  deciveCH = :deciveCH")
    List<Shortcut> findDataByhostSNandTypeandCH(String hostDeviceSN,String deviceType,String deciveCH);

    /**撈取某hostSN and deviceCH and deviceType and isShow的資料 用來查詢是否加入過捷徑*/
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deciveCH = :deviceCH and deviceType = :deviceType and isShow = :isShow")
    List<Shortcut> findDataByCHandSNandType(String hostDeviceSN,String deviceCH,String deviceType,boolean isShow);

    /**撈取某hostSN and deviceCH and deviceType的資料 用來查詢是否加入過捷徑*/
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deciveCH = :deviceCH and deviceType = :deviceType")
    List<Shortcut> findByCHandSNandType(String hostDeviceSN,String deviceCH,String deviceType);

    /**透過isShow查看是否顯示在主頁*/
    @Query("select * from "+tableName+" where isShow = :isShow")
    List<Shortcut> findisShow(boolean isShow);

    /**透過DeviceSN還有DeviceType判斷是否加入過shortcut*/
    @Query("select * from "+tableName+" where hostDeviceSN = :hostDeviceSN and deviceType = :deviceType")
    List<Shortcut> findDeviceSNandDeviceType(String hostDeviceSN,String deviceType);

    /**對deviceCH模糊搜尋*/
    @Query("SELECT * FROM "+tableName+ " WHERE deciveCH LIKE '%' || :searchName || '%' and hostDeviceSN =:hostDeviceSN ")
    List<Shortcut> fuzzySearchDeviceCH(String searchName,String hostDeviceSN);
    /**=======================================================================================*/
    /**簡易更新資料的方法*/
    @Update
    void updateData(Shortcut myData);

    /**詳細更新資料的方法*/
    @Query("UPDATE "+tableName+" SET name = :name WHERE id = :id" )
    void updateData(int id,String name);

   /* *//**詳細更新資料的方法*//*
    @Query("UPDATE "+tableName+" SET name = :name WHERE id = :id" )
    void updateData(int id,Shortcut shortcut);*/

    /**更新捷徑資料的方法*/
    @Query("UPDATE "+tableName+" SET icon = :icon ,isShow = :isShow WHERE id = :id" )
    void updateShowandicon(int id,int icon,boolean isShow);

    /**更新捷徑資料的方法*/
    @Query("UPDATE "+tableName+" SET type = :type WHERE id = :id" )
    void updateTypeWithID(int id,String type);

    /**更新MaxScene捷徑資料的方法*/
    @Query("UPDATE "+tableName+" SET icon = :icon ,isShow = :isShow,name = :name WHERE id = :id" )
    void updateMaxSceneShortcut(int id,int icon,boolean isShow,String name);
    /**只更新MaxScene捷徑名稱的方法*/
    @Query("UPDATE "+tableName+" SET name = :name,isShow = :isShow WHERE id = :id" )
    void updateMaxSceneNameAndShow(int id,String name,boolean isShow);
    /**只更新MaxScene捷徑是否開啟的方法*/
    @Query("UPDATE "+tableName+" SET isShow = :isShow WHERE id = :id" )
    void updateMaxSceneShow(int id,boolean isShow);

    /**只更新MaxScene shortcut icon*/
    @Query("UPDATE "+tableName+" SET icon = :icon WHERE id = :id" )
    void updateMaxSceneOnlyIcon(int icon,int id);

    /**只更新MaxScene shortcut name*/
    @Query("UPDATE "+tableName+" SET name = :name WHERE id = :id" )
    void updateMaxSceneOnlyName(String name,int id);

    /**更新hostDeviceName的方法*/
    @Query("UPDATE "+tableName+" SET hostDeviceName = :hostDeviceName WHERE deviceType = :deviceType and hostDeviceSN = :hostDeviceSN" )
    void updateDeviceHostName(String hostDeviceName,String deviceType,String hostDeviceSN);

    /**更新特定捷徑的type*/
    @Query("UPDATE "+tableName+" SET type = :type WHERE deviceType = :deviceType and hostDeviceSN = :hostDeviceSN and deciveCH = :deciveCH" )
    void updateType(String type,String deviceType,String hostDeviceSN,String deciveCH);

    /**=======================================================================================*/
    /**簡單刪除資料的方法*/
    @Delete
    void deleteData(Shortcut myData);

    /**複雜(?)刪除資料的方法*/
    @Query("DELETE  FROM " + tableName + " WHERE id = :id")
    void deleteData(int id);

    /**刪除特定裝置的shortcut*/
    @Query("DELETE  FROM " + tableName + " WHERE deviceType = :deviceType and hostDeviceSN = :hostDeviceSN" )
    void deleteDeviceShortcut(String deviceType,String hostDeviceSN);

    /**清除資料表*/
    @Query("DELETE FROM " + tableName)
    void nukeTable();


}
