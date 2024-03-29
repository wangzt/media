package com.wzt.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.main_recyclerview)
        val adapter = MainAdapter(loadItemBeans())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    private fun loadItemBeans():List<ItemBean> {
        val itemBeans = mutableListOf<ItemBean>()
        itemBeans.add(ItemBean("AirHockey", "com.wzt.media.activity.AirHockeyActivity"))
        itemBeans.add(ItemBean("AirHockey2", "com.wzt.media.activity.AirHockeyActivity2"))
        itemBeans.add(ItemBean("AirHockey3", "com.wzt.media.activity.AirHockeyActivity3"))
        itemBeans.add(ItemBean("AirHockey3D", "com.wzt.media.activity.AirHockeyActivity3D"))
        itemBeans.add(ItemBean("AirHockeyTexture", "com.wzt.media.activity.AirHockeyActivityTexture"))
        itemBeans.add(ItemBean("Camera1", "com.wzt.media.camera.Camera1Activity"))
        itemBeans.add(ItemBean("Camera2", "com.wzt.media.camera.Camera2Activity"))
        return itemBeans
    }

    inner class MainAdapter(val list: List<ItemBean>): RecyclerView.Adapter<MainViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return 1
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            return MainViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false))
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            holder.updateView(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

    inner class MainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var textView: TextView = itemView.findViewById(R.id.item_text)

        fun updateView(item: ItemBean) {
            textView.text = item.name
            itemView.setOnClickListener {
                val intent = Intent()
                intent.setClassName(itemView.context, item.activity)
                itemView.context.startActivity(intent)
            }
        }
    }
}

data class ItemBean(val name: String, val activity: String)
