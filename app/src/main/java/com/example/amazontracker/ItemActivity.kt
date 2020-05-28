package com.example.amazontracker

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_item.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ItemActivity : AppCompatActivity() {
    lateinit var  dbHandler: DBHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        //Setting up my actionBar
        setSupportActionBar(item_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Details"

        //Setting rendered component
        val itemName: TextView = this.findViewById(R.id.tv_item)
        val itemPrice: TextView = this.findViewById(R.id.tv_item_price)
        val itemHistory: TextView = this.findViewById(R.id.tv_history)
        val itemPoT: TextView = this.findViewById(R.id.price_over_time)
        itemName.text = intent.getStringExtra(INTENT_ITEM_NAME)
        itemPrice.text = "Current price : "+intent.getStringExtra(INTENT_ITEM_PRICE)
        itemHistory.text = getHistory(intent.getStringExtra(INTENT_ITEM_URL))
        itemPoT.text = "Price over time"

        //Chart init
        val graph : GraphView = findViewById(R.id.chart)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(getDataPoints(intent.getStringExtra(INTENT_ITEM_URL)))
        graph.addSeries(series)

        //Chart settings
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        graph.gridLabelRenderer.numHorizontalLabels = 3
        graph.gridLabelRenderer.setHorizontalLabelsAngle(15)
        graph.gridLabelRenderer.numVerticalLabels = 10
        graph.gridLabelRenderer.horizontalAxisTitle = "Days"
        graph.gridLabelRenderer.verticalAxisTitle = "Price"
        graph.gridLabelRenderer.horizontalAxisTitleColor = Color.RED
        graph.gridLabelRenderer.verticalAxisTitleColor = Color.RED
        graph.viewport.setMinY(0.toDouble())
        graph.viewport.isScrollable = true
        graph.viewport.setScrollableY(true)
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
    }

    private fun getDataPoints(url: String): Array<DataPoint> {
        /*
         * Return array of datapoint used to fill chart
         */
        dbHandler = DBHandler(this)
        var items : List<Item>
        var values: MutableList<DataPoint> = ArrayList()
        items = dbHandler.getItemHistory(url)
        items.forEach{
            val format = SimpleDateFormat("yyyy-MM-dd")
            val x : Date = format.parse(it.data)
            val y : Double = it.price!!.toDouble()
            val dp = DataPoint(x, y)
            values.add(dp)
        }
        return values.toTypedArray()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean{
        //It handle back to the home
        return if(item?.itemId == android.R.id.home){
            finish()
            true
        }else
            super.onOptionsItemSelected(item)
    }


    fun getHistory(url: String) : String{
        /*
         * Return a string that contains every (date,price) of passed item
         */
        dbHandler = DBHandler(this)
        var items : List<Item>
        var myString:String = ""
        items = dbHandler.getItemHistory(url)
        items.forEach{
            myString = myString + it.data + "\u0020:\u0020"+ it.price + "€\n"
        }
        return myString
    }
}
