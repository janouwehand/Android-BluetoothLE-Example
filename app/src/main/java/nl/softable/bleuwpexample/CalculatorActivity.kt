package nl.softable.bleuwpexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : AppCompatActivity() {

    companion object{
        val DEVICEADDRESS = "DEVICEADDRESS"
    }

    private lateinit var deviceAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        deviceAddress = intent.getStringExtra(DEVICEADDRESS)!!
        title = deviceAddress
    }


}