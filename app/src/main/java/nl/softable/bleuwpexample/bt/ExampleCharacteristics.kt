package nl.softable.bleuwpexample.bt

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.github.jparkie.promise.Promise
import com.github.jparkie.promise.Promises
import nl.softable.bleuwpexample.Constants
import java.util.*
import kotlin.concurrent.thread


class ExampleCharacteristics(
  private val context: Context,
  adapter: BluetoothAdapter,
  val serviceUUID: String,
  deviceAddress: String
)
{
  val dev: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)

  private inner class ExampleCharacteristicsWriterWorker(val characteristics: List<ExampleWriteCharacteristic>)
  {
    var gatt: BluetoothGatt? = null
    var service: BluetoothGattService? = null
    var queue = ArrayDeque<ExampleWriteCharacteristic>()
    val promise = Promises.promise<Boolean>()

    fun start()
    {
      queue = ArrayDeque<ExampleWriteCharacteristic>(characteristics)

      gatt = dev.connectGatt(context, false, bluetoothGattCallback)
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback()
    {
      override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int)
      {
        if (newState == BluetoothProfile.STATE_CONNECTED)
        {
          Log.d(Constants.TAG, "successfully connected to the GATT Server")
          gatt?.discoverServices()
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED)
        {
          Log.d(Constants.TAG, "disconnected from the GATT Server")
        }
      }

      override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
      {
        service = gatt?.getService(UUID.fromString(serviceUUID))
        dequeueNextCharacteristicWrite()
      }

      override fun onCharacteristicWrite(
        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
      )
      {
        super.onCharacteristicWrite(gatt, characteristic, status)
        dequeueNextCharacteristicWrite()
      }

      fun dequeueNextCharacteristicWrite()
      {
        if (queue.size <= 0)
        {
          promise?.set(true)
          gatt?.disconnect()
          return
        }

        var item = queue.pop()
        var waardeuuid = UUID.fromString(item.uuid)
        var value = service?.getCharacteristic(waardeuuid)
        value?.setValue(item.value, item.format, 0)
        gatt?.writeCharacteristic(value)
      }
    }
  }

  private inner class ExampleCharacteristicsReaderWorker(val characteristics: List<ExampleReadCharacteristic>)
  {
    var gatt: BluetoothGatt? = null
    var service: BluetoothGattService? = null
    var queue = ArrayDeque<ExampleReadCharacteristic>()
    val promise = Promises.promise<Boolean>()

    fun start()
    {
      queue = ArrayDeque<ExampleReadCharacteristic>(characteristics)

      gatt = dev.connectGatt(context, false, bluetoothGattCallback)
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback()
    {
      override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int)
      {
        if (newState == BluetoothProfile.STATE_CONNECTED)
        {
          Log.d(Constants.TAG, "successfully connected to the GATT Server")
          gatt?.discoverServices()
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED)
        {
          Log.d(Constants.TAG, "disconnected from the GATT Server")
        }
      }

      override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
      {
        service = gatt?.getService(UUID.fromString(serviceUUID))
        dequeueNextCharacteristicRead()
      }

      var current: ExampleReadCharacteristic? = null

      override fun onCharacteristicRead(
        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
      )
      {
        // Update value
        var result = characteristic?.getIntValue(current!!.format, 0)
        current?.result = result

        super.onCharacteristicRead(gatt, characteristic, status)
        dequeueNextCharacteristicRead()
      }

      fun dequeueNextCharacteristicRead()
      {
        if (queue.size <= 0)
        {
          promise?.set(true)
          gatt?.disconnect()
          return
        }

        var item = queue.pop()
        current = item
        var waardeuuid = UUID.fromString(item.uuid)
        var waarde = service?.getCharacteristic(waardeuuid)
        gatt?.readCharacteristic(waarde)
      }
    }
  }

  fun write(characteristics: List<ExampleWriteCharacteristic>): Promise<Boolean>
  {
    var executer = ExampleCharacteristicsWriterWorker(characteristics)
    thread {
      executer.start()
    }
    return executer.promise
  }

  fun read(characteristics: List<ExampleReadCharacteristic>): Promise<Boolean>
  {
    var executer = ExampleCharacteristicsReaderWorker(characteristics)
    thread {
      executer.start()
    }
    return executer.promise
  }

}

class ExampleWriteCharacteristic(val uuid: String, val format: Int, val value: Int)
class ExampleReadCharacteristic(val uuid: String, val format: Int)
{
  var result: Int? = null
}
