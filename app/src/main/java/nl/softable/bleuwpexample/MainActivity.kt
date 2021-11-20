package nl.softable.bleuwpexample

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.course1.ExampleScanner
import com.example.course1.ExampleScannerCallback
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "BLE example"

        var cont = true

        // Check to see if the BLE feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
            ?.also {
                cont = false
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show()
                Timer("ShowingToast", false).schedule(2000) {
                    finish()
                }
            }

        if (cont) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                }
            }
        }
    }

    var progress: ProgressDialog? = null

    private var callback = object : ExampleScannerCallback() {

        override fun onMatch(result: ScanResult) {
            closeProgress()
        }

        override fun onNoMatch() {
            closeProgress()
        }

        fun closeProgress() {
            progress?.dismiss()
            progress = null
        }
    }

    fun lookForServer(view: View) {
        if (progress == null) {

            progress = ProgressDialog(this)
            progress?.setTitle("Searching")
            progress?.setMessage("Wait while searching...")
            progress?.setCancelable(false)
            progress?.show()

            val adapter = BluetoothAdapter.getDefaultAdapter()
            ExampleScanner(adapter, Constants.serviceUUID, callback).scan()
        }
    }

}

