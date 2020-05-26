package com.example.amazontracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DBHandler(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        val createItemTable:String = "CREATE TABLE $TABLE_ITEM (" +
                "$COL_URL TEXT PRIMARY KEY," +
                "$COL_NAME TEXT," +
                "$COL_IMG BLOB" +
                ");"

        val createPriceTable:String = "CREATE TABLE $TABLE_PRICE (" +
                "$COL_DATE DATE," +
                "$COL_ITEM_URL TEXT," +
                "$COL_PRICE NUMERIC," +
                "PRIMARY KEY($COL_DATE,$COL_ITEM_URL)," +
                "FOREIGN KEY($COL_ITEM_URL) REFERENCES Item($COL_URL)" +
                ");"

        db?.execSQL(createItemTable)
        db?.execSQL(createPriceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun addItem(item : Item): Boolean {
        val db = writableDatabase
        val doc = Jsoup.connect(item.url).get()
        item.name = doc.getElementById("productTitle").text()
        Log.d("AOOOO", item.url)
        val cv = ContentValues()
        cv.put(COL_URL, item.url)
        cv.put(COL_NAME, item.name)
        val result:Long = db.insert(TABLE_ITEM, null, cv)
        checkPrice(item)
        return result != (-1).toLong()
    }

    fun checkPrice(item: Item){
        val db = writableDatabase
        val date: String = getDate()
        val doc = Jsoup.connect(item.url).get()
        val price = doc.getElementById("priceblock_ourprice").text()
        val cv = ContentValues()
        cv.put(COL_ITEM_URL, item.url)
        cv.put(COL_DATE, date)
        cv.put(COL_PRICE, price)
        val result:Long = db.insert(TABLE_PRICE, null, cv)
    }

    fun getItem() : MutableList<Item>{
        val result: MutableList<Item> = ArrayList()
        val db:SQLiteDatabase = readableDatabase
        val date = getDate()
        Log.d("today", date)
        val queryResult = db.rawQuery("SELECT * from $TABLE_ITEM INNER JOIN $TABLE_PRICE ON $TABLE_ITEM.$COL_URL=$TABLE_PRICE.$COL_ITEM_URL WHERE $COL_DATE='$date'", null)

        if(queryResult.moveToFirst()){
            do{
                val item = Item()
                item.name = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                item.url = queryResult.getString(queryResult.getColumnIndex(COL_URL))
                item.price = queryResult.getFloat(queryResult.getColumnIndex(COL_PRICE))
                //Log.d("name", item.name)
                //Log.d("url", item.url)
                //Log.d("price", item.price.toString())
                result.add(item)
            }while(queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }

    fun getDate() : String{
        var date = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            date =  current.format(formatter)
        } else {
            var day: Date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            date = formatter.format(day)
        }
        Log.d("answer",date)
        return date
    }
}