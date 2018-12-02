package com.zry.disklrucache

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.jakewharton.disklrucache.DiskLruCache
import kotlinx.android.synthetic.main.activity_main.getFirst
import kotlinx.android.synthetic.main.activity_main.getSec
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {

    val firstImgUrl = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg"
    val secImgUrl = "http://img.hb.aicdn.com/849553ec05d383aa0be488630772cbdeb59f9fec1f22bd-97yIjr_fw658"

    private lateinit var mDiskLruCache : DiskLruCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDiskLruCache()

        getFirst.setOnClickListener {
            val imageUrl = firstImgUrl
            val key = hashKeyForDisk(imageUrl)

            val snapshot = mDiskLruCache.get(key)
            if (snapshot == null) {
                Thread(Runnable {
                    try {

                        val editor = mDiskLruCache.edit(key)
                        if (editor != null) {
                            val outputStream = editor!!.newOutputStream(0)
                            if (downloadUrlToStream(imageUrl, outputStream)) {
                                editor!!.commit()
                            } else {
                                editor!!.abort()
                            }
                        }
                        mDiskLruCache.flush()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }).start()
            }
        }

        getSec.setOnClickListener {
            val imageUrl = secImgUrl
            val key = hashKeyForDisk(imageUrl)

            val snapshot = mDiskLruCache.get(key)
            if (snapshot == null) {
                Thread(Runnable {
                    try {

                        val editor = mDiskLruCache.edit(key)
                        if (editor != null) {
                            val outputStream = editor!!.newOutputStream(0)
                            if (downloadUrlToStream(imageUrl, outputStream)) {
                                editor!!.commit()
                            } else {
                                editor!!.abort()
                            }
                        }
                        mDiskLruCache.flush()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }).start()
            }
        }
    }

    private fun initDiskLruCache(){
        try {
            val cacheDir = getDiskCacheDir(this, "bitmap")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 1, (10 * 1024 * 1024).toLong())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath: String
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            cachePath = context.externalCacheDir.path
        } else {
            cachePath = context.cacheDir.path
        }
        return File(cachePath + File.separator + uniqueName)
    }

    private fun downloadUrlToStream(urlString: String, outputStream: OutputStream): Boolean {
        var urlConnection: HttpURLConnection? = null
        var out: BufferedOutputStream? = null
        var inputStream: BufferedInputStream? = null
        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            inputStream = BufferedInputStream(urlConnection!!.inputStream, 8 * 1024)
            out = BufferedOutputStream(outputStream, 8 * 1024)

            var b: Int = 0
            do {
                var b = inputStream!!.read()
                if (b == -1) break
                out!!.write(b)

            } while (true)

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (urlConnection != null) {
                urlConnection!!.disconnect()
            }
            try {
                if (out != null) {
                    out!!.close()
                }
                if (inputStream != null) {
                    inputStream!!.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun hashKeyForDisk(key: String): String {
        var cacheKey: String
        try {
            val mDigest = MessageDigest.getInstance("MD5")
            mDigest.update(key.toByteArray())
            cacheKey = bytesToHexString(mDigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            cacheKey = key.hashCode().toString()
        }

        return cacheKey
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            val hex = Integer.toHexString(0xFF and bytes[i].toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}
