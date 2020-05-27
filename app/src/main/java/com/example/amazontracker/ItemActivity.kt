package com.example.amazontracker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        setSupportActionBar(item_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Dettagli"

        val itemName: TextView = this.findViewById(R.id.tv_item)
        val itemPrice: TextView = this.findViewById(R.id.tv_item_price)
        itemName.text = intent.getStringExtra(INTENT_ITEM_NAME)
        itemPrice.text = "Prezzo attuale:"+intent.getStringExtra(INTENT_ITEM_PRICE)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean{
        return if(item?.itemId == android.R.id.home){
            finish()
            true
        }else
            super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        getData(this)
        super.onResume()
    }

    fun getData(context: Context){

    }
}
