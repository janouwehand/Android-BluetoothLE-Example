package nl.softable.bleuwpexample

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nl.softable.bleuwpexample.bt.ExampleScanner
import nl.softable.bleuwpexample.bt.ExampleScannerCallback
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    fun gotoCalculator(result: ScanResult) {
        val intent = Intent(this, CalculatorActivity::class.java).apply {
            putExtra(CalculatorActivity.DEVICEADDRESS, result.device.address)
        }
        startActivity(intent)
    }

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
    var act = this

    private var callback = object : ExampleScannerCallback() {

        override fun onMatch(result: ScanResult) {
            closeProgress()
            gotoCalculator(result)
        }

        override fun onNoMatch() {
            closeProgress()

            AlertDialog.Builder(act)
                .setTitle("Not found")
                .setMessage("The remote service could not be found. Make sure both devices are in range of each other.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok) { dialog, which -> }
                .show()
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

