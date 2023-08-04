package com.recepgemalmaz.recipesbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import com.recepgemalmaz.recipesbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.yemek_ekle,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.yemekEkle_item){
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim", 0)
            Navigation.findNavController(this,R.id.fragmentContainerView).navigate(action)
        }

        
        return super.onOptionsItemSelected(item)

    }
}