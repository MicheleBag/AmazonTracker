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
import android.widget.ImageView
import android.widget.PopupMenu
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

        //Actionbar settings
        setSupportActionBar(home_toolbar)
        supportActionBar?.title = "Amazon Tracker"

        //DO-NOT-REMOVE
        //Used to access on device network
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        dbHandler = DBHandler(this)
        rv_home.layoutManager = LinearLayoutManager(this)

        //Handling prices update
        var todayDate = dbHandler.getDate()
        //Get the last check date from preferences
        val prefs = getSharedPreferences("Checks", Context.MODE_PRIVATE)
        val lastCheckDate = prefs.getString("lastCheck", "0000-00-00")
        if(lastCheckDate == todayDate){
            //update or do nothing
            Log.d("DailyCheck", "Daily price check already done")
        }
        else{
            //Last price check wasn't done today so insert new row
            Log.d("DailyCheck", "Daily price check")
            var items : List<Item>
            items = dbHandler.getAllItems()
            items.forEach{
                Log.d("Check: ItemURL", it.url)
                dbHandler.checkPrice(it)
            }
            //Updating last check date
            val editor = getSharedPreferences("Checks", Context.MODE_PRIVATE).edit()
            editor.putString("lastCheck", todayDate)
            editor.apply()
        }

        //Add item button settings for popup dialog
        float_btn.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_main, null)
            val itemUrl = view.findViewById<EditText>(R.id.txt_url)
            dialog.setView(view)
            dialog.setTitle("Enter an amazon item link")
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

    class HomeAdapter(val activity: MainActivity, val list:MutableList<Item>): RecyclerView.Adapter<HomeAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent0: ViewGroup, parent1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_child_home, parent0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, pos1: Int) {
            holder.itemName.text = list[pos1].name
            holder.itemPrice.text = list[pos1].price.toString()+"€"
            val itemUrl = list[pos1].url

            holder.itemName.setOnClickListener{
                //Go to item activity passing some params
                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra(INTENT_ITEM_URL, itemUrl)
                intent.putExtra(INTENT_ITEM_NAME, list[pos1].name)
                intent.putExtra(INTENT_ITEM_PRICE, list[pos1].price.toString()+"€")
                activity.startActivity(intent)
            }

            holder.menu.setOnClickListener{
                //Context insted of activity isn't working because we need to invoke refreshList()
                val popup = PopupMenu(activity, holder.menu)
                popup.inflate(R.menu.popup)
                popup.setOnMenuItemClickListener {
                    //Used when to allow future menu item adds
                    when(it.itemId){
                        R.id.menu_delete -> {
                            activity.dbHandler.deleteItem(itemUrl)
                            activity.refreshList()
                        }
                    }
                    true
                }
                popup.show()
            }
        }

        class ViewHolder(v: View): RecyclerView.ViewHolder(v){
            //Getting rv_child layot component
            val itemName:TextView = v.findViewById(R.id.tv_item_name)
            val itemPrice:TextView = v.findViewById(R.id.tv_item_price)
            val menu:ImageView = v.findViewById(R.id.iv_menu)
        }
    }


}
