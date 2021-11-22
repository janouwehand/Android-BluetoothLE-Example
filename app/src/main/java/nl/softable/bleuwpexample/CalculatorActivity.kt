package nl.softable.bleuwpexample

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.softable.bleuwpexample.Tools.hideKeyboard
import nl.softable.bleuwpexample.bt.ExampleCharacteristics
import nl.softable.bleuwpexample.bt.ExampleReadCharacteristic
import nl.softable.bleuwpexample.bt.ExampleWriteCharacteristic
import nl.softable.bleuwpexample.databinding.ActivityCalculatorBinding
import kotlin.concurrent.thread

class CalculatorActivity : AppCompatActivity() {

    companion object {
        val DEVICEADDRESS = "DEVICEADDRESS"
    }

    private lateinit var deviceAddress: String
    var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        deviceAddress = intent.getStringExtra(DEVICEADDRESS)!!
        title = deviceAddress
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun queryServer(view: View) {
        var gatt = ExampleCharacteristics(
            this,
            BluetoothAdapter.getDefaultAdapter(),
            Constants.serviceUUID,
            deviceAddress
        )

        var edtOperandA = findViewById<EditText>(R.id.edtOperandA)
        var edtOperandB = findViewById<EditText>(R.id.edtOperandB)
        var spinner = findViewById<Spinner>(R.id.spinner)

        if (TextUtils.isEmpty(edtOperandA.text)) {
            showError("Please enter value for operand A")
            return
        }

        if (TextUtils.isEmpty(edtOperandB.text)) {
            showError("Please enter value for operand B")
            return
        }

        var valueOperandA = edtOperandA.text.toString().toIntOrNull()
        if (valueOperandA == null) {
            showError("Please enter a number for operand A")
            return
        }

        var valueOperandB = edtOperandB.text.toString().toIntOrNull()
        if (valueOperandB == null) {
            showError("Please enter a number for operand B")
            return
        }

        var valueOperator = spinner.selectedItemPosition + 1

        var writes = listOf(
            ExampleWriteCharacteristic(Constants.operand1UUID, FORMAT_UINT32, valueOperandA),
            ExampleWriteCharacteristic(Constants.operand2UUID, FORMAT_UINT32, valueOperandB),
            ExampleWriteCharacteristic(Constants.operatorUUID, FORMAT_UINT32, valueOperator)
        )

        var read = ExampleReadCharacteristic(Constants.resultUUID, FORMAT_UINT32)

        showProgress()

        thread {
            gatt.write(writes).await()
            gatt.read(listOf(read)).await()

            var result = if (read.result == null) "-" else read.result.toString()

            runOnUiThread {
                findViewById<TextView>(R.id.edtResult).apply {
                    text = result
                }
                closeProgress()
                hideKeyboard()
            }
        }
    }

    fun showProgress() {
        progress = ProgressDialog(this)
        progress?.setTitle("Communicating")
        progress?.setMessage("Wait while writing and reading characteristics...")
        progress?.setCancelable(false)
        progress?.show()
    }

    fun closeProgress() {
        progress?.dismiss()
        progress = null
    }

    fun showError(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(msg)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { dialog, which -> }
            .show()
    }
}