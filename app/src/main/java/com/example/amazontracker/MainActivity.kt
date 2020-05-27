package com.example.amazontracker

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    lateinit var  dbHandler: DBHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(home_toolbar)
        supportActionBar?.title = "Amazon Tracker"
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)



        dbHandler = DBHandler(this)
        rv_home.layoutManager = LinearLayoutManager(this)

        float_btn.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_main, null)
            val itemUrl = view.findViewById<EditText>(R.id.txt_url)
            dialog.setView(view)
            dialog.setPositiveButton("Add"){_: DialogInterface, _:Int ->
                if(itemUrl.text.isNotEmpty()){
                    val item = Item()
                    item.url = itemUrl.text.toString()
                    dbHandler.addItem(item)
                    refreshList()
                    Log.d("url",item.url)
                }
            }
            dialog.setNegativeButton("Cancel"){_:DialogInterface, _:Int ->

            }
            dialog.show()
        }
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList(){
        rv_home.adapter = HomeAdapter(this, dbHandler.getItem())
    }

    class HomeAdapter(val context: Context,val list:MutableList<Item>): RecyclerView.Adapter<HomeAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent0: ViewGroup, parent1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_child_home, parent0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, pos1: Int) {
            holder.itemName.text = list[pos1].name
            holder.itemPrice.text = list[pos1].price.toString()+"€"
            val itemUrl = list[pos1].url
            holder.itemName.setOnClickListener{
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra(INTENT_ITEM_URL, itemUrl)
                intent.putExtra(INTENT_ITEM_NAME, list[pos1].name)
                intent.putExtra(INTENT_ITEM_PRICE, list[pos1].price.toString()+"€")
                context.startActivity(intent)
            }
        }

        class ViewHolder(v: View): RecyclerView.ViewHolder(v){
            val itemName:TextView = v.findViewById(R.id.tv_item_name)
            val itemPrice:TextView = v.findViewById(R.id.tv_item_price)
        }
    }


}
