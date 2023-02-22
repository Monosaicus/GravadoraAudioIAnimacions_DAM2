package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denicks21.recorder.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var mFileName: File? = null
    private var isButtonClicked: Boolean = false
    private var isMediaPlayerPrepared: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Set listeners
        binding.btnRecord.setOnClickListener {
            startRecording()
        }
        binding.btnStop.setOnClickListener {
            if (isMediaPlayerPrepared){
                stopAudio()
            }
        }
        binding.btnPlay.setOnClickListener {
            if(isButtonClicked){
                pauseAudio()
            }
            else{
                playAudio()
            }
        }
        binding.btnSave.setOnClickListener {
            saveRecording()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Comprova si s'han acceptat els permisos
    private fun checkPermissions(): Boolean {

        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    // Demana els permisos
    private fun requestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    // Inicia l'enregistrament de l'àudio
    // Si no s'han acceptat els permisos, els demana
    // Si s'han acceptat, inicia l'enregistrament
    private fun startRecording() {

        // Check permissions
        if (checkPermissions()) {

            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            binding.idTVstatus.text = "Recording in progress"
            binding.btnRecord.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
            binding.btnRecord.visibility = Button.GONE
        } else {
            // Request permissions
            requestPermissions()
        }
    }

    // Para l'enregistrament i guarda el fitxer
    // Si no s'ha iniciat l'enregistrament, mostra un missatge
    // Si s'ha iniciat, para l'enregistrament i guarda el fitxer
    private fun saveRecording() {
        // Stop recording
        if (mFileName == null) {

            // Message
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_LONG).show()

        } else if (mRecorder != null) {
            binding.btnRecord.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
            binding.btnRecord.visibility = Button.VISIBLE
            binding.btnSave.startAnimation(AnimationUtils.loadAnimation(this, R.anim.btnclickanim))
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            binding.idTVstatus.text = "Recording interrupted"
        }
    }

    // Reprodueix l'àudio
    // Si no s'ha preparat el mPlayer, el prepara
    // Si s'ha preparat, el reprodueix
    private fun playAudio() {
        try {
            if (!isMediaPlayerPrepared){
                // Use the MediaPlayer class to listen to recorded audio files
                mPlayer = MediaPlayer()
                // Preset la font del file audio
                mPlayer!!.setDataSource(mFileName.toString())

                // Fetch the source of the mPlayer
                mPlayer!!.prepare()
                isMediaPlayerPrepared = true
            }

            // Start the mPlayer
            mPlayer!!.start()
            binding.idTVstatus.text = "Listening recording"
            binding.btnPlay.setBackgroundResource(R.drawable.btn_rec_pause)
            isButtonClicked = true
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
        }
    }

    // Pausa la reproducció de l'àudio
    private fun pauseAudio() {
        // Stop playing the audio file
        binding.idTVstatus.text = "Recording paused"
        mPlayer!!.pause()
        binding.btnPlay.setBackgroundResource(R.drawable.btn_rec_play)
        isButtonClicked = false
    }

    // Atura la reproducció de l'àudio i reinicia el mPlayer
    private fun stopAudio(){
        binding.idTVstatus.text = "Recording stopped"
        mPlayer!!.stop()
        binding.btnPlay.setBackgroundResource(R.drawable.btn_rec_play)
        isMediaPlayerPrepared = false
        isButtonClicked = false
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}