package nl.softable.bleuwpexample

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import nl.softable.bleuwpexample.bt.ExampleCharacteristics
import nl.softable.bleuwpexample.bt.ExampleWriteCharacteristic
import nl.softable.bleuwpexample.databinding.ActivityCalculatorBinding

class CalculatorActivity : AppCompatActivity() {

    companion object {
        val DEVICEADDRESS = "DEVICEADDRESS"
    }

    private lateinit var deviceAddress: String
    lateinit var binding: ActivityCalculatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        deviceAddress = intent.getStringExtra(DEVICEADDRESS)!!
        title = deviceAddress

        binding = ActivityCalculatorBinding.inflate(layoutInflater)
    }

    fun queryServer(view: View) {
        var gatt = ExampleCharacteristics(
            this,
            BluetoothAdapter.getDefaultAdapter(),
            Constants.serviceUUID,
            deviceAddress
        )

        var valueOperandA = binding.edtOperandA.text.toString().toInt()
        var valueOperandB = binding.edtOperandB.text.toString().toInt()
        var valueOperator = binding.spinner.selectedItemPosition + 1

        var writes = listOf(
            ExampleWriteCharacteristic(Constants.operand1UUID, FORMAT_UINT32, valueOperandA),
            ExampleWriteCharacteristic(Constants.operand2UUID, FORMAT_UINT32, valueOperandB),
            ExampleWriteCharacteristic(Constants.operatorUUID, FORMAT_UINT32, valueOperator)
        )

        gatt.write(writes).await()

    }
}