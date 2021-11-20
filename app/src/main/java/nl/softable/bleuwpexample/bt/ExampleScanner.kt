package nl.softable.bleuwpexample.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import nl.softable.bleuwpexample.Constants

class ExampleScanner(
  bluetoothAdapter: BluetoothAdapter,
  val serviceUUID: String,
  val callback: ExampleScannerCallback
)
{
  private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
  private var scanning = false
  private val handler = Handler()
  private lateinit var device: BluetoothDevice
  private val SCANPERIOD: Long = 10000

  fun scan()
  {
    if (!scanning)
    {
      // Stops scanning after a pre-defined scan period.
      handler.postDelayed({
        scanning = false
        bluetoothLeScanner.stopScan(leScanCallback)

        if (!::device.isInitialized)
        {
          callback.onNoMatch()
        }
      }, SCANPERIOD)

      scanning = true

      var serviceId = ParcelUuid.fromString(serviceUUID)

      var filter = ScanFilter.Builder().setServiceUuid(serviceId).build()

      var settings = ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

      var filters = listOf(filter)

      bluetoothLeScanner.startScan(filters, settings, leScanCallback)
    }
    else
    {
      scanning = false
      bluetoothLeScanner.stopScan(leScanCallback)
    }
  }

  fun deviceFound(result: ScanResult)
  {
    device = result.device
    scanning = false
    callback.onMatch(result)
  }

  private val leScanCallback: ScanCallback = object : ScanCallback()
  {
    override fun onScanResult(callbackType: Int, result: ScanResult)
    {
      Log.d(
        Constants.TAG,
        "${result.device.address}, connectable=${result.isConnectable}, rssi=${result.rssi}, alias=${result.device.name}"
      )

      bluetoothLeScanner.stopScan(this)
      deviceFound(result)
    }
  }
}

abstract class ExampleScannerCallback
{
  open fun onMatch(result: ScanResult)
  {
  }

  open fun onNoMatch()
  {
  }
}