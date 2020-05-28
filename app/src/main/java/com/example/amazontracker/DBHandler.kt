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
        /*
         * Scrape data from amazon link using jsoup and insert those data in $TABLE_ITEM.
         * After insert it checks the daily price.
         */
        val db = writableDatabase
        val doc = Jsoup.connect(item.url).get()
        item.name = doc.getElementById("productTitle").text()
        val cv = ContentValues()
        cv.put(COL_URL, item.url)
        cv.put(COL_NAME, item.name)
        val result:Long = db.insert(TABLE_ITEM, null, cv)
        checkPrice(item)
        return result != (-1).toLong()
    }

    fun checkPrice(item: Item){
        /*
         * Check daily item price and insert it in $TABLE_PRICE
         */
        val db = writableDatabase
        val date: String = getDate()
        val doc = Jsoup.connect(item.url).get()
        //val price = doc.getElementById("priceblock_ourprice").text()

        //Random price used to test price chart
        val price  = (0..300).random()

        val cv = ContentValues()
        cv.put(COL_ITEM_URL, item.url)
        cv.put(COL_DATE, date)
        cv.put(COL_PRICE, price)
        db.insert(TABLE_PRICE, null, cv)
    }

    fun getItem() : MutableList<Item>{
        /*
         * Return list of items with current daily price
         */
        val result: MutableList<Item> = ArrayList()
        val db:SQLiteDatabase = readableDatabase
        val date = getDate()
        val queryResult = db.rawQuery("SELECT * from $TABLE_ITEM INNER JOIN $TABLE_PRICE ON $TABLE_ITEM.$COL_URL=$TABLE_PRICE.$COL_ITEM_URL WHERE $COL_DATE='$date'", null)

        if(queryResult.moveToFirst()){
            do{
                val item = Item()
                item.name = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                item.url = queryResult.getString(queryResult.getColumnIndex(COL_URL))
                item.price = queryResult.getFloat(queryResult.getColumnIndex(COL_PRICE))
                result.add(item)
            }while(queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }

    fun getAllItems() : MutableList<Item>{
        val result: MutableList<Item> = ArrayList()
        val db:SQLiteDatabase = readableDatabase

        val queryResult = db.rawQuery("SELECT * from $TABLE_ITEM", null)
        if(queryResult.moveToFirst()){
            do{
                val item = Item()
                item.name = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                item.url = queryResult.getString(queryResult.getColumnIndex(COL_URL))
                result.add(item)
            }while(queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }

    fun getItemHistory(url: String): MutableList<Item>{
        /*
         * Return list of (price,data) of an item
         */
        val result: MutableList<Item> = ArrayList()
        val db:SQLiteDatabase = readableDatabase

        val queryResult = db.rawQuery("SELECT * from $TABLE_PRICE WHERE $COL_ITEM_URL='$url'", null)
        if(queryResult.moveToFirst()){
            do{
                val item = Item()
                item.price = queryResult.getFloat(queryResult.getColumnIndex(COL_PRICE))
                item.data = queryResult.getString(queryResult.getColumnIndex(COL_DATE))
                result.add(item)
            }while(queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }


    fun deleteItem(url : String){
        /*
         *Delete item and all stored prices
         */
        val db = writableDatabase
        db.delete(TABLE_PRICE, "$COL_ITEM_URL=?", arrayOf(url.toString()))
        db.delete(TABLE_ITEM, "$COL_URL=?", arrayOf(url.toString()))
    }

    fun getDate() : String{
        //Return today date : String
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
        return date
    }
}