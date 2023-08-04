package com.recepgemalmaz.recipesbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.recepgemalmaz.recipesbook.databinding.ActivityMainBinding
import com.recepgemalmaz.recipesbook.databinding.FragmentTarifBinding
import java.io.ByteArrayOutputStream
import kotlin.math.ln



class TarifFragment : Fragment(){
    private lateinit var binding: FragmentTarifBinding



    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTarifBinding.bind(view)

        val button = view.findViewById<View>(R.id.button)
        button.setOnClickListener {
            kaydet(it)
        }
        val imageView = view.findViewById<View>(R.id.imageView)
        imageView.setOnClickListener {
            gorselSec(it)
        }
        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (gelenBilgi.equals("menudengeldim")){
                //yeni yemek ekleme
                binding.yemekIsmiText.setText("")
                binding.yemekMalzemeText.setText("")
                binding.button.visibility = View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.yukle)
                binding.imageView.setImageBitmap(gorselSecmeArkaPlani)
            } else {
                //daha önce eklenmiş yemeği gösterme
                binding.button.visibility = View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                        val cursor = database.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzeme")
                        val yemekGorselIndex = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            binding.yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                            binding.yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorselIndex)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            binding.imageView.setImageBitmap(bitmap)
                        }

                        cursor.close()

                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }


    }

    fun kaydet(view: View){
        //Veritabanına kaydetme işlemleri
        //println("buttonnnnn")

        val yemekIsmi = binding.yemekIsmiText.text.toString()
        val yemekMalzeme = binding.yemekMalzemeText.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)
            val outPutStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outPutStream)
            val byteDizisi = outPutStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzeme VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzeme, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzeme)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()


                }

            } catch (e: Exception){
                e.printStackTrace()

            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }



    }
    fun gorselSec(view: View){
        //println("Görsel seçildi")
        //Galeriye erişim ve görsel seçme işlemleri
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

            }else{
                //izin verilmiş
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {


        if(requestCode == 1){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izin verildi
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            secilenGorsel = data.data
            try {
                activity?.let {
                    if (secilenGorsel != null){
                        if (android.os.Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)

                        }
                    }
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap , maximumBoyut : Int) : Bitmap{

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height
        val bitmapOrani : Double = width.toDouble() / height.toDouble()
        if (bitmapOrani > 1){
            //görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            //görsel dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }



    }


