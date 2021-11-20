package nl.softable.bleuwpexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.jar.Attributes

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