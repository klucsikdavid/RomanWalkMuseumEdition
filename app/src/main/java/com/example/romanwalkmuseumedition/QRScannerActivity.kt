package com.example.romanwalkmuseumedition

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.romanwalkmuseumedition.model.Code
import com.example.romanwalkmuseumedition.service.FirebaseService
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.discount_details.view.*

class QRScannerActivity: AppCompatActivity() {

    private lateinit var qrCodeContent: String
    private lateinit var museumID: String
    private lateinit var projectID: String
    private lateinit var gameID: String
    private var percentage: Long = 0
    private var firestoreCodes: HashMap<String, Code> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scan_qr_button.setOnClickListener {
            qrScannerHandler()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    qrCodeContent = result.contents
                    handleDiscount()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun qrScannerHandler() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        scanner.setBeepEnabled(false)
        scanner.initiateScan()
    }

    private fun handleDiscount() {
        getDataFromQRCodeContent()
        downloadFirebaseCodes()
    }

    private fun getDataFromQRCodeContent() {
        var listOfData = qrCodeContent.split(";")
        museumID = listOfData[0]
        projectID = listOfData[1]
        gameID = listOfData[2]
        percentage = listOfData[3].toLong()
    }

    private fun downloadFirebaseCodes() {
        var firebaseService = FirebaseService(this)
        firebaseService.downloadCodes(museumID, projectID, ::loadCodes)
    }

    private fun loadCodes(map: HashMap<String, Code>) {
        firestoreCodes = map
        checkScannedCode()
        redeemGeneratedCode()
    }

    private fun checkScannedCode() {
        if (!firestoreCodes.containsKey(gameID)) {
            showPopupWithMessage("Ezt a QR-kódot nem tudjuk beváltani, mert nem szerepel az adatbázisban!")
        } else {
            showDetailsOfScannedDiscount()
        }
    }

    private fun redeemGeneratedCode() {
        var firebaseService = FirebaseService(this)
        firebaseService.redeemDiscount(museumID, projectID, gameID)
    }

    private fun showPopupWithMessage(message: String) {
        val builder = AlertDialog.Builder(this, R.style.AlertDialog_style)
        builder.setMessage(message)
        val mAlertDialog = builder.show()
    }

    private fun showDetailsOfScannedDiscount() {
        val popup = LayoutInflater.from(this).inflate(R.layout.discount_details, null)
        if (firestoreCodes.get(gameID)!!.validated) {
            popup.isRedeemed_textview.text = "Már beváltottad ezt a kedvezményt!"
        } else {
            popup.isRedeemed_textview.text = "Sikeres beváltás!"
        }
        popup.discount_textview.text = percentage.toString()
        popup.museum_textview.text = museumID
        popup.project_textview.text = projectID
        popup.gameid_textview.text = gameID

        val mBuilder = AlertDialog.Builder(this, R.style.AlertDialog_style)
            .setView(popup)
        val mAlertDialog = mBuilder.show()
    }
}