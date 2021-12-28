package com.xter.pickit

import android.Manifest
import android.content.Intent
import android.content.Intent.EXTRA_ALLOW_MULTIPLE
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.xter.pickit.databinding.ActivityMainBinding
import com.xter.pickit.db.RoomDBM
import com.xter.pickit.ext.*
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.PhotoAlbumViewModel
import com.xter.pickit.ui.group.PhotoGroupViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_ablum, R.id.nav_group, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkPermission()
    }

    fun checkPermission() {
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                this,
                "需要读写存储权限",
                PERMISSION_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            toHost()
        }
    }

    fun toHost(){
        RoomDBM.get().init(this)
        val bundle = Bundle()
        if(intent?.extras != null){
            L.d("intent=${intent}")
            L.d("extras=${intent?.extras}")
            if(intent.extras!!.getBoolean(EXTRA_ALLOW_MULTIPLE)){
                bundle.putInt(KEY_PICK, PICK_EXTERNAL)
            }else{
                //外部多选
                bundle.putInt(KEY_PICK, PICK_EXTERNAL_MULTIPLE)
            }
        }else{
            bundle.putInt(KEY_PICK, PICK_NONE)
        }
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
            .setGraph(R.navigation.mobile_navigation, bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.album, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        L.d("存储权限GET")
        toHost()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun finish() {
        //如果有人来取数据，就回传数据
        val resultIntent = Intent()
        findNavController(R.id.nav_host_fragment_content_main).currentDestination?.let { navDest->
            when(navDest.id){
                R.id.nav_ablum->{
                    //TODO 两个界面的返回值不同
                    resultIntent.setDataAndType(Uri.parse("photoVM"),"image/jpeg")
                    setResult(RESULT_OK,resultIntent)
                }
                R.id.nav_group->{

                }
                else->{

                }
            }
        }
        super.finish()
    }
}